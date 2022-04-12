(ns opa4j.parser
  (:require [clojure.data.json :as json])
  (:require [clojure.edn :as edn])
  (:require [clojure.string :as str])
  (:import (java.time Instant)))

(declare make-block)
(declare make-blocks)

(defn log
  ([level msg]
   (let [now (Instant/now)]
     (println (format "%s: %s %s" now level msg))))
  ([level msg & args]
   (log level (apply format msg args))))

(defn log-info
  ([msg] (log "INFO" msg))
  ([msg & args] (apply log "INFO" msg args)))

(defn log-debug
  ([msg] (log "DEBUG" msg))
  ([msg & args] (apply log "DEBUG" msg args)))

(defn log-trace
  ([msg] (log "TRACE" msg))
  ([msg & args] (apply log "TRACE" msg args)))

(defn get-local [state index]
  (get (get state :local) index))

(defn set-local [state index value]
  (let [local (get state :local)]
    (assoc state :local (assoc local index value))))

(defn get-value [state key]
  (let [static (get state :static)
        key-type (get key "type")
        key-value (get key "value")]
    (case key-type
      "local" (get-local state key-value)
      "string_index" (let [strings (get static "strings")]
                       (get (get strings key-value) "value"))
      "bool" key-value
      (throw (Exception. (format "unknown value type '%s'" key-type))))))

(defn get-static-string [state index]
  (get-value state {"type" "string_index" "value" index}))

(defn break
  ([state] (assoc state :break-index 0))
  ([state break-index] (assoc state :break-index break-index)))

(defn add-result [state value]
  (let [result-set (get state :result)]
    (assoc state :result-set (conj result-set value))))

(defn make-ArrayAppendStmt [stmt-info]
  (log-debug "making ArrayAppendStmt stmt")
  (let [array-index (get stmt-info "array")
        value-index (get stmt-info "value")]
    (fn [state]
      (let [array (get-local state array-index)]
        (if (nil? array)
          (do
            (log-debug "ArrayAppendStmt - <%s> is not a local var" array-index)
            (break state))
          (let [val (get-value state value-index)]
            (if (nil? val)
              (do
                (log-debug "ArrayAppendStmt - value <%s> not present" value-index)
                (break state))
              (do
                (log-debug "ArrayAppendStmt - Appending '%s' to <%s>" val array-index)
                (set-local state array-index (conj array val))))))))))

(defn make-AssignVarStmt [stmt-info]
  (log-debug "making AssignVarStmt stmt")
  (let [source-index (get stmt-info "source")
        target (get stmt-info "target")]
    (fn [state]
      (let [val (get-value state source-index)]
        (if (nil? val)
          (do
            (log-debug "AssignVarStmt - <%s> not present" source-index)
            (break state))
          (do
            (log-debug "AssignVarStmt - assigning '%s' from <%s> to <%s>" val source-index target)
            (set-local state target val)))))))

(defn make-AssignVarOnceStmt [stmt-info]
  (log-debug "making AssignVarOnceStmt stmt")
  (let [source-index (get stmt-info "source")
        target (get stmt-info "target")]
    (fn [state]
      (if-not (nil? (get-local state target))
        (throw (Exception. (format "local %s already assigned" target)))
        (let [val (get-value state source-index)]
          (log-debug "AssignVarStmt - assigning '%s' from %s to %s" val source-index target)
          (set-local state target val))))))

(defn make-BlockStmt [stmt-info]
  (log-debug "making BlockStmt stmt")
  (log-trace "info: %s" stmt-info)
  (let [blocks (make-blocks (get stmt-info "blocks"))]
    (fn [state]
      (log-trace "BlockStmt - local vars: %s" (get state :local))
      (blocks state))))

(defn make-BreakStmt [stmt-info]
  (log-debug "making BreakStmt stmt")
  (let [index (get stmt-info "index")]
    (fn [state]
      (log-debug "BreakStmt - index: %s", index)
      (break state index))))

(defn map-by-index [array]
  (loop [array array
         map {}
         i 0]
    (if (empty? array)
      map
      (recur (next array) (assoc map i (first array)) (inc i)))))

(defn make-CallStmt [stmt-info]
  (log-debug "making CallStmt stmt")
  (let [target (get stmt-info "result")
        func-name (get stmt-info "func")]
    (fn [state]
      (log-debug "CallStmt - calling func <%s>" func-name)
      (let [func (get (get state :funcs) func-name)
            local (map-by-index (map (fn [arg] (get-value state arg)) (get stmt-info "args")))
            func-state {:static (get state :static)
                        :funcs  (get state :funcs)
                        :local  local}
            result (func func-state)]
        (log-debug "CallStmt - <%s> returning: '%s'" func-name result)
        (if (nil? result)
          (break state)
          (set-local state target result))))))

(defn make-DotStmt [stmt-info]
  (let [source-index (get stmt-info "source")
        key-index (get stmt-info "key")
        target (get stmt-info "target")]
    (log-debug "making DotStmt stmt")
    (fn [state]
      (let [source (get-value state source-index)
            key (get-value state key-index)]
        (if (nil? source)
          (do
            (log-debug "DotStmt - <%s> not present" source-index)
            (break state))
          (let [val (get (get-value state source-index) key)]
            (if-not (nil? val)
              (do
                (log-debug "DotStmt - got '%s' to var <%s>" val target)
                (set-local state target val))
              (do
                (log-debug "DotStmt - <%s> not present in <%s>" key source-index)
                (break state)))))))))

(defn make-EqualStmt [stmt-info]
  (log-debug "making EqualStmt stmt")
  (let [a-index (get stmt-info "a")
        b-index (get stmt-info "b")]
    (fn [state]
      (let [a (get-value state a-index)
            b (get-value state b-index)
            result (= a b)]
        (log-debug "EqualStmt - '%s' != '%s' == %s" a b result)
        (if result
          state
          (break state))))))

(defn make-IsDefinedStmt [stmt-info]
  (log-debug "making IsDefinedStmt stmt")
  (let [source (get stmt-info "source")]
    (fn [state]
      (if (contains? (get state :local) source)
        (do
          (log-debug "%d is defined" source)
          state)
        (do
          (log-debug "%d is not defined" source)
          (break state))))))

(defn make-MakeNumberRefStmt [stmt-info]
  (log-debug "making MakeNumberRefStmt stmt")
  (let [index (get stmt-info "Index")                       ; NOTE: 'Index' is capitalized
        target (get stmt-info "target")]
    (fn [state]
      (let [val (edn/read-string (get-static-string state index))]
        (log-debug "MakeNumberRefStmt - parsed number: %s" val)
        (set-local state target val)))))

(defn make-MakeArrayStmt [stmt-info]
  (log-debug "making MakeArrayStmt stmt: %s" stmt-info)
  (let [target (get stmt-info "target")]
    (fn [state]
      (log-debug "MakeArrayStmt - assigning empty array to local var %d" target)
      (set-local state target []))))

(defn make-MakeObjectStmt [stmt-info]
  (log-debug "making MakeObjectStmt stmt: %s" stmt-info)
  (let [target (get stmt-info "target")]
    (fn [state]
      (log-debug "MakeObjectStmt - assigning empty object to local var %d" target)
      (set-local state target {}))))

(defn make-MakeSetStmt [stmt-info]
  (log-debug "making MakeSetStmt stmt: %s" stmt-info)
  (let [target (get stmt-info "target")]
    (fn [state]
      (log-debug "MakeSetStmt - assigning empty set to local var %d" target)
      (set-local state target #{}))))

(defn make-NotEqualStmt [stmt-info]
  (log-debug "making NotEqualStmt stmt")
  (let [a-index (get stmt-info "a")
        b-index (get stmt-info "b")]
    (fn [state]
      (let [a (get-value state a-index)
            b (get-value state b-index)
            result (not= a b)]
        (log-debug "NotEqualStmt - '%s' != '%s' == %s" a b result)
        (if result
          state
          (break state))))))

(defn make-ObjectInsertStmt [stmt-info]
  (log-debug "making ObjectInsertStmt stmt")
  (let [key-index (get stmt-info "key")
        value-index (get stmt-info "value")
        object-index (get stmt-info "object")]
    (fn [state]
      (log-trace "ObjectInsertStmt - info: %s" stmt-info)
      (let [object (get-local state object-index)
            key (get-value state key-index)
            value (get-value state value-index)]
        (log-debug "ObjectInsertStmt - inserting '%s' at '%s' to var %d" value key object-index)
        (set-local state object-index (assoc object key value))))))

(defn make-ObjectMergeStmt [stmt-info]
  (log-debug "making ObjectMergeStmt stmt")
  (fn [state]
    (let [to-key (get stmt-info "a")
          from-key (get stmt-info "b")
          target-key (get stmt-info "target")]
      (log-debug "ObjectMergeStmt - merging %d and %d into %d" to-key from-key target-key)
      (set-local state target-key (merge (get-local state to-key) (get-local state from-key))))))

(defn make-ResetLocalStmt [stmt-info]
  (log-debug "making ResetLocalStmt stmt")
  (let [target (get stmt-info "target")]
    (fn [state]
      (log-debug "ResetLocalStmt - resetting %d" target)
      (dissoc state target))))

(defn make-ResultSetAddStmt [stmt-info]
  (log-debug "making ResultSetAddStmt stmt")
  (let [value (get stmt-info "value")]
    (fn [state]
      (let [val (get-local state value)]
        (if (nil? val)
          (do
            (log-debug "ResultSetAddStmt - nothing to add to result set")
            state)
          (do
            (log-debug "ResultSetAddStmt - adding %s to resultset" val)
            (add-result state val)))))))

(defn make-ReturnLocalStmt [_]
  (log-debug "making ReturnLocalStmt stmt")
  (fn [state]
    (log-debug "ReturnLocalStmt - exiting function")        ; TODO: Do we need to recursively break out of all nested blocks to exit the function?
    state))                                                 ; No-op, the function itself knows what local var is the result

(defn make-SetAddStmt [stmt-info]
  (log-debug "making SetAddStmt stmt")
  (let [set-index (get stmt-info "set")
        value-index (get stmt-info "value")]
    (fn [state]
      (let [set (get-local state set-index)]
        (if (nil? set)
          (do
            (log-debug "SetAddStmt - <%s> is not a local var" set-index)
            (break state))
          (let [val (get-value state value-index)]
            (if (nil? val)
              (do
                (log-debug "SetAddStmt - value <%s> not present" value-index)
                (break state))
              (do
                (log-debug "SetAddStmt - Adding '%s' to <%s>" val set-index)
                (set-local state set-index (conj set val))))))))))

(defn make-ScanStmt [stmt-info]
  (log-debug "making ScanStmt stmt")
  (let [source-index (get stmt-info "source")
        key-index (get stmt-info "key")
        value-index (get stmt-info "value")
        block-stmt (make-block (get stmt-info "block"))]
    (fn [state]
      (log-debug "ScanStmt - scanning <%d>" source-index)
      (let [source (get-local state source-index)]
        (if (or (nil? source) (not (coll? source)) (empty? source))
          (do
            (log-debug "ScanStmt - '%s' is not a collection" source-index)
            (break state))
          (if (map? source)
            (do
              (log-trace "ScanStmt - source is map")
              (throw (Exception. (format "ScanStmt not implemented for maps"))))
            (do
              (log-trace "ScanStmt - source is list")
              (loop [source-indexed (map-indexed (fn [i v] [i v]) source)
                     state state]
                (if (empty? source-indexed)
                  (do
                    (log-trace "ScanStmt - done")
                    (break state))
                  (let [entry (first source-indexed)
                        key (get entry 0)
                        value (get entry 1)
                        state (set-local state key-index key)
                        state (set-local state value-index value)]
                    (recur (next source-indexed) (block-stmt state))))))))))))

(defn make-stmt [stmt-info]
  (log-debug "making stmt" stmt-info)
  (let [type (get stmt-info "type")
        stmt-info (get stmt-info "stmt")
        stmt (case type
               "ArrayAppendStmt" (make-ArrayAppendStmt stmt-info)
               "AssignVarStmt" (make-AssignVarStmt stmt-info)
               "AssignVarOnceStmt" (make-AssignVarOnceStmt stmt-info)
               "BlockStmt" (make-BlockStmt stmt-info)
               "BreakStmt" (make-BreakStmt stmt-info)
               "CallStmt" (make-CallStmt stmt-info)
               "DotStmt" (make-DotStmt stmt-info)
               "EqualStmt" (make-EqualStmt stmt-info)
               "IsDefinedStmt" (make-IsDefinedStmt stmt-info)
               "MakeArrayStmt" (make-MakeArrayStmt stmt-info)
               "MakeNumberRefStmt" (make-MakeNumberRefStmt stmt-info)
               "MakeObjectStmt" (make-MakeObjectStmt stmt-info)
               "MakeSetStmt" (make-MakeSetStmt stmt-info)
               "NotEqualStmt" (make-NotEqualStmt stmt-info)
               "ObjectInsertStmt" (make-ObjectInsertStmt stmt-info)
               "ObjectMergeStmt" (make-ObjectMergeStmt stmt-info)
               "ResetLocalStmt" (make-ResetLocalStmt stmt-info)
               "ResultSetAddStmt" (make-ResultSetAddStmt stmt-info)
               "ReturnLocalStmt" (make-ReturnLocalStmt stmt-info)
               "ScanStmt" (make-ScanStmt stmt-info)
               "SetAddStmt" (make-SetAddStmt stmt-info)
               (throw (Exception. (format "%s statement type not implemented" type))))]
    (fn [state]
      (log-trace "%s - calling with info: %s, vars: %s" type stmt-info (get state :local))
      (stmt state))))

(defn make-stmts [stmts-info]
  (log-debug "making stmts")
  (let [stmts (doall (for [stmt-info stmts-info]
                       (make-stmt stmt-info)))]
    (fn [state]
      (log-debug "executing statements")
      (loop [stmts stmts
             state state]
        (let [stmts-count (count stmts)]
          (if (or (zero? stmts-count) (contains? state :break-index))
            (do
              (if (pos? stmts-count)
                (log-trace "skipping %d statement(s)" stmts-count)
                "no-op")
              state)
            (recur (next stmts) ((first stmts) state))))))))

(defn make-block [block-info]
  (log-debug "making block")
  (let [stmts-info (get block-info "stmts")
        stmts (make-stmts stmts-info)]
    (fn [state]
      (log-debug "executing block")
      (stmts state))))

(defn make-blocks [blocks-info]
  (log-debug "making blocks")
  (let [blocks (doall (for [block-info blocks-info]
                        (make-block block-info)))]
    (fn [state]
      (log-debug "executing blocks")
      (loop [blocks blocks
             state state]
        (let [break-index (get state :break-index)]
          (if-not (nil? break-index)
            (do
              (log-trace "breaking out of block; index: %d" break-index)
              (let [new-break-index (dec break-index)]
                (if (>= new-break-index 0)
                  (assoc state :break-index new-break-index)
                  (dissoc state :break-index))))
            (if (empty? blocks)
              state
              (recur (next blocks) ((first blocks) state)))))))))

; the data document seems to be expected to be a hierarchy of maps resembling the entry-point path (plan name).
(defn populate-data [plan-info]
  (let [plan-name (get plan-info "name")]
    (if (pos? (count plan-name))
      (loop [components (reverse (str/split plan-name #"/"))
             result {}]
        (if (empty? components)
          result
          (recur (next components) {(first components) result})))
      {})))

(defn make-plan [plan-info]
  (let [name (get plan-info "name")
        blocks-info (get plan-info "blocks")]
    (log-debug "making plan '%s'" name)
    (log-trace "plan: %s" plan-info)
    (let [blocks (make-blocks blocks-info)]
      [name (fn [info input]
              (let [state (assoc info :local {0 input
                                              1 (populate-data plan-info)})]
                (log-debug "Plan - executing '%s'" name)
                (let [state (blocks state)
                      result-set (get state :result-set)]
                  (log-debug "Plan - result-set: %s" result-set)
                  result-set)))])))

(defn make-plans [plans-info]
  (log-debug "making plans")
  (doall (for [plan-info (get plans-info "plans")]
           (make-plan plan-info))))

(defn make-func [func-info]
  (let [name (get func-info "name")
        return (get func-info "return")
        blocks-info (get func-info "blocks")
        blocks (make-blocks blocks-info)]
    [name (fn [state]
            (log-debug "executing func '%s'" name)
            (let [state (blocks state)
                  result (get-local state return)]
              (log-debug "function '%s' returning '%s'" name result)
              result))]))

(defn make-funcs [funcs-info]
  (log-debug "making funcs")
  (loop [func-infos (get funcs-info "funcs")
         func-map {}]
    (if (empty? func-infos)
      func-map
      (let [[name func] (make-func (first func-infos))]
        (recur (next func-infos) (assoc func-map name func))))))

(defn parse
  "Parses the incoming string"
  [str] (let [ir (json/read-str str)
              static (get ir "static")
              plans-info (get ir "plans")
              funcs-info (get ir "funcs")]
          {:plans  (make-plans plans-info)
           :funcs  (make-funcs funcs-info)
           :static static}))

(defn parse-file
  "Reads and parses the incoming file"
  ([file]
   (log-debug "Parsing file '%s'" file)
   (parse (slurp file))))
;
; Copyright 2022 Johan Fylling, Anders Eknert
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
;

(ns jarl.parser
  (:require [clojure.data.json :as json])
  (:require [clojure.edn :as edn])
  (:require [clojure.tools.logging :as log])
  (:require [clojure.string :as str])
  (:require [jarl.builtins.registry :as builtins])
  (:import (se.fylling.jarl BuiltinException)))

(declare make-block)
(declare make-blocks)

(defn get-local [state index]
  (get (get state :local) index))

(defn contains-local? [state index]
  (contains? (get state :local) index))

(defn dissoc-local [state index]
  (let [local (get state :local)]
    (assoc state :local (dissoc local index))))

(defn get-string [state index]
  (get (get (get (get state :static) "strings") index) "value"))

(defn contains-string? [state index]
  (contains? (get (get state :static) "strings") index))

(defn set-local [state index value]
  (let [local (get state :local)]
    (assoc state :local (assoc local index value))))

(defn get-value [state key]
  (let [key-type (get key "type")
        key-value (get key "value")]
    (case key-type
      "local" (get-local state key-value)
      "string_index" (get-string state key-value)
      "bool" key-value
      (throw (Exception. (format "unknown value type '%s'" key-type))))))

(defn contains-value? [state key]
  (let [key-type (get key "type")
        key-value (get key "value")]
    (case key-type
      "local" (contains-local? state key-value)
      "string_index" (contains-string? state key-value)
      "bool" true
      (throw (Exception. (format "unknown value type '%s'" key-type))))))

(defn get-static-string [state index]
  (get-value state {"type" "string_index" "value" index}))

(defn get-func [state name]
  (let [func (get (get state :funcs) name)]
    (if-not (nil? func)
      func
      (get (get state :builtin-funcs) name))))

(defn break
  ([state] (assoc state :break-index 0))
  ([state break-index] (assoc state :break-index break-index)))

(defn add-result [state value]
  (let [result-set (get state :result)]
    (assoc state :result-set (conj result-set value))))

(defn make-ArrayAppendStmt [stmt-info]
  (log/debugf "making ArrayAppendStmt stmt")
  (let [array-index (get stmt-info "array")
        value-index (get stmt-info "value")]
    (fn [state]
      (if-not (contains-local? state array-index)
        (do
          (log/infof "ArrayAppendStmt - <%s> is not a local var" array-index)
          (break state))
        (let [val (get-value state value-index)]
          (if (nil? val)
            (do
              (log/debugf "ArrayAppendStmt - value <%s> not present" value-index)
              (break state))
            (let [array (get-local state array-index)]
              (log/debugf "ArrayAppendStmt - Appending '%s' to <%s>" val array-index)

              (set-local state array-index (conj array val)))))))))

(defn make-AssignVarStmt [stmt-info]
  (log/debugf "making AssignVarStmt stmt")
  (let [source-index (get stmt-info "source")
        target (get stmt-info "target")]
    (fn [state]
      (if-not (contains-value? state source-index)
        (do
          (log/debugf "AssignVarStmt - <%s> not present" source-index)
          (break state))
        (let [val (get-value state source-index)]
          (log/debugf "AssignVarStmt - assigning '%s' from <%s> to <%s>" val source-index target)
          (set-local state target val))))))

(defn make-AssignVarOnceStmt [stmt-info]
  (log/debugf "making AssignVarOnceStmt stmt")
  (let [source-index (get stmt-info "source")
        target (get stmt-info "target")]
    (fn [state]
      (if (contains-local? state target)
        (throw (Exception. (format "local %s already assigned" target)))
        (if-not (contains-value? state source-index)
          (do
            (log/debugf "AssignVarOnceStmt - <%s> not present" source-index)
            (break state))
          (let [val (get-value state source-index)]
            (log/debugf "AssignVarOnceStmt - assigning '%s' from <%s> to <%s>" val source-index target)
            (set-local state target val)))))))

(defn make-BlockStmt [stmt-info]
  (log/debugf "making BlockStmt stmt")
  (log/tracef "info: %s" stmt-info)
  (let [blocks (make-blocks (get stmt-info "blocks"))]
    (fn [state]
      (log/debugf "BlockStmt - ")
      (blocks state))))

(defn make-BreakStmt [stmt-info]
  (log/debugf "making BreakStmt stmt")
  (let [index (get stmt-info "index")]
    (fn [state]
      (log/debugf "BreakStmt - index: %s", index)
      (break state index))))

(defn map-by-index [array]
  (loop [array array
         map {}
         i 0]
    (if (empty? array)
      map
      (recur (next array) (assoc map i (first array)) (inc i)))))

(defn make-CallStmt [stmt-info]
  (log/debugf "making CallStmt stmt")
  (let [target (get stmt-info "result")
        func-name (get stmt-info "func")]
    (fn [state]
      (log/debugf "CallStmt - calling func <%s>" func-name)
      (let [func (get-func state func-name)]
        (if (nil? func)
          (throw (Exception. (format "unknown function '%s'" func-name)))
          (let [local (map-by-index (map (fn [arg] (get-value state arg)) (get stmt-info "args")))
                func-state {:static        (get state :static)
                            :funcs         (get state :funcs)
                            :builtin-funcs (get state :builtin-funcs)
                            :local         local}
                result (func func-state)]
            (if (contains? result :result)
              (do
                (log/debugf "CallStmt - <%s> returning: '%s'" func-name result)
                (set-local state target (get result :result)))
              (do
                (log/debugf "CallStmt - <%s> undefined" func-name)
                (break state)))))))))

(defn make-DotStmt [stmt-info]
  (let [source-index (get stmt-info "source")
        key-index (get stmt-info "key")
        target (get stmt-info "target")]
    (log/debugf "making DotStmt stmt")
    (fn [state]
      (let [source (get-value state source-index)
            key (get-value state key-index)]
        (if (nil? source)
          (do
            (log/debugf "DotStmt - <%s> not present" source-index)
            (break state))
          (let [val (get (get-value state source-index) key)]
            (if-not (nil? val)
              (do
                (log/debugf "DotStmt - got '%s' to var <%s>" val target)
                (set-local state target val))
              (do
                (log/debugf "DotStmt - <%s> not present in <%s>" key source-index)
                (break state)))))))))

(defn make-EqualStmt [stmt-info]
  (log/debugf "making EqualStmt stmt")
  (let [a-index (get stmt-info "a")
        b-index (get stmt-info "b")]
    (fn [state]
      (let [a (get-value state a-index)
            b (get-value state b-index)
            result (= a b)]
        (log/debugf "EqualStmt - '%s' != '%s' == %s" a b result)
        (if result
          state
          (break state))))))

(defn make-IsDefinedStmt [stmt-info]
  (log/debugf "making IsDefinedStmt stmt")
  (let [source (get stmt-info "source")]
    (fn [state]
      (if (contains-local? state source)
        (do
          (log/debugf "IsDefinedStmt - local var <%d> is defined" source)
          state)
        (do
          (log/debugf "IsDefinedStmt - local var <%d> is not defined" source)
          (break state))))))

(defn make-IsUndefinedStmt [stmt-info]
  (log/debugf "making IsUndefinedStmt stmt")
  (let [source (get stmt-info "source")]
    (fn [state]
      (if-not (contains-local? state source)
        (do
          (log/debugf "IsUndefinedStmt - local var <%d> is not defined" source)
          state)
        (do
          (log/debugf "IsUndefinedStmt - local var <%d> is defined" source)
          (break state))))))

(defn make-MakeNumberRefStmt [stmt-info]
  (log/debugf "making MakeNumberRefStmt stmt")
  (let [index (get stmt-info "Index")                       ; NOTE: 'Index' is capitalized
        target (get stmt-info "target")]
    (fn [state]
      (let [val (edn/read-string (get-static-string state index))]
        (log/debugf "MakeNumberRefStmt - parsed number: %s" val)
        (set-local state target val)))))

(defn make-MakeArrayStmt [stmt-info]
  (log/debugf "making MakeArrayStmt stmt: %s" stmt-info)
  (let [target (get stmt-info "target")]
    (fn [state]
      (log/debugf "MakeArrayStmt - assigning empty array to local var %d" target)
      (set-local state target []))))

(defn make-MakeNullStmt [stmt-info]
  (log/debugf "making MakeNullStmt stmt: %s" stmt-info)
  (let [target (get stmt-info "target")]
    (fn [state]
      (log/debugf "MakeNullStmt - assigning 'null' to local var %d" target)
      (set-local state target nil))))

(defn make-MakeObjectStmt [stmt-info]
  (log/debugf "making MakeObjectStmt stmt: %s" stmt-info)
  (let [target (get stmt-info "target")]
    (fn [state]
      (log/debugf "MakeObjectStmt - assigning empty object to local var %d" target)
      (set-local state target {}))))

(defn make-MakeSetStmt [stmt-info]
  (log/debugf "making MakeSetStmt stmt: %s" stmt-info)
  (let [target (get stmt-info "target")]
    (fn [state]
      (log/debugf "MakeSetStmt - assigning empty set to local var %d" target)
      (set-local state target #{}))))

(defn make-NopStmt [stmt-info]
  (log/debugf "making NopStmt stmt: %s" stmt-info)
  (fn [state] state))

(defn make-NotEqualStmt [stmt-info]
  (log/debugf "making NotEqualStmt stmt")
  (let [a-index (get stmt-info "a")
        b-index (get stmt-info "b")]
    (fn [state]
      (let [a (get-value state a-index)
            b (get-value state b-index)
            result (not= a b)]
        (log/debugf "NotEqualStmt - '%s' != '%s' == %s" a b result)
        (if result
          state
          (break state))))))

(defn make-NotStmt [stmt-info]
  (log/debugf "making NotStmt stmt")
  (let [block (make-block (get stmt-info "block"))]
    (fn [state]
      (let [state (block state)
            break-index (get state :break-index)]
        (if-not (nil? break-index)
          (do
            (log/tracef "NotStmt - not defined; break-index: %d" break-index)
            (let [new-break-index (dec break-index)]
              (if (>= new-break-index 0)
                (break state new-break-index)
                (dissoc state :break-index))))
          (do
            (log/tracef "NotStmt - defined")
            (break state)))))))

(defn make-ObjectInsertOnceStmt [stmt-info]
  (log/debugf "making ObjectInsertOnceStmt stmt")
  (let [key-index (get stmt-info "key")
        value-index (get stmt-info "value")
        object-index (get stmt-info "object")]
    (fn [state]
      (let [object (get-local state object-index)
            key (get-value state key-index)
            value (get-value state value-index)]
        (if (nil? object)
          (do
            (log/debugf "ObjectInsertOnceStmt - <%s> not present" object-index)
            (break state))
          (let [old-value (get object key)]
            (if (and (not (nil? old-value)) (not= old-value value))
              (do
                (log/debugf "ObjectInsertOnceStmt - object <%s> already contains key <%s> with different value" object-index key-index)
                (break state))
              (do
                (log/debugf "ObjectInsertOnceStmt - inserting '%s' at <%s> to var <%d>" value key object-index)
                (set-local state object-index (assoc object key value))))))))))

(defn make-ObjectInsertStmt [stmt-info]
  (log/debugf "making ObjectInsertStmt stmt")
  (let [key-index (get stmt-info "key")
        value-index (get stmt-info "value")
        object-index (get stmt-info "object")]
    (fn [state]
      (let [object (get-local state object-index)
            key (get-value state key-index)
            value (get-value state value-index)]
        (if (nil? object)
          (do
            (log/debugf "ObjectInsertStmt - <%s> not present" object-index)
            (break state))
          (do
            (log/debugf "ObjectInsertStmt - inserting '%s' at <%s> to var <%d>" value key object-index)
            (set-local state object-index (assoc object key value))))))))

(defn make-ObjectMergeStmt [stmt-info]
  (log/debugf "making ObjectMergeStmt stmt")
  (fn [state]
    (let [to-key (get stmt-info "a")
          from-key (get stmt-info "b")
          target-key (get stmt-info "target")]
      (log/debugf "ObjectMergeStmt - merging %d and %d into %d" to-key from-key target-key)
      (set-local state target-key (merge (get-local state to-key) (get-local state from-key))))))

(defn make-ResetLocalStmt [stmt-info]
  (log/debugf "making ResetLocalStmt stmt")
  (let [target (get stmt-info "target")]
    (fn [state]
      (log/debugf "ResetLocalStmt - resetting %d" target)
      (dissoc-local state target))))

(defn make-ResultSetAddStmt [stmt-info]
  (log/debugf "making ResultSetAddStmt stmt")
  (let [value (get stmt-info "value")]
    (fn [state]
      (let [val (get-local state value)]
        (if (nil? val)
          (do
            (log/debugf "ResultSetAddStmt - nothing to add to result set")
            state)
          (do
            (log/debugf "ResultSetAddStmt - adding %s to resultset" val)
            (add-result state val)))))))

(defn make-ReturnLocalStmt [_]
  (log/debugf "making ReturnLocalStmt stmt")
  (fn [state]
    (log/debugf "ReturnLocalStmt - exiting function")        ; TODO: Do we need to recursively break out of all nested blocks to exit the function?
    state))                                                 ; No-op, the function itself knows what local var is the result

(defn make-SetAddStmt [stmt-info]
  (log/debugf "making SetAddStmt stmt")
  (let [set-index (get stmt-info "set")
        value-index (get stmt-info "value")]
    (fn [state]
      (let [set (get-local state set-index)]
        (if (nil? set)
          (do
            (log/debugf "SetAddStmt - <%s> is not a local var" set-index)
            (break state))
          (let [val (get-value state value-index)]
            (if (nil? val)
              (do
                (log/debugf "SetAddStmt - value <%s> not present" value-index)
                (break state))
              (do
                (log/debugf "SetAddStmt - Adding '%s' to <%s>" val set-index)
                (set-local state set-index (conj set val))))))))))

(defn make-ScanStmt [stmt-info]
  (log/debugf "making ScanStmt stmt")
  (let [source-index (get stmt-info "source")
        key-index (get stmt-info "key")
        value-index (get stmt-info "value")
        block-stmt (make-block (get stmt-info "block"))]
    (fn [state]
      (log/debugf "ScanStmt - scanning <%d>" source-index)
      (let [source (get-local state source-index)]
        (if (or (nil? source) (not (coll? source)) (empty? source))
          (do
            (log/debugf "ScanStmt - '%s' is not a collection" source-index)
            (break state))
          (if (map? source)
            (do
              (log/tracef "ScanStmt - source is map")
              (throw (Exception. (format "ScanStmt not implemented for maps"))))
            (do
              (log/tracef "ScanStmt - source is list")
              (loop [source-indexed (map-indexed (fn [i v] [i v]) source)
                     state state]
                (if (empty? source-indexed)
                  (do
                    (log/tracef "ScanStmt - done")
                    (break state))
                  (let [entry (first source-indexed)
                        key (get entry 0)
                        value (get entry 1)
                        state (set-local state key-index key)
                        state (set-local state value-index value)]
                    (recur (next source-indexed) (block-stmt state))))))))))))

(defn make-stmt [stmt-info]
  (log/debugf "making stmt" stmt-info)
  (let [type (get stmt-info "type")
        stmt-info (get stmt-info "stmt")
        stmt (case type
               "ArrayAppendStmt" (make-ArrayAppendStmt stmt-info)
               ;"AssignIntStmt"
               "AssignVarOnceStmt" (make-AssignVarOnceStmt stmt-info)
               "AssignVarStmt" (make-AssignVarStmt stmt-info)
               "BlockStmt" (make-BlockStmt stmt-info)
               "BreakStmt" (make-BreakStmt stmt-info)
               ;"CallDynamicStmt"
               "CallStmt" (make-CallStmt stmt-info)
               "DotStmt" (make-DotStmt stmt-info)
               "EqualStmt" (make-EqualStmt stmt-info)
               ;"IsArrayStmt"
               "IsDefinedStmt" (make-IsDefinedStmt stmt-info)
               ;"IsObjectStmt"
               "IsUndefinedStmt" (make-IsUndefinedStmt stmt-info)
               ;"LenStmt"
               "MakeArrayStmt" (make-MakeArrayStmt stmt-info)
               "MakeNullStmt" (make-MakeNullStmt stmt-info)
               ;"MakeNumberIntStmt"
               "MakeNumberRefStmt" (make-MakeNumberRefStmt stmt-info)
               "MakeObjectStmt" (make-MakeObjectStmt stmt-info)
               "MakeSetStmt" (make-MakeSetStmt stmt-info)
               "NopStmt" (make-NopStmt stmt-info)
               "NotEqualStmt" (make-NotEqualStmt stmt-info)
               "NotStmt" (make-NotStmt stmt-info)
               "ObjectInsertOnceStmt" (make-ObjectInsertOnceStmt stmt-info)
               "ObjectInsertStmt" (make-ObjectInsertStmt stmt-info)
               "ObjectMergeStmt" (make-ObjectMergeStmt stmt-info)
               "ResetLocalStmt" (make-ResetLocalStmt stmt-info)
               "ResultSetAddStmt" (make-ResultSetAddStmt stmt-info)
               "ReturnLocalStmt" (make-ReturnLocalStmt stmt-info)
               "ScanStmt" (make-ScanStmt stmt-info)
               "SetAddStmt" (make-SetAddStmt stmt-info)
               ;"WithStmt"
               (throw (Exception. (format "%s statement type not implemented" type))))]
    (fn [state]
      (log/tracef "%s - calling with info: %s, vars: %s" type stmt-info (get state :local))
      (stmt state))))

(defn make-stmts [stmts-info]
  (log/debugf "making stmts")
  (let [stmts (doall (for [stmt-info stmts-info]
                       (make-stmt stmt-info)))]
    (fn [state]
      (log/debugf "executing statements")
      (loop [stmts stmts
             state state]
        (let [stmts-count (count stmts)]
          (if (or (zero? stmts-count) (contains? state :break-index))
            (do
              (when (pos? stmts-count)
                (log/tracef "skipping %d statement(s)" stmts-count))
              state)
            (recur (next stmts) ((first stmts) state))))))))

(defn make-block [block-info]
  (log/debugf "making block")
  (let [stmts-info (get block-info "stmts")
        stmts (make-stmts stmts-info)]
    (fn [state]
      (log/debugf "executing block")
      (let [state (stmts state)
            break-index (get state :break-index)]
        (if-not (nil? break-index)
          (do
            (log/tracef "broke out of block; break-index: %d" break-index)
            (let [new-break-index (dec break-index)]
              (if (>= new-break-index 0)
                (break state new-break-index)
                (dissoc state :break-index))))
          state)))))

(defn make-blocks [blocks-info]
  (log/debugf "making blocks")
  (let [blocks (doall (for [block-info blocks-info]
                        (make-block block-info)))]
    (fn [state]
      (log/debugf "executing blocks")
      (loop [blocks blocks
             state state]
        (if (or (contains? state :break-index) (empty? blocks))
          (do
            (let [block-count (count blocks)]
              (when (pos? block-count)
                (log/tracef "skipping %d block(s); index: %d" block-count)))
            state)
          (recur (next blocks) ((first blocks) state)))))))

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
    (log/debugf "making plan '%s'" name)
    (log/tracef "plan: %s" plan-info)
    (let [blocks (make-blocks blocks-info)]
      [name (fn [info input]
              (let [state (assoc info :local {0 input
                                              1 (populate-data plan-info)})]
                (log/debugf "Plan - executing '%s'" name)
                (let [state (blocks state)
                      result-set (get state :result-set)]
                  (log/debugf "Plan - result-set: %s" result-set)
                  result-set)))])))

(defn make-plans [plans-info]
  (log/debugf "making plans")
  (into-array (doall (for [plan-info (get plans-info "plans")]
                       (make-plan plan-info)))))

(defn make-func [func-info]
  (let [name (get func-info "name")
        return-index (get func-info "return")
        blocks-info (get func-info "blocks")
        blocks (make-blocks blocks-info)]
    (log/debugf "making func <%s>" name)
    [name (fn [state]
            (log/debugf "executing func <%s>" name)
            (let [state (blocks state)]
              (if (contains-local? state return-index)
                (let [result (get-local state return-index)]
                  (log/debugf "function <%s> returning '%s'" name result)
                  {:result result})
                (do
                  (log/debugf "function <%s> undefined" name)
                  {}))))]))

(defn make-funcs [funcs-info]
  (log/debugf "making funcs")
  (loop [func-infos (get funcs-info "funcs")
         func-map {}]
    (if (empty? func-infos)
      func-map
      (let [[name func] (make-func (first func-infos))]
        (recur (next func-infos) (assoc func-map name func))))))

(defn make-builtin-func [func-info]
  (let [name (get func-info "name")
        builtin-func (builtins/get-builtin name)]
    (log/debugf "making built-in func <%s>" name)
    (if (nil? builtin-func)
      (throw (Exception. (format "unknown function '%s'" name)))
      [name (fn [state]
              (let [args (get state :local)]
                (log/debugf "executing built-in func <%s> with args: %s" name, args)
                (try
                  (let [result (builtin-func args)]
                    (log/debugf "built-in function <%s> returning '%s'" name result)
                    {:result result})
                  (catch BuiltinException e
                    (log/debugf "function <%s> returned undefined value: %s" name (.getMessage e))
                    {}))))])))

(defn make-builtin-funcs [builtin-funcs-info]
  (log/debugf "making built-in funcs")
  (loop [func-infos builtin-funcs-info
         func-map {}]
    (if (empty? func-infos)
      func-map
      (let [[name func] (make-builtin-func (first func-infos))]
        (recur (next func-infos) (assoc func-map name func))))))

(defn parse
  "Parses the incoming string"
  [str] (let [ir (json/read-str str)
              static (get ir "static")
              plans-info (get ir "plans")
              funcs-info (get ir "funcs")
              builtin-funcs-info (get (get ir "static") "builtin_funcs")]
          {:plans         (make-plans plans-info)
           :funcs         (make-funcs funcs-info)
           :builtin-funcs (make-builtin-funcs builtin-funcs-info)
           :static        static}))

(defn parse-file
  "Reads and parses the incoming file"
  ([file]
   (log/infof "Parsing file '%s'" file)
   (parse (slurp file))))
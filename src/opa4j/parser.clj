(ns opa4j.parser
  (:require [clojure.data.json :as json])
  (:import (java.time Instant)))

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

(defn get-local [data-map index]
  (get (get data-map :local) index))

(defn set-local [data-map index value]
  (let [local (get data-map :local)]
    (assoc data-map :local (assoc local index value))))

(defn get-data [data-map key]
  (let [static (get data-map :static)
        key-type (get key "type")
        key-value (get key "value")]
    (case key-type
      "local" (get-local data-map key-value)
      "string_index" (let [strings (get static "strings")]
                       (get (get strings key-value) "value"))
      (throw (Exception. (format "unknown value type ''" key-type))))))

(defn make-AssignVarStmt [stmt-info]
  "TODO"
  (log-debug "making AssignVarStmt stmt")
  (fn [data]
    (log-debug "executing AssignVarStmt statement")
    data))

(defn make-AssignVarOnceStmt [stmt-info]
  "TODO"
  (log-debug "making AssignVarOnceStmt stmt")
  (fn [data]
    (log-debug "executing AssignVarOnceStmt statement")
    data))

(defn make-BlockStmt [stmt-info]
  (log-debug "making BlockStmt stmt")
  (log-trace "info: %s" stmt-info)
  (let [blocks (make-blocks (get stmt-info "blocks"))]
    (fn [data]
      (log-trace "BlockStmt - local vars: %s" (get data :local))
      (blocks data))))

(defn make-BreakStmt [stmt-info]
  "TODO"
  (log-debug "making BreakStmt stmt")
  (fn [data]
    (log-debug "executing BreakStmt statement")
    data))

(defn map-by-index [array]
  (loop [array array
         map {}
         i 0]
    (if (empty? array)
      map
      (recur (next array) (assoc map i (first array)) (+ i 1)))))

(defn make-CallStmt [stmt-info]
  (log-debug "making CallStmt stmt")
  (let [target (get stmt-info "result")]
    (let [func-name (get stmt-info "func")]
      (fn [data]
        (log-debug "CallStmt - calling func '%s'" func-name)
        (let [func (get (get data :funcs) func-name)]
          (let [local (map-by-index (map (fn [arg] (get-data data arg)) (get stmt-info "args")))]
            (let [func-data {:static (get data :static)
                             :funcs  (get data :funcs)
                             :local  local}]
              (let [result (func func-data)]
                (log-debug "CallStmt - returning: %s" result)
                (set-local data target result)))))))))

(defn make-DotStmt [stmt-info]
  (let [source-index (get stmt-info "source")
        key-index (get stmt-info "key")
        target (get stmt-info "target")]
    (log-debug "making DotStmt stmt")
    (fn [data]
      (let [source (get-data data source-index)
            key (get-data data key-index)]
        (log-trace "DotStmt - getting '%s' from '%s' to var %s" key source target)
        (let [val (get (get-data data source-index) key)]
          (log-debug "DotStmt - got '%s' from ('%s' in '%s') to var %s" val key source target)
          (set-local data target val))))))

(defn make-IsDefinedStmt [stmt-info]
  "TODO"
  (log-debug "making IsDefinedStmt stmt")
  (fn [data]
    (log-debug "executing IsDefinedStmt statement")
    data))

(defn make-MakeNumberRefStmt [stmt-info]
  "TODO"
  (log-debug "making MakeNumberRefStmt stmt")
  (fn [data]
    (log-debug "executing MakeNumberRefStmt statement")
    data))

(defn make-MakeObjectStmt [stmt-info]
  (log-debug "making MakeObjectStmt stmt: %s" stmt-info)
  (let [target (get stmt-info "target")]
    (fn [data]
      (log-trace "MakeObjectStmt - info: %s" stmt-info)
      (log-debug "MakeObjectStmt - assigning empty object to local var %d" target)
      (set-local data target {}))))

(defn make-NotEqualStmt [stmt-info]
  "TODO"
  (log-debug "making NotEqualStmt stmt")
  (fn [data]
    (log-debug "executing NotEqualStmt statement")
    data))

(defn make-ObjectInsertStmt [stmt-info]
  (log-debug "making ObjectInsertStmt stmt")
  (let [key-index (get stmt-info "key")
        value-index (get stmt-info "value")
        object-index (get stmt-info "object")]
    (fn [data]
      (log-trace "ObjectInsertStmt - info: %s" stmt-info)
      (let [object (get-local data object-index)
            key (get-data data key-index)
            value (get-data data value-index)]
        (log-debug "ObjectInsertStmt - inserting '%s' at '%s' to var %d" value key object-index)
        (set-local data object-index (assoc object key value))))))

(defn make-ObjectMergeStmt [stmt-info]
  (log-debug "making ObjectMergeStmt stmt")
  (fn [data]
    (let [to-key (get stmt-info "a")
          from-key (get stmt-info "b")
          target-key (get stmt-info "target")]
      (log-debug "ObjectMergeStmt - merging %d and %d into %d" to-key from-key target-key)
      (set-local data target-key (merge (get-local data to-key) (get-local data from-key))))))

(defn make-ResetLocalStmt [stmt-info]
  "TODO"
  (log-debug "making ResetLocalStmt stmt")
  (fn [data]
    (log-debug "executing ResetLocalStmt statement")
    data))

(defn make-ResultSetAddStmt [stmt-info]
  "TODO"
  (log-debug "making ResultSetAddStmt stmt")
  (fn [data]
    (log-debug "executing ResultSetAddStmt statement")
    data))

(defn make-ReturnLocalStmt [stmt-info]
  "TODO"
  (log-debug "making ReturnLocalStmt stmt")
  (fn [data]
    (log-debug "executing ReturnLocalStmt statement")
    data))

(defn make-stmt [stmt-info]
  (log-debug "making stmt" stmt-info)
  (let [type (get stmt-info "type")
        stmt-info (get stmt-info "stmt")]
    (let [stmt (case type
                 "AssignVarStmt" (make-AssignVarStmt stmt-info)
                 "AssignVarOnceStmt" (make-AssignVarOnceStmt stmt-info)
                 "BlockStmt" (make-BlockStmt stmt-info)
                 "BreakStmt" (make-BreakStmt stmt-info)
                 "CallStmt" (make-CallStmt stmt-info)
                 "DotStmt" (make-DotStmt stmt-info)
                 "IsDefinedStmt" (make-IsDefinedStmt stmt-info)
                 "MakeNumberRefStmt" (make-MakeNumberRefStmt stmt-info)
                 "MakeObjectStmt" (make-MakeObjectStmt stmt-info)
                 "NotEqualStmt" (make-NotEqualStmt stmt-info)
                 "ObjectInsertStmt" (make-ObjectInsertStmt stmt-info)
                 "ObjectMergeStmt" (make-ObjectMergeStmt stmt-info)
                 "ResetLocalStmt" (make-ResetLocalStmt stmt-info)
                 "ResultSetAddStmt" (make-ResultSetAddStmt stmt-info)
                 "ReturnLocalStmt" (make-ReturnLocalStmt stmt-info)
                 (throw (Exception. (format "%s statement type not implemented" type))))]
      (fn [data]
        (log-trace "%s - statement called with local vars: %s" type (get data :local))
        (stmt data)))))

(defn make-stmts [stmts-info]
  (log-debug "making stmts")
  (let [stmts (doall (for [stmt-info stmts-info]
                       (make-stmt stmt-info)))]
    (fn [data]
      (log-debug "executing statements")
      (loop [stmts stmts
             data data]
        (if (== (count stmts) 0)
          data
          (recur (next stmts) ((first stmts) data)))))))

(defn make-block [block-info]
  (log-debug "making block")
  (let [stmts-info (get block-info "stmts")]
    (let [stmts (make-stmts stmts-info)]
      (fn [data]
        (log-debug "executing block")
        (stmts data)))))

(defn make-blocks [blocks-info]
  (log-debug "making blocks")
  (let [blocks (doall (for [block-info blocks-info]
                        (make-block block-info)))]
    (fn [data]
      (log-debug "executing blocks")
      (loop [blocks blocks
             vars data]
        (if (== (count blocks) 0)
          vars
          (recur (next blocks) ((first blocks) vars)))))))

(defn make-plan [plan-info]
  (let [name (get plan-info "name")
        blocks-info (get plan-info "blocks")]
    (log-debug "making plan '%s'" name)
    (log-trace "plan: %s" plan-info)
    (let [blocks (make-blocks blocks-info)]
      [name (fn [data]
              (let [data (assoc data :local {0 {} 1 { "simple" {}}})]
                (log-debug "executing plan '%s'" name)
                (blocks data)))])))

(defn make-plans [plans-info]
  (log-debug "making plans")
  (doall (for [plan-info (get plans-info "plans")]
           (make-plan plan-info))))

(defn make-func [func-info]
  (let [name (get func-info "name")
        params (get func-info "params")
        return (get func-info "return")
        blocks-info (get func-info "blocks")]
    (let [blocks (make-blocks blocks-info)]
      [name (fn [data]
              (log-debug "executing func '%s'" name)
              (blocks data)
              42)])))

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
  [str] (let [ir (json/read-str str)]
          (let [static (get ir "static")
                plans-info (get ir "plans")
                funcs-info (get ir "funcs")]
            {:plans  (make-plans plans-info)
             :funcs  (make-funcs funcs-info)
             :static static})))

(defn parse-file
  "Reads and parses the incoming file"
  ([] (println "No file provided!"))
  ([file]
   (log-debug "Parsing file '%s'" file)
   (parse (slurp file))))
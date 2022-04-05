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

(defn make-AssignVarStmt [stmt-info static]
  "TODO"
  (log-debug "making AssignVarStmt stmt")
  (fn [vars] (log-debug "executing AssignVarStmt statement")))

(defn make-BlockStmt [stmt-info static]
  (log-debug "making BlockStmt stmt")
  (let [blocks (make-blocks (get (get stmt-info "stmt") "blocks") static)]
    (fn [vars]
      (log-debug "executing BlockStmt statement")
      (blocks vars))))

(defn make-BreakStmt [stmt-info static]
  "TODO"
  (log-debug "making BreakStmt stmt")
  (fn [vars] (log-debug "executing BreakStmt statement")))

(defn make-CallStmt [stmt-info static]
  "TODO"
  (log-debug "making CallStmt stmt")
  (fn [vars] (log-debug "executing CallStmt statement")))

(defn make-DotStmt [stmt-info static]
  "TODO"
  (log-debug "making DotStmt stmt")
  (fn [vars] (log-debug "executing DotStmt statement")))

(defn make-MakeObjectStmt [stmt-info static]
  "TODO"
  (log-debug "making MakeObjectStmt stmt")
  (fn [vars]
    (log-debug "executing MakeObjectStmt statement")
    ;(throw (Exception. "AssignVarStmt statement not implemented"))
    ))

(defn make-ObjectInsertStmt [stmt-info static]
  "TODO"
  (log-debug "making ObjectInsertStmt stmt")
  (fn [vars] (log-debug "executing ObjectInsertStmt statement")))

(defn make-ObjectMergeStmt [stmt-info static]
  "TODO"
  (log-debug "making ObjectMergeStmt stmt")
  (fn [vars] (log-debug "executing ObjectMergeStmt statement")))

(defn make-ResultSetAddStmt [stmt-info static]
  "TODO"
  (log-debug "making ResultSetAddStmt stmt")
  (fn [vars] (log-debug "executing ResultSetAddStmt statement")))

(defn make-stmt [stmt-info static]
  (log-debug "making stmt" stmt-info)
  (let [type (get stmt-info "type")]
    (case type
      "AssignVarStmt" (make-AssignVarStmt stmt-info static)
      "BlockStmt" (make-BlockStmt stmt-info static)
      "BreakStmt" (make-BreakStmt stmt-info static)
      "CallStmt" (make-CallStmt stmt-info static)
      "DotStmt" (make-DotStmt stmt-info static)
      "MakeObjectStmt" (make-MakeObjectStmt stmt-info static)
      "ObjectInsertStmt" (make-ObjectInsertStmt stmt-info static)
      "ObjectMergeStmt" (make-ObjectMergeStmt stmt-info static)
      "ResultSetAddStmt" (make-ResultSetAddStmt stmt-info static)
      (throw (Exception. (format "%s statement type not implemented" type))))))

(defn make-stmts [stmts-info static]
  (log-debug "making stmts")
  (let [stmts (doall (for [stmt-info stmts-info]
                       (make-stmt stmt-info static)))]
    (fn [vars]
      (log-debug "executing statements")
      (doseq [stmt stmts] (stmt vars)))))

(defn make-block [block-info static]
  (log-debug "making block")
  (let [stmts-info (get block-info "stmts")]
    (let [stmts (make-stmts stmts-info static)]
      (fn [vars]
        (log-debug "executing block")
        (stmts vars)))))

(defn make-blocks [blocks-info static]
  (log-debug "making blocks")
  (let [blocks (doall (for [block-info blocks-info]
                        (make-block block-info static)))]
    (fn [vars]
      (log-debug "executing blocks")
      (doseq [block blocks] (block vars)))))

(defn make-plan [plan-info static]
  (let [name (get plan-info "name") blocks-info (get plan-info "blocks")]
    (log-debug "making plan '%s'" name)
    (log-trace "static: %s" static)
    (log-trace "plan: %s" plan-info)
    (let [blocks (make-blocks blocks-info static)]
      [name (fn [vars]
        (log-debug "executing plan '%s'" name)
        (blocks vars))])))

(defn make-plans [plans-info static]
  (log-debug "making plans")
  (let [plans (doall (for [plan-info plans-info]
                       (make-plan plan-info static)))]
    plans))

(defn parse
  "Parses the incoming string"
  [str] (let [ir (json/read-str str)]
          (let [static (get ir "static") plans (get (get ir "plans") "plans")]
            (make-plans plans static))))

(defn parse-file
  "Reads and parses the incoming file"
  ([] (println "No file provided!"))
  ([file]
   (log-debug "Parsing file '%s'" file)
   (parse (slurp file))))
(ns opa4j.parser
  (:require [clojure.data.json :as json]))

(declare make-blocks)

(defn log
  ([msg] (println msg))
  ([msg & args] (println msg args)))

(defn make-AssignVarStmt [stmt-data static]
  "TODO"
  (log "making AssignVarStmt stmt")
  (fn [vars] (log "executing AssignVarStmt statement")))

(defn make-BlockStmt [stmt-data static]
  (log "making BlockStmt stmt")
  (let [blocks (make-blocks (get (get stmt-data "stmt") "blocks") static)]
    (fn [vars]
      (log "executing BlockStmt statement")
      (blocks vars))))

(defn make-BreakStmt [stmt-data static]
  "TODO"
  (log "making BreakStmt stmt")
  (fn [vars] (log "executing BreakStmt statement")))

(defn make-CallStmt [stmt-data static]
  "TODO"
  (log "making CallStmt stmt")
  (fn [vars] (log "executing CallStmt statement")))

(defn make-DotStmt [stmt-data static]
  "TODO"
  (log "making DotStmt stmt")
  (fn [vars] (log "executing DotStmt statement")))

(defn make-MakeObjectStmt [stmt-data static]
  "TODO"
  (log "making MakeObjectStmt stmt")
  (fn [vars]
    (log "executing MakeObjectStmt statement")
    ;(throw (Exception. "AssignVarStmt statement not implemented"))
    ))

(defn make-ObjectInsertStmt [stmt-data static]
  "TODO"
  (log "making ObjectInsertStmt stmt")
  (fn [vars] (log "executing ObjectInsertStmt statement")))

(defn make-ObjectMergeStmt [stmt-data static]
  "TODO"
  (log "making ObjectMergeStmt stmt")
  (fn [vars] (log "executing ObjectMergeStmt statement")))

(defn make-ResultSetAddStmt [stmt-data static]
  "TODO"
  (log "making ResultSetAddStmt stmt")
  (fn [vars] (log "executing ResultSetAddStmt statement")))

(defn make-stmt [stmt-data static]
  (log "making stmt" stmt-data)
  (let [type (get stmt-data "type")]
    (case type
      "AssignVarStmt" (make-AssignVarStmt stmt-data static)
      "BlockStmt" (make-BlockStmt stmt-data static)
      "BreakStmt" (make-BreakStmt stmt-data static)
      "CallStmt" (make-CallStmt stmt-data static)
      "DotStmt" (make-DotStmt stmt-data static)
      "MakeObjectStmt" (make-MakeObjectStmt stmt-data static)
      "ObjectInsertStmt" (make-ObjectInsertStmt stmt-data static)
      "ObjectMergeStmt" (make-ObjectMergeStmt stmt-data static)
      "ResultSetAddStmt" (make-ResultSetAddStmt stmt-data static)
      (throw (Exception. (format "%s statement type not implemented" type))))))

(defn make-stmts [stmts-data static]
  (log "making stmts")
  (let [stmts (doall (for [stmt-data stmts-data]
                       (make-stmt stmt-data static)))]
    (fn [vars]
      (log "executing statements")
      (doseq [stmt stmts] (stmt vars)))))

(defn make-block [block-data static]
  (log "making block")
  (let [stmts-data (get block-data "stmts")]
    (let [stmts (make-stmts stmts-data static)]
      (fn [vars]
        (log "executing block")
        (stmts vars)))))

(defn make-blocks [blocks-data static]
  (log "making blocks")
  (let [blocks (doall (for [block-data blocks-data]
                        (make-block block-data static)))]
    (fn [vars]
      (log "executing blocks")
      (doseq [block blocks] (block vars)))))

(defn make-plan [plan-data static]
  (let [name (get plan-data "name") blocks-data (get plan-data "blocks")]
    (log "making plan" name)
    (log "static:" static)
    (log "plan:" plan-data)
    (let [blocks (make-blocks blocks-data static)]
      (fn [vars]
        (log "executing plan" name)
        (blocks vars)))))

(defn parse
  "Parses the incoming string"
  [str] (let [ir (json/read-str str)]
          (let [static (get ir "static") plans (get (get ir "plans") "plans")]
            (make-plan (first plans) static))))

(defn parse-file
  "Reads and parses the incoming file"
  ([] (println "No file provided!"))
  ([file]
   (log "Parsing file" file)
   (parse (slurp file))))
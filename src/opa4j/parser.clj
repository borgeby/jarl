(ns opa4j.parser
  (:require [clojure.data.json :as json]))

(declare make-blocks)

(defn log
  ([msg] (println msg))
  ([msg & args] (println msg args)))

(defn make-AssignVarStmt [stmt-info static]
  "TODO"
  (log "making AssignVarStmt stmt")
  (fn [vars] (log "executing AssignVarStmt statement")))

(defn make-BlockStmt [stmt-info static]
  (log "making BlockStmt stmt")
  (let [blocks (make-blocks (get (get stmt-info "stmt") "blocks") static)]
    (fn [vars]
      (log "executing BlockStmt statement")
      (blocks vars))))

(defn make-BreakStmt [stmt-info static]
  "TODO"
  (log "making BreakStmt stmt")
  (fn [vars] (log "executing BreakStmt statement")))

(defn make-CallStmt [stmt-info static]
  "TODO"
  (log "making CallStmt stmt")
  (fn [vars] (log "executing CallStmt statement")))

(defn make-DotStmt [stmt-info static]
  "TODO"
  (log "making DotStmt stmt")
  (fn [vars] (log "executing DotStmt statement")))

(defn make-MakeObjectStmt [stmt-info static]
  "TODO"
  (log "making MakeObjectStmt stmt")
  (fn [vars]
    (log "executing MakeObjectStmt statement")
    ;(throw (Exception. "AssignVarStmt statement not implemented"))
    ))

(defn make-ObjectInsertStmt [stmt-info static]
  "TODO"
  (log "making ObjectInsertStmt stmt")
  (fn [vars] (log "executing ObjectInsertStmt statement")))

(defn make-ObjectMergeStmt [stmt-info static]
  "TODO"
  (log "making ObjectMergeStmt stmt")
  (fn [vars] (log "executing ObjectMergeStmt statement")))

(defn make-ResultSetAddStmt [stmt-info static]
  "TODO"
  (log "making ResultSetAddStmt stmt")
  (fn [vars] (log "executing ResultSetAddStmt statement")))

(defn make-stmt [stmt-info static]
  (log "making stmt" stmt-info)
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
  (log "making stmts")
  (let [stmts (doall (for [stmt-info stmts-info]
                       (make-stmt stmt-info static)))]
    (fn [vars]
      (log "executing statements")
      (doseq [stmt stmts] (stmt vars)))))

(defn make-block [block-info static]
  (log "making block")
  (let [stmts-info (get block-info "stmts")]
    (let [stmts (make-stmts stmts-info static)]
      (fn [vars]
        (log "executing block")
        (stmts vars)))))

(defn make-blocks [blocks-info static]
  (log "making blocks")
  (let [blocks (doall (for [block-info blocks-info]
                        (make-block block-info static)))]
    (fn [vars]
      (log "executing blocks")
      (doseq [block blocks] (block vars)))))

(defn make-plan [plan-info static]
  (let [name (get plan-info "name") blocks-info (get plan-info "blocks")]
    (log "making plan" name)
    (log "static:" static)
    (log "plan:" plan-info)
    (let [blocks (make-blocks blocks-info static)]
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
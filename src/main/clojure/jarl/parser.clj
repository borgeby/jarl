(ns jarl.parser
  (:require [clojure.data.json :as json])
  (:require [clojure.tools.logging :as log])
  (:require [clojure.string :as str])
  (:require [jarl.builtins.registry :as builtins])
  (:require [jarl.eval :as eval]))

(declare make-block)
(declare make-blocks)

(defn make-ArrayAppendStmt [stmt-info]
  (log/debug "making ArrayAppendStmt stmt")
  (let [array-index (get stmt-info "array")
        value-index (get stmt-info "value")]
    (fn [state]
      (eval/eval-ArrayAppendStmt array-index value-index state))))

(defn make-AssignVarStmt [stmt-info]
  (log/debug "making AssignVarStmt stmt")
  (let [source-index (get stmt-info "source")
        target (get stmt-info "target")]
    (fn [state]
      (eval/eval-AssignVarStmt source-index target state))))

(defn make-AssignVarOnceStmt [stmt-info]
  (log/debug "making AssignVarOnceStmt stmt")
  (let [source-index (get stmt-info "source")
        target (get stmt-info "target")]
    (fn [state]
      (eval/eval-AssignVarOnceStmt source-index target state))))

(defn make-BlockStmt [stmt-info]
  (log/debug "making BlockStmt stmt")
  (log/tracef "info: %s" stmt-info)
  (let [blocks (make-blocks (get stmt-info "blocks"))]
    (fn [state]
      (eval/eval-BlockStmt blocks state))))

(defn make-BreakStmt [stmt-info]
  (log/debug "making BreakStmt stmt")
  (let [index (get stmt-info "index")]
    (fn [state]
      (eval/eval-BreakStmt index state))))

(defn make-CallStmt [stmt-info]
  (log/debug "making CallStmt stmt")
  (let [result (get stmt-info "result")
        func-name (get stmt-info "func")
        args (get stmt-info "args")]
    (fn [state]
      (eval/eval-CallStmt result func-name args state))))

(defn make-DotStmt [stmt-info]
  (let [source-index (get stmt-info "source")
        key-index (get stmt-info "key")
        target (get stmt-info "target")]
    (log/debug "making DotStmt stmt")
    (fn [state]
      (eval/eval-DotStmt source-index key-index target state))))

(defn make-EqualStmt [stmt-info]
  (log/debug "making EqualStmt stmt")
  (let [a-index (get stmt-info "a")
        b-index (get stmt-info "b")]
    (fn [state]
      (eval/eval-EqualStmt a-index b-index state))))

(defn make-IsDefinedStmt [stmt-info]
  (log/debug "making IsDefinedStmt stmt")
  (let [source (get stmt-info "source")]
    (fn [state]
      (eval/eval-IsDefinedStmt source state))))

(defn make-IsUndefinedStmt [stmt-info]
  (log/debug "making IsUndefinedStmt stmt")
  (let [source (get stmt-info "source")]
    (fn [state]
      (eval/eval-IsUndefinedStmt source state))))

(defn make-MakeNumberRefStmt [stmt-info]
  (log/debug "making MakeNumberRefStmt stmt")
  (let [index (get stmt-info "Index")                       ; NOTE: 'Index' is capitalized
        target (get stmt-info "target")]
    (fn [state]
      (eval/eval-MakeNumberRefStmt index target state))))

(defn make-MakeArrayStmt [stmt-info]
  (log/debugf "making MakeArrayStmt stmt: %s" stmt-info)
  (let [target (get stmt-info "target")]
    (fn [state]
      (eval/eval-MakeArrayStmt target state))))

(defn make-MakeNullStmt [stmt-info]
  (log/debugf "making MakeNullStmt stmt: %s" stmt-info)
  (let [target (get stmt-info "target")]
    (fn [state]
      (eval/eval-MakeNullStmt target state))))

(defn make-MakeObjectStmt [stmt-info]
  (log/debugf "making MakeObjectStmt stmt: %s" stmt-info)
  (let [target (get stmt-info "target")]
    (fn [state]
      (eval/eval-MakeObjectStmt target state))))

(defn make-MakeSetStmt [stmt-info]
  (log/debugf "making MakeSetStmt stmt: %s" stmt-info)
  (let [target (get stmt-info "target")]
    (fn [state]
      (eval/eval-MakeSetStmt target state))))

(defn make-NopStmt [stmt-info]
  (log/debugf "making NopStmt stmt: %s" stmt-info)
  (fn [state]
    (eval/eval-NopStmt state)))

(defn make-NotEqualStmt [stmt-info]
  (log/debug "making NotEqualStmt stmt")
  (let [a-index (get stmt-info "a")
        b-index (get stmt-info "b")]
    (fn [state]
      (eval/eval-NotEqualStmt a-index b-index state))))

(defn make-NotStmt [stmt-info]
  (log/debug "making NotStmt stmt")
  (let [block (make-block (get stmt-info "block"))]
    (fn [state]
      (eval/eval-NotStmt block state))))

(defn make-ObjectInsertOnceStmt [stmt-info]
  (log/debug "making ObjectInsertOnceStmt stmt")
  (let [key-index (get stmt-info "key")
        value-index (get stmt-info "value")
        object-index (get stmt-info "object")]
    (fn [state]
      (eval/eval-ObjectInsertOnceStmt key-index value-index object-index state))))

(defn make-ObjectInsertStmt [stmt-info]
  (log/debug "making ObjectInsertStmt stmt")
  (let [key-index (get stmt-info "key")
        value-index (get stmt-info "value")
        object-index (get stmt-info "object")]
    (fn [state]
      (eval/eval-ObjectInsertStmt key-index value-index object-index state))))

(defn make-ObjectMergeStmt [stmt-info]
  (log/debug "making ObjectMergeStmt stmt")
  (let [to-key (get stmt-info "a")
        from-key (get stmt-info "b")
        target-key (get stmt-info "target")]
    (fn [state]
      (eval/eval-ObjectMergeStmt to-key from-key target-key state))))

(defn make-ResetLocalStmt [stmt-info]
  (log/debug "making ResetLocalStmt stmt")
  (let [target (get stmt-info "target")]
    (fn [state]
      (eval/eval-ResetLocalStmt target state))))

(defn make-ResultSetAddStmt [stmt-info]
  (log/debug "making ResultSetAddStmt stmt")
  (let [value (get stmt-info "value")]
    (fn [state]
      (eval/eval-ResultSetAddStmt value state))))

(defn make-ReturnLocalStmt [_]
  (log/debug "making ReturnLocalStmt stmt")
  (fn [state]
    (eval/eval-ReturnLocalStmt state)))

(defn make-SetAddStmt [stmt-info]
  (log/debug "making SetAddStmt stmt")
  (let [set-index (get stmt-info "set")
        value-index (get stmt-info "value")]
    (fn [state]
      (eval/eval-SetAddStmt set-index value-index state))))

(defn make-ScanStmt [stmt-info]
  (log/debug "making ScanStmt stmt")
  (let [source-index (get stmt-info "source")
        key-index (get stmt-info "key")
        value-index (get stmt-info "value")
        block-stmt (make-block (get stmt-info "block"))]
    (fn [state]
      (eval/eval-ScanStmt source-index key-index value-index block-stmt state))))

(defn make-stmt [stmt-info]
  (log/debugf "making stmt: %s" stmt-info)
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
      (eval/eval-stmt type stmt state))))

(defn make-stmts [stmts-info]
  (log/debug "making stmts")
  (let [stmts (doall (for [stmt-info stmts-info]
                       (make-stmt stmt-info)))]
    (fn [state]
      (eval/eval-stmts stmts state))))

(defn make-block [block-info]
  (log/debug "making block")
  (let [stmts-info (get block-info "stmts")
        stmts (make-stmts stmts-info)]
    (fn [state]
      (eval/eval-block stmts state))))

(defn make-blocks [blocks-info]
  (log/debug "making blocks")
  (let [blocks (doall (for [block-info blocks-info]
                        (make-block block-info)))]
    (fn [state]
      (eval/eval-blocks blocks state))))

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
  (log/debug "making plans")
  (into-array (doall (for [plan-info (get plans-info "plans")]
                       (make-plan plan-info)))))

(defn make-func [func-info]
  (let [name (get func-info "name")
        return-index (get func-info "return")
        blocks-info (get func-info "blocks")
        blocks (make-blocks blocks-info)]
    (log/debugf "making func <%s>" name)
    [name (fn [state]
            (eval/eval-func name return-index blocks state))]))

(defn make-funcs [funcs-info]
  (log/debug "making funcs")
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
              (eval/eval-builtin-func name builtin-func state))])))

(defn make-builtin-funcs [builtin-funcs-info]
  (log/debug "making built-in funcs")
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

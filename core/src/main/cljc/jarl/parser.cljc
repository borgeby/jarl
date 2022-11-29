(ns jarl.parser
  (:require [jarl.builtins.registry :as builtins]
            [jarl.encoding.json :as json]
            [jarl.eval :as eval]
            [jarl.formatting :refer [sprintf]]
            [jarl.logging :as log]
            [jarl.state :as state]))

(declare make-block)
(declare make-blocks)
(declare make-stmts)

(defn make-ArrayAppendStmt [{:strs [array value]}]
  (log/debug "making ArrayAppendStmt stmt")
  (fn [state]
    (eval/eval-ArrayAppendStmt array value state)))

(defn make-AssignIntStmt [{:strs [value target]}]
  (log/debug "making AssignIntStmt stmt")
  (fn [state]
    (eval/eval-AssignIntStmt value target state)))

(defn make-AssignVarStmt [{:strs [target source]}]
  (log/debug "making AssignVarStmt stmt")
  (fn [state]
    (eval/eval-AssignVarStmt source target state)))

(defn make-AssignVarOnceStmt [{:strs [target source]}]
  (log/debug "making AssignVarOnceStmt stmt")
  (fn [state]
    (eval/eval-AssignVarOnceStmt source target state)))

(defn make-BlockStmt [{:strs [blocks] :as stmt-info}]
  (log/debug "making BlockStmt stmt")
  (log/tracef "info: %s" stmt-info)
  (let [blocks (make-blocks blocks)]
    (fn [state]
      (eval/eval-BlockStmt blocks state))))

(defn make-BreakStmt [{:strs [index]}]
  (log/debug "making BreakStmt stmt")
  (fn [state]
    (eval/eval-BreakStmt index state)))

(defn make-CallDynamicStmt [{:strs [result path args file row col]}]
  (log/debug "making CallDynamicStmt stmt")
  (fn [state]
    (eval/eval-CallDynamicStmt result path args file row col state)))

(defn make-CallStmt [{:strs [result func args file row col]}]
  (log/debug "making CallStmt stmt")
  (fn [state]
    (eval/eval-CallStmt result func args file row col state)))

(defn make-DotStmt [{:strs [target source key]}]
  (log/debug "making DotStmt stmt")
  (fn [state]
    (eval/eval-DotStmt source key target state)))

(defn make-EqualStmt [{:strs [a b]}]
  (log/debug "making EqualStmt stmt")
  (fn [state]
    (eval/eval-EqualStmt a b state)))

(defn make-IsArrayStmt [{:strs [source]}]
  (log/debug "making IsArrayStmt stmt")
  (fn [state]
    (eval/eval-IsArrayStmt source state)))

(defn make-IsDefinedStmt [{:strs [source]}]
  (log/debug "making IsDefinedStmt stmt")
  (fn [state]
    (eval/eval-IsDefinedStmt source state)))

(defn make-IsObjectStmt [{:strs [source]}]
  (log/debug "making IsObjectStmt stmt")
  (fn [state]
    (eval/eval-IsObjectStmt source state)))

(defn make-IsUndefinedStmt [{:strs [source]}]
  (log/debug "making IsUndefinedStmt stmt")
  (fn [state]
    (eval/eval-IsUndefinedStmt source state)))

(defn make-LenStmt [{:strs [source target]}]
  (log/debug "making LenStmt stmt")
  (fn [state]
    (eval/eval-LenStmt source target state)))

(defn make-MakeNumberRefStmt [{:strs [target] index "Index"}] ; NOTE: 'Index' is capitalized
  (log/debug "making MakeNumberRefStmt stmt")
  (fn [state]
    (eval/eval-MakeNumberRefStmt index target state)))

(defn make-MakeArrayStmt [{:strs [target] :as stmt-info}]
  (log/debugf "making MakeArrayStmt stmt: %s" stmt-info)
  (fn [state]
    (eval/eval-MakeArrayStmt target state)))

(defn make-MakeNullStmt [{:strs [target] :as stmt-info}]
  (log/debugf "making MakeNullStmt stmt: %s" stmt-info)
  (fn [state]
    (eval/eval-MakeNullStmt target state)))

(defn make-MakeNumberIntStmt [{:strs [value target] :as stmt-info}]
  (log/debugf "making MakeNumberIntStmt stmt: %s" stmt-info)
  (fn [state]
    (eval/eval-MakeNumberIntStmt value target state)))

(defn make-MakeObjectStmt [{:strs [target] :as stmt-info}]
  (log/debugf "making MakeObjectStmt stmt: %s" stmt-info)
  (fn [state]
    (eval/eval-MakeObjectStmt target state)))

(defn make-MakeSetStmt [{:strs [target] :as stmt-info}]
  (log/debugf "making MakeSetStmt stmt: %s" stmt-info)
  (fn [state]
    (eval/eval-MakeSetStmt target state)))

(defn make-NopStmt [stmt-info]
  (log/debugf "making NopStmt stmt: %s" stmt-info)
  eval/eval-NopStmt)

(defn make-NotEqualStmt [{:strs [a b]}]
  (log/debug "making NotEqualStmt stmt")
  (fn [state]
    (eval/eval-NotEqualStmt a b state)))

(defn make-NotStmt [{:strs [block]}]
  (log/debug "making NotStmt stmt")
  (let [stmts (make-stmts (get block "stmts"))]
    (fn [state]
      (eval/eval-NotStmt stmts state))))

(defn make-ObjectInsertOnceStmt [{:strs [key value object]}]
  (log/debug "making ObjectInsertOnceStmt stmt")
  (fn [state]
    (eval/eval-ObjectInsertOnceStmt key value object state)))

(defn make-ObjectInsertStmt [{:strs [key value object]}]
  (log/debug "making ObjectInsertStmt stmt")
  (fn [state]
    (eval/eval-ObjectInsertStmt key value object state)))

(defn make-ObjectMergeStmt [{:strs [a b target]}]
  (log/debug "making ObjectMergeStmt stmt")
  (fn [state]
    (eval/eval-ObjectMergeStmt a b target state)))

(defn make-ResetLocalStmt [{:strs [target]}]
  (log/debug "making ResetLocalStmt stmt")
  (fn [state]
    (eval/eval-ResetLocalStmt target state)))

(defn make-ResultSetAddStmt [{:strs [value]}]
  (log/debug "making ResultSetAddStmt stmt")
  (fn [state]
    (eval/eval-ResultSetAddStmt value state)))

(defn make-ReturnLocalStmt [_]
  (log/debug "making ReturnLocalStmt stmt")
  eval/eval-ReturnLocalStmt)

(defn make-SetAddStmt [{:strs [set value]}]
  (log/debug "making SetAddStmt stmt")
  (fn [state]
    (eval/eval-SetAddStmt set value state)))

(defn make-ScanStmt [{:strs [source key value block]}]
  (log/debug "making ScanStmt stmt")
  (let [block (make-block block)]
    (fn [state]
      (eval/eval-ScanStmt source key value block state))))

(defn make-WithStmt [{:strs [block local path value]}]
  (log/debug "making WithStmt stmt")
  (let [stmts (make-stmts (get block "stmts"))]
    (fn [state]
      (eval/eval-WithStmt local path value stmts state))))

(defn make-stmt [{:strs [type] :as stmt-info}]
  (log/debugf "making stmt: %s" stmt-info)
  (let [{stmt-info "stmt"} stmt-info
        stmt (case type
               "ArrayAppendStmt" (make-ArrayAppendStmt stmt-info)
               "AssignIntStmt" (make-AssignIntStmt stmt-info)
               "AssignVarOnceStmt" (make-AssignVarOnceStmt stmt-info)
               "AssignVarStmt" (make-AssignVarStmt stmt-info)
               "BlockStmt" (make-BlockStmt stmt-info)
               "BreakStmt" (make-BreakStmt stmt-info)
               "CallDynamicStmt" (make-CallDynamicStmt stmt-info)
               "CallStmt" (make-CallStmt stmt-info)
               "DotStmt" (make-DotStmt stmt-info)
               "EqualStmt" (make-EqualStmt stmt-info)
               "IsArrayStmt" (make-IsArrayStmt stmt-info)
               "IsDefinedStmt" (make-IsDefinedStmt stmt-info)
               "IsObjectStmt" (make-IsObjectStmt stmt-info)
               "IsUndefinedStmt" (make-IsUndefinedStmt stmt-info)
               "LenStmt" (make-LenStmt stmt-info)
               "MakeArrayStmt" (make-MakeArrayStmt stmt-info)
               "MakeNullStmt" (make-MakeNullStmt stmt-info)
               "MakeNumberIntStmt" (make-MakeNumberIntStmt stmt-info)
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
               "WithStmt" (make-WithStmt stmt-info)
               (throw (ex-info (sprintf "%s statement type not implemented" type) {:type :parser-exception})))]
    (fn [state]
      (eval/eval-stmt type stmt state))))

(defn make-stmts [stmts-info]
  (log/debug "making stmts")
  (let [stmts (mapv make-stmt stmts-info)]
    (fn [state]
      (eval/eval-stmts stmts state))))

(defn make-block [{:strs [stmts]}]
  (log/debug "making block")
  (let [stmts (make-stmts stmts)]
    (fn [state]
      (eval/eval-block stmts state))))

(defn make-blocks [blocks-info]
  (log/debug "making blocks")
  (let [blocks (mapv make-block blocks-info)]
    (fn [state]
      (eval/eval-blocks blocks state))))

(defn- stringify-coll-keys [val]
  (cond
    (or (list? val) (set? val) (vector? val))
    (vec (map stringify-coll-keys val))
    (map? val)
    (reduce (fn [pairs [key val]]
              (let [key (if (string? key) key (json/write-str key))
                    val (stringify-coll-keys val)]
                (assoc pairs key val)))
            {}
            (seq val))
    :else val))

(defn- align-result-set [result-set]
  (stringify-coll-keys result-set))

(defn make-plan [{:strs [name] blocks-info "blocks" :as plan-info}]
  (log/debugf "making plan '%s'" name)
  (log/tracef "plan: %s" plan-info)
  (let [blocks (make-blocks blocks-info)]
    ; return a [name fn] pair
    [name (fn [info data input]
            (let [state (state/init-state info input data)]
              (log/debugf "Plan - executing '%s'" name)
              (let [state (blocks state)
                    result-set (get state :result-set)
                    result-set (align-result-set result-set)]
                (log/debugf "Plan - result-set: %s" result-set)
                result-set)))]))

(defn make-plans [{:strs [plans]}]
  (log/debug "making plans")
  (mapv make-plan plans))

(defn make-func [{:strs [name path params] return-index "return" blocks-info "blocks"}]
  (let [blocks (make-blocks blocks-info)]
    (log/debugf "making func <%s>" name)
    [name path (fn [args state]
                 (eval/eval-func name params return-index blocks args state))]))

(defn make-funcs [{:strs [funcs]}]
  (log/debug "making funcs")
  (let [assoc-func (fn [func-map func-info]
                     (let [[name path func] (make-func func-info)]
                       (-> func-map (assoc name func) (assoc path func))))]
    (reduce assoc-func {} funcs)))

(defn make-builtin-func [{:strs [name]} builtin-resolver]
  (let [builtin-func (builtin-resolver name)]
    (log/debugf "making built-in func <%s>" name)
    (if (nil? builtin-func)
      (throw (ex-info (sprintf "unknown function '%s'" name) {:type :parser-exception}))
      [name (fn [args state]
              (eval/eval-builtin-func name builtin-func args state))])))

(defn make-builtin-funcs [builtin-funcs-info builtin-resolver]
  (log/debug "making built-in funcs")
  (let [assoc-builtin-func (fn [func-map func-info]
                             (let [[name func] (make-builtin-func func-info builtin-resolver)]
                               (assoc func-map name func)))]
    (reduce assoc-builtin-func {} builtin-funcs-info)))

(defn parse
  "Parses the incoming Intermediate Representation map"
  ([ir]
   (parse ir builtins/get-builtin))
  ([{:strs [static plans funcs]} builtin-resolver]
   (let [builtin-funcs-info (get static "builtin_funcs")]
     {:plans         (make-plans plans)
      :funcs         (make-funcs funcs)
      :builtin-funcs (make-builtin-funcs builtin-funcs-info builtin-resolver)
      :static        static})))

(defn parse-json
  "Parses the incoming string"
  ([str]
   (parse (json/read-str str)))
  ([str builtin-resolver]
   (parse (json/read-str str) builtin-resolver)))

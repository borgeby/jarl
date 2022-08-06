(ns jarl.eval
  (:require [clojure.string :as string]
            [taoensso.timbre :as log]
            [jarl.builtins.utils :refer [check-args]]
            [jarl.formatting :refer [sprintf]]
            [jarl.state :as state]
            [jarl.types :as types]
            [jarl.utils :as utils]
            [jarl.exceptions :as errors]
            #?(:clj  [clojure.edn :as edn]
               :cljs [cljs.tools.reader.edn :as edn]))
  #?(:clj (:import  (clojure.lang ExceptionInfo))))

(defn break
  ([state] (assoc state :break-index 0))
  ([state break-index] (assoc state :break-index break-index)))

(defn eval-ArrayAppendStmt [array-index value-index state]
  (if-not (state/contains-local? state array-index)
    (do
      (log/debugf "ArrayAppendStmt - <%s> is not a local var" array-index)
      (break state))
    (if (state/contains-value? state value-index)
      (let [val (state/get-value state value-index)
            array (state/get-local state array-index)]
        (log/debugf "ArrayAppendStmt - Appending '%s' to <%s>" val array-index)
        (state/set-local state array-index (conj array val)))
      (do
        (log/debugf "ArrayAppendStmt - value <%s> not present" value-index)
        (break state)))))

(defn eval-AssignIntStmt [value target state]
  (when (or (not (number? value))
            (not (zero? (mod value 1))))
    (throw (ex-info (sprintf "'%s' is not an integer" value) {:type :eval-exception})))
  (log/debugf "AssignIntStmt - assigning '%d' to local var %d" value target)
  (state/set-local state target (int value)))

(defn eval-AssignVarStmt [source-index target state]
  (if-not (state/contains-value? state source-index)
    (do
      (log/debugf "AssignVarStmt - <%s> not present, not making assignment" source-index)
      state)                                                ; 'undefined' source value doesn't cause abort
    (let [val (state/get-value state source-index)]
      (log/debugf "AssignVarStmt - assigning '%s' from <%s> to <%s>" val source-index target)
      (state/set-local state target val))))

(defn eval-AssignVarOnceStmt [source-index target state]
  (log/tracef "AssignVarOnceStmt - Assigning var <%s> to <%d>, unless already present and not equal", source-index, target)
  (if-not (state/contains-value? state source-index)
    (do
      (log/debugf "AssignVarOnceStmt - <%s> not present, not making assignment" source-index)
      state)
    (let [value (state/get-value state source-index)]
      (if (state/contains-local? state target)
        (let [orig-value (state/get-local state target)]
          (if (types/rego-equal? value orig-value)
            state                                           ; do nothing, existing value == new value
            (do
              (log/debugf "AssignVarOnceStmt - <%s> already assigned" source-index)
              (throw (errors/assignment-conflict-ex "<%s> already assigned" source-index)))))
        (do
          (log/debugf "AssignVarOnceStmt - assigning '%s' from <%s> to <%s>" value source-index target)
          (state/set-local state target value)))))
  )

(defn eval-BlockStmt [blocks state]
  (log/debug "BlockStmt - ")
  (blocks state))

(defn eval-BreakStmt [index state]
  (log/debugf "BreakStmt - index: %s", index)
  (break state index))

(defn- call-func [func target func-name args state]
  (if (nil? func)
    (throw (ex-info (sprintf "unknown function '%s'" func-name) {:type :eval-exception}))
    (let [func-state (dissoc state :local :plans)
          result (func args func-state)]
      (if (contains? result :result)
        (do
          (log/debugf "CallStmt - <%s> returning: '%s'" func-name result)
          (state/set-local state target (get result :result)))
        (do
          (log/debugf "CallStmt - <%s> undefined" func-name)
          (break state))))))

(defn- create-func-args [state keys]
  (loop [i 0
         args {}
         keys keys]
    (if (empty? keys)
      args
      (let [key (first keys)]
        (if (state/contains-value? state key)
          (recur (inc i) (assoc args i (state/get-value state key)) (next keys))
          (recur (inc i) args (next keys)))))))

(defn eval-CallDynamicStmt [target path args state]
  (let [path (map #(state/get-value state %) path)
        func-name (string/join "." path)]
    (log/debugf "CallDynamicStmt - calling dynamic func <%s>" func-name)
    (let [func (state/get-func state path)
          args (create-func-args state (map (fn [val] {"type" "local" "value" val}) args))]
      (if (nil? func)
        (break state)                                       ; dynamic func miss
        (call-func func target func-name args state)))))

(defn eval-CallStmt [target func-name args state]
  (log/debugf "CallStmt - calling func <%s> with args: %s; target <%d>" func-name args target)
  (let [func (state/get-func state func-name)
        args (create-func-args state args)]                 ; OPA IR docs claims args to be 'positional' (local), but they are of 'operand' type.
    (log/tracef "CallStmt - realized args: %s" args)
    (call-func func target func-name args state)))

(defn eval-DotStmt
  "Gets value with key described by `key-info` from source described by `source-info` and stores it in local var at `target-index`"
  [source-pos key-pos target-index state]
  (log/debugf "DotStmt - getting <%s> from <%s> to <%d>" key-pos source-pos target-index)
  (cond
    (not (state/contains-value? state source-pos)) (do
                                                     (log/debugf "DotStmt - source <%s> not present" source-pos)
                                                     (break state))
    (not (state/contains-value? state key-pos)) (do
                                                  (log/debugf "DotStmt - key <%s> not present" key-pos)
                                                  (break state))
    :else (let [source (state/get-value state source-pos)
                key (state/get-value state key-pos)]
            (if-not (coll? source)
              (do
                (log/debugf "DotStmt - '%s' at <%s> is not a collection" source source-pos)
                (break state))
              (if (contains? source key)
                (let [value (get source key)]
                  (log/debugf "DotStmt - got '%s' to var <%s>" value target-index)
                  (state/set-local state target-index value))
                (do
                  (log/debugf "DotStmt - <%s> not present in <%s>" key source-pos)
                  (break state)))))))

(defn eval-EqualStmt [a-pos b-pos state]
  (log/tracef "EqualStmt - <%s> == <%s>" a-pos b-pos)
  (let [a (state/get-value state a-pos)
        b (state/get-value state b-pos)
        result (types/rego-equal? a b)]
    (log/debugf "EqualStmt - ('%s' == '%s') == %s" a b result)
    (if result
      state
      (break state))))

(defn eval-IsArrayStmt [source state]
  (let [obj (state/get-value state source)]
    (if (or (list? obj) (set? obj) (vector? obj))
      (do
        (log/debugf "IsArrayStmt - <%s> is array" source)
        state)
      (do
        (log/debugf "IsArrayStmt - <%s> is not array" source)
        (break state)))))

(defn eval-IsDefinedStmt [source state]
  (if (state/contains-local? state source)
    (do
      (log/debugf "IsDefinedStmt - local var <%d> is defined" source)
      state)
    (do
      (log/debugf "IsDefinedStmt - local var <%d> is not defined" source)
      (break state))))

(defn eval-IsUndefinedStmt [source state]
  (if-not (state/contains-local? state source)
    (do
      (log/debugf "IsUndefinedStmt - local var <%d> is not defined" source)
      state)
    (do
      (log/debugf "IsUndefinedStmt - local var <%d> is defined" source)
      (break state))))

(defn eval-IsObjectStmt [source state]
  (let [obj (state/get-value state source)]
    (if (map? obj)
      (do
        (log/debugf "IsObjectStmt - <%s> is object" source)
        state)
      (do
        (log/debugf "IsObjectStmt - <%s> is not object" source)
        (break state)))))

(defn eval-LenStmt [source target state]
  (let [val (state/must-get-value state source)]
    (if (or (coll? val) (string? val))
      (let [len (count val)]
        (state/set-local state target len))
      (throw (ex-info "invalid argument(s)" {:type :eval-exception})))))

(defn eval-MakeNumberRefStmt
  "Parses the static string at `index` into a number, putting the result in local var `target`"
  [index target state]
  (log/tracef "MakeNumberRefStmt - parsing local number <%d> to local var <%d>" index target)
  (let [val (edn/read-string (state/get-static-string state index))]
    (log/debugf "MakeNumberRefStmt - putting parsed number '%s' in local var <%d>" val target)
    (state/set-local state target val)))

(defn eval-MakeArrayStmt [target state]
  (log/debugf "MakeArrayStmt - assigning empty array to local var %d" target)
  (state/set-local state target []))

(defn eval-MakeNullStmt [target state]
  (log/debugf "MakeNullStmt - assigning 'null' to local var %d" target)
  (state/set-local state target nil))

(defn eval-MakeNumberIntStmt [value target state]
  (when (or (not (number? value))
            (not (zero? (mod value 1))))
    (throw (ex-info (sprintf "'%s' is not an integer" value) {:type :eval-exception})))
  (let [value (int value)]
    (log/debugf "MakeNumberIntStmt - assigning '%d' to local var <%d>" value target)
    (state/set-local state target value)))

(defn eval-MakeObjectStmt [target state]
  (log/debugf "MakeObjectStmt - assigning empty object to local var <%d>" target)
  (state/set-local state target {}))

(defn eval-MakeSetStmt [target state]
  (log/debugf "MakeSetStmt - assigning empty set to local var <%d>" target)
  (state/set-local state target (sorted-set-by types/rego-compare)))

(defn eval-NopStmt [state]
  (log/debug "NopStmt - Doing nothing")
  state)

(defn eval-NotEqualStmt [a-pos b-pos state]
  (log/tracef "NotEqualStmt - <%s> != <%s>" a-pos b-pos)
  (let [a (state/get-value state a-pos)
        b (state/get-value state b-pos)
        result (not= a b)]
    (log/debugf "NotEqualStmt - ('%s' != '%s') == %s" a b result)
    (if result
      state
      (break state))))

(defn eval-NotStmt [stmts state]
  (let [state (stmts state)
        break-index (get state :break-index)]
    (if-not (nil? break-index)
      (do
        (log/tracef "NotStmt - not defined; break-index: %d" break-index)
        (let [new-break-index (dec break-index)]
          (if (>= new-break-index 0)
            (break state new-break-index)
            (dissoc state :break-index))))
      (do
        (log/trace "NotStmt - defined")
        (break state)))))

(defn eval-ObjectInsertOnceStmt [key-index value-index object-index state]
  (let [object (state/get-local state object-index)
        key (state/get-value state key-index)
        value (state/get-value state value-index)]
    (if (nil? object)
      (do
        (log/debugf "ObjectInsertOnceStmt - <%s> not present" object-index)
        (break state))
      (let [old-value (get object key)]
        (if (and (not (nil? old-value)) (not= old-value value))
          (do
            (log/debugf "ObjectInsertOnceStmt - object <%s> already contains key <%s> with different value" object-index key-index)
            (throw (errors/conflict-ex "object keys must be unique")))
          (do
            (log/debugf "ObjectInsertOnceStmt - inserting '%s' at <%s> to var <%d>" value key object-index)
            (state/set-local state object-index (assoc object key value))))))))

(defn eval-ObjectInsertStmt [key-index value-index object-index state]
  (let [object (state/get-local state object-index)
        key (state/get-value state key-index)
        value (state/get-value state value-index)]
    (if (nil? object)
      (do
        (log/debugf "ObjectInsertStmt - <%s> not present" object-index)
        (break state))
      (do
        (log/debugf "ObjectInsertStmt - inserting '%s' at <%s> to var <%d>" value key object-index)
        (state/set-local state object-index (assoc object key value))))))

(defn eval-ObjectMergeStmt [to-key from-key target-key state]
  (log/debugf "ObjectMergeStmt - merging %d and %d into %d" to-key from-key target-key)
  (state/set-local state target-key (merge (state/get-local state to-key) (state/get-local state from-key))))

(defn eval-ResetLocalStmt [target state]
  (log/debugf "ResetLocalStmt - resetting %d" target)
  (state/dissoc-local state target))

(defn eval-ResultSetAddStmt [value state]
  (let [val (state/get-local state value)]
    (if (nil? val)
      (do
        (log/debug "ResultSetAddStmt - nothing to add to result set")
        state)
      (do
        (log/debugf "ResultSetAddStmt - adding %s to resultset" val)
        (state/add-result state val)))))

(defn eval-ReturnLocalStmt [state]
  (log/debug "ReturnLocalStmt - exiting function")          ; TODO: Do we need to recursively break out of all nested blocks to exit the function?
  state)                                                    ; No-op, the function itself knows what local var is the result

(defn eval-SetAddStmt [set-index value-pos state]
  (let [set (state/get-local state set-index)]
    (if-not (set? set)
      (do
        (log/debugf "SetAddStmt - local var <%s> is not a set" set-index)
        (break state))
      (if-not (state/contains-value? state value-pos)
        (do
          (log/debugf "SetAddStmt - value <%s> not present" value-pos)
          (break state))
        (let [val (state/get-value state value-pos)]
          (log/debugf "SetAddStmt - Adding '%s' to <%s>" val set-index)
          (state/set-local state set-index (conj set val)))))))

(defn eval-ScanStmt
  "Scan list/set/map local var at `source-index`; executing `block-stmt` for every key/index-value pair encountered"
  [source-index key-index value-index block-stmt state]
  (log/debugf "ScanStmt - scanning <%d>" source-index)
  (let [source (state/get-local state source-index)]
    (if-not (coll? source)                                  ; OPA IR docs states 'source' may not be an empty collection; but if we 'break' for such, statements like 'every x in [] { x != x }' will be 'undefined'.
      (do
        (log/debugf "ScanStmt - '%s' is empty or not a collection" source-index)
        (break state))
      (let [is-set (set? source)]
        (log/trace "ScanStmt - source is list or map")
        (loop [source-indexed (if (map? source)
                                source
                                (map-indexed (fn [i v] [i v]) source))
               state state]
          (if (empty? source-indexed)
            (do
              (log/trace "ScanStmt - done")
              state)
            (let [entry (first source-indexed)
                  value (get entry 1)
                  key (if is-set value (get entry 0))       ; Rego set entries are indexed by their value
                  state (state/set-local state key-index key)
                  state (state/set-local state value-index value)]
              (log/tracef "ScanStmt - executing block with key <%d> = '%s', and value <%d> = '%s'"
                          key-index key value-index value)
              (recur (next source-indexed) (block-stmt state)))))))))

(defn- int-path-to-str-path [state int-path]
  (vec (map #(state/get-string state %) int-path)))         ; doall doesn't realize the array in a way that can be logged properly

(defn eval-WithStmt [local-index path value-info stmts state]
  (let [str-path (int-path-to-str-path state path)
        value (state/get-value state value-info)
        state (state/push-with-stack state local-index str-path value)]
    (log/debugf "WithStmt - replacing <%s> in local var <%d> with '%s'" str-path local-index value)
    (state/pop-with-stack (stmts state))))

(defn eval-stmt [type stmt state]
  (log/tracef "%s - calling with vars: %s; with-stack: %s" type (get state :local) (get state :with-stack))
  (try
    (stmt state)
    (catch ExceptionInfo e
      (if (errors/undefined-ex? e)
        (do
          (log/debugf "statement type %s produced undefined result: %s" type (ex-message e))
          (break state))
        (throw e)))))

(defn eval-stmts [stmts state]
  (log/debug "executing statements")
  (loop [stmts stmts
         state state]
    (let [stmts-count (count stmts)]
      (if (or (zero? stmts-count) (contains? state :break-index))
        (do
          (when (pos? stmts-count)
            (log/tracef "skipping %d statement(s)" stmts-count))
          state)
        (recur (next stmts) ((first stmts) state))))))

(defn eval-block [stmts state]
  (log/debug "block - executing")
  (let [state (stmts state)
        break-index (get state :break-index)]
    (if-not (nil? break-index)
      (do
        (log/tracef "block - broke out of block; break-index: %d" break-index)
        (let [new-break-index (dec break-index)]
          (if (>= new-break-index 0)
            (break state new-break-index)
            (do
              (log/trace "block - ending break sequence")
              (dissoc state :break-index)))))
      state)))

(defn eval-blocks [blocks state]
  (log/debug "blocks - executing")
  (loop [blocks blocks
         state state]
    (if (or (contains? state :break-index) (empty? blocks))
      (do
        (let [block-count (count blocks)]
          (when (pos? block-count)
            (log/tracef "skipping %d block(s)" block-count)))
        state)
      (recur (next blocks) ((first blocks) state)))))

(defn- map-args-by-params [params args]
  (loop [i 0
         params params
         mapped-args {}]
    (if (empty? params)
      mapped-args
      (let [param (first params)]
        (if (contains? args i)
          (recur (inc i) (next params) (assoc mapped-args param (get args i)))
          (recur (inc i) (next params) mapped-args))))))

(defn eval-func [name params return-index blocks args state]
  (log/debugf "func - executing <%s>" name)
  (let [local (map-args-by-params params args)
        state (assoc state :local local)]
    (try
      (let [state (blocks state)]
        (if (state/contains-local? state return-index)
          (let [result (state/get-local state return-index)]
            (log/debugf "function <%s> returning '%s'" name result)
            {:result result})
          (do
            (log/debugf "function <%s> undefined" name)
            {})))
      (catch ExceptionInfo e
        (condp = (errors/ex-type e)
          :jarl.exceptions/multiple-outputs-conflict-ex
          (do
            (log/tracef e "re-throwing")
            (throw e)) ; ConflictException has already been made more specific by nested function/rule call; re-throw
          ::errors/assignment-conflict-exception
          (do
            (log/tracef "nested call threw AssignmentConflictException: %s" (ex-message e))
            (if (> (count params) 2) ; 'input' and 'data' documents are always present as args 0 and 1, respectively; additional args means this is a function call, otherwise a rule.
              (throw (errors/multiple-outputs-conflict-ex e "functions must not produce multiple outputs for same inputs"))
              (throw (errors/multiple-outputs-conflict-ex e "complete rules must not produce multiple outputs"))))
          ; default
          (throw e))))))

(defn- ->type [{:strs [type of]}]
  (if (nil? of)
    type
    (if (vector? of)
      (let [types (set (map #(get % "type") of))]
        (if (= types #{"any"}) "any" types)) ; special case for type_name built-in, which is declared as "any of any"
      (get of "type"))))

(defn type-check-args [builtin-name plan-builtins argv]
  (when-not (zero? (count argv)) ; no check for zero-arity functions
    (if-let [args-def (-> (filterv #(= builtin-name (get % "name")) plan-builtins)
                          (first)
                          (get-in ["decl" "args"]))]
      (let [types-def (mapv ->type args-def)]
        (check-args builtin-name types-def argv))
      (throw (errors/type-ex "Arguments definition for builtin %s not provided in plan" builtin-name)))))

(defn eval-builtin-func [name builtin-func args state]
  (log/debugf "executing built-in func <%s> with args: %s" name, args)
    (try
      (let [argv (utils/indexed-map->vector args)
            plan-builtins (get-in state [:static "builtin_funcs"])
            _ (type-check-args name plan-builtins argv)
            result (builtin-func {:args argv :builtin-context (:builtin-context state)})]
        (log/debugf "built-in function <%s> returning '%s'" name result)
        {:result result})
      (catch ExceptionInfo e
        (if (or (errors/builtin-ex? e) (errors/type-ex? e))
          (condp = name
            ; Special cases - these builtin functions have a more relaxed runtime type check of arguments than the
            ; function definition suggests. It would be nice to set this attribute as metadata on these builtins, but
            ; it has proven to be problematic to get the _var_ here rather than the function. Additionally, using
            ; metadata on vars will not work for implementation written in e.g. Java or Javascript. A proper solution
            ; will likely involve a `BuiltinFunction` _protocol_ or similar, which may be implemented in either
            ; Clojure or any of the target host platforms. Until then..
            "regex.is_valid" {:result false}
            "yaml.is_valid"  {:result false}
            "json.is_valid"  {:result false}

            ; default
            (do
              (log/tracef "function <%s> threw error: %s" name (ex-message e))
              (if (true? (get state :strict-builtin-errors))
                (throw e)
                (do
                  (log/debugf "function <%s> returned undefined value" name)
                  {}))))
          (throw e)))))

(defn find-plan [info entry-point]
  (let [plan (some (fn [[name plan]] (when (= name entry-point) plan)) (:plans info))]
    plan))

(defn eval-plan [info entry-point data input]
  (let [plan (find-plan info entry-point)]
    (when (nil? plan)
      (throw (errors/undefined-ex "no plan for entrypoint: %s" entry-point)))
    (plan info data input)))

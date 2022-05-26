(ns jarl.eval
  (:require [clojure.edn :as edn]
            [clojure.string :as string]
            [jarl.state :as state]
            [jarl.types :as types]
            [jarl.util :as util]
            [clojure.tools.logging :as log])
  (:import (se.fylling.jarl BuiltinException UndefinedException)))

(defn break
  ([state] (assoc state :break-index 0))
  ([state break-index] (assoc state :break-index break-index)))

(defn eval-ArrayAppendStmt [array-index value-index state]
  (if-not (state/contains-local? state array-index)
    (do
      (log/infof "ArrayAppendStmt - <%s> is not a local var" array-index)
      (break state))
    (let [val (state/get-value state value-index)]
      (if (nil? val)
        (do
          (log/debugf "ArrayAppendStmt - value <%s> not present" value-index)
          (break state))
        (let [array (state/get-local state array-index)]
          (log/debugf "ArrayAppendStmt - Appending '%s' to <%s>" val array-index)
          (state/set-local state array-index (conj array val)))))))

(defn eval-AssignIntStmt [value target state]
  (when (or (not (number? value))
            (not (zero? (mod value 1))))
    (throw (Exception. (format "'%s' is not an integer" value))))
  (log/debugf "AssignIntStmt - assigning '%d' to local var %d" value target)
  (state/set-local state target (int value)))

(defn eval-AssignVarStmt [source-index target state]
  (if-not (state/contains-value? state source-index)
    (do
      (log/debugf "AssignVarStmt - <%s> not present" source-index)
      (break state))
    (let [val (state/get-value state source-index)]
      (log/debugf "AssignVarStmt - assigning '%s' from <%s> to <%s>" val source-index target)
      (state/set-local state target val))))

(defn eval-AssignVarOnceStmt [source-index target state]
  (if (state/contains-local? state target)
    (throw (Exception. (format "local %s already assigned" target)))
    (if-not (state/contains-value? state source-index)
      (do
        (log/debugf "AssignVarOnceStmt - <%s> not present" source-index)
        (break state))
      (let [val (state/get-value state source-index)]
        (log/debugf "AssignVarOnceStmt - assigning '%s' from <%s> to <%s>" val source-index target)
        (state/set-local state target val)))))

(defn eval-BlockStmt [blocks state]
  (log/debug "BlockStmt - ")
  (blocks state))

(defn eval-BreakStmt [index state]
  (log/debugf "BreakStmt - index: %s", index)
  (break state index))

(defn- call-func [func target func-name args state]
  (if (nil? func)
    (throw (Exception. (format "unknown function '%s'" func-name)))
    (let [local (util/map-by-index args)
          func-state (assoc (select-keys state [:static :funcs :builtin-funcs]) :local local)
          result (func func-state)]
      (if (contains? result :result)
        (do
          (log/debugf "CallStmt - <%s> returning: '%s'" func-name result)
          (state/set-local state target (get result :result)))
        (do
          (log/debugf "CallStmt - <%s> undefined" func-name)
          (break state))))))

(defn eval-CallDynamicStmt [target path args state]
  (let [path (map #(state/get-value state %) path)
        func-name (string/join "." path)]
    (log/debugf "CallDynamicStmt - calling dynamic func <%s>" path)
    (let [func (state/get-func state path)
          args (map #(state/get-local state %) args)]
      (call-func func target func-name args state))))

(defn eval-CallStmt [target func-name args state]
  (log/debugf "CallStmt - calling func <%s>" func-name)
  (let [func (state/get-func state func-name)
        args (map (fn [arg] (state/get-value state arg)) args)]
    (call-func func target func-name args state)))

(defn eval-DotStmt [source-index key-index target state]
  (let [source (state/get-value state source-index)
        key (state/get-value state key-index)]
    (if (nil? source)
      (do
        (log/debugf "DotStmt - <%s> not present" source-index)
        (break state))
      (let [val (get (state/get-value state source-index) key)]
        (if-not (nil? val)
          (do
            (log/debugf "DotStmt - got '%s' to var <%s>" val target)
            (state/set-local state target val))
          (do
            (log/debugf "DotStmt - <%s> not present in <%s>" key source-index)
            (break state)))))))

(defn eval-EqualStmt [a-index b-index state]
  (let [a (state/get-value state a-index)
        b (state/get-value state b-index)
        result (= a b)]
    (log/debugf "EqualStmt - '%s' != '%s' == %s" a b result)
    (if result
      state
      (break state))))

(defn eval-IsArrayStmt [source state]
  (let [obj (state/get-value state source)]
    (if (or (list? obj) (set? obj))
      (do
        (log/debugf "IsArrayStmt - <%d> is array" source)
        state)
      (do
        (log/debugf "IsArrayStmt - <%d> is not array" source)
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
        (log/debugf "IsObjectStmt - <%d> is object" source)
        state)
      (do
        (log/debugf "IsObjectStmt - <%d> is not object" source)
        (break state)))))

(defn eval-LenStmt [source target state]
  (let [val (state/must-get-value state source)]
    (if (or (coll? val) (string? val))
      (let [len (count val)]
        (state/set-local state target len))
      (throw (Exception. "invalid argument(s)")))))

(defn eval-MakeNumberRefStmt [index target state]
  (let [val (edn/read-string (state/get-static-string state index))]
    (log/debugf "MakeNumberRefStmt - parsed number: %s" val)
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
    (throw (Exception. (format "'%s' is not an integer" value))))
  (log/debugf "MakeNumberIntStmt - assigning '%d' to local var %d" value target)
  (state/set-local state target (int value)))

(defn eval-MakeObjectStmt [target state]
  (log/debugf "MakeObjectStmt - assigning empty object to local var %d" target)
  (state/set-local state target {}))

(defn eval-MakeSetStmt [target state]
  (log/debugf "MakeSetStmt - assigning empty set to local var %d" target)
  (state/set-local state target (sorted-set-by types/rego-compare)))

(defn eval-NopStmt [state]
  (log/debug "NopStmt - Doing nothing")
  state)

(defn eval-NotEqualStmt [a-index b-index state]
  (let [a (state/get-value state a-index)
        b (state/get-value state b-index)
        result (not= a b)]
    (log/debugf "NotEqualStmt - '%s' != '%s' == %s" a b result)
    (if result
      state
      (break state))))

(defn eval-NotStmt [block state]
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
            (break state))
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

(defn eval-SetAddStmt [set-index value-index state]
  (let [set (state/get-local state set-index)]
    (if (nil? set)
      (do
        (log/debugf "SetAddStmt - <%s> is not a local var" set-index)
        (break state))
      (let [val (state/get-value state value-index)]
        (if (nil? val)
          (do
            (log/debugf "SetAddStmt - value <%s> not present" value-index)
            (break state))
          (do
            (log/debugf "SetAddStmt - Adding '%s' to <%s>" val set-index)
            (state/set-local state set-index (conj set val))))))))

(defn eval-ScanStmt [source-index key-index value-index block-stmt state]
  (log/debugf "ScanStmt - scanning <%d>" source-index)
  (let [source (state/get-local state source-index)]
    (if (or (nil? source) (not (coll? source)) (empty? source))
      (do
        (log/debugf "ScanStmt - '%s' is not a collection" source-index)
        (break state))
      (do
        (log/trace "ScanStmt - source is list or map")
        (loop [source-indexed (if (map? source)
                                source
                                (map-indexed (fn [i v] [i v]) source))
               state state]
          (if (empty? source-indexed)
            (do
              (log/trace "ScanStmt - done")
              (break state))
            (let [entry (first source-indexed)
                  key (get entry 0)
                  value (get entry 1)
                  state (state/set-local state key-index key)
                  state (state/set-local state value-index value)]
              (recur (next source-indexed) (block-stmt state)))))))))

(defn eval-stmt [type stmt state]
  (log/tracef "%s - calling with vars: %s" type (get state :local))
  (try
    (stmt state)
    (catch UndefinedException e
      (log/debugf "statement type %s produced undefined result: %s" type (.getMessage e))
      (break state))))

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
        (log/tracef "broke out of block; break-index: %d" break-index)
        (let [new-break-index (dec break-index)]
          (if (>= new-break-index 0)
            (break state new-break-index)
            (dissoc state :break-index))))
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

(defn eval-func [name return-index blocks state]
  (log/debugf "func - executing <%s>" name)
  (let [state (blocks state)]
    (if (state/contains-local? state return-index)
      (let [result (state/get-local state return-index)]
        (log/debugf "function <%s> returning '%s'" name result)
        {:result result})
      (do
        (log/debugf "function <%s> undefined" name)
        {}))))

(defn eval-builtin-func [name builtin-func state]
  (let [args (get state :local)]
    (log/debugf "executing built-in func <%s> with args: %s" name, args)
    (try
      (let [arg-list (util/indexed-map-to-array args)
            result (apply builtin-func arg-list)]
        (log/debugf "built-in function <%s> returning '%s'" name result)
        {:result result})
      (catch BuiltinException e
        (log/debugf "function <%s> returned undefined value: %s" name (.getMessage e))
        {}))))

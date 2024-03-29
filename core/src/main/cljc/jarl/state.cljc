(ns jarl.state
  (:require [taoensso.timbre :as log]
            [jarl.runtime.environment :as environment]
            [jarl.time :as time]
            [jarl.utils :as utils]))

(defn- upsert-local [value stack index]
  (let [upsert (fn [aggregate [stack-index stack-path stack-value]]   ; we process the stack bottom->top
                 (if (= stack-index index)
                   (do (log/tracef "with-stack hit for <%d>" index)
                       (if (empty? stack-path)
                         stack-value
                         (utils/indiscriminate-assoc-in aggregate stack-path stack-value)))
                   aggregate))]
    (reduce upsert value stack)))

(defn get-local
  ([{stack :with-stack local :local} index]
   (let [value (get local index)]
     (upsert-local value stack index)))
  ([state index not-found]
   (let [value (get-local state index)]
     (if (nil? value)
       not-found
       value))))

(defn- stack-contains? [stack index]
  (some? (some (fn [[frame-index]] (= frame-index index)) stack)))

(defn contains-local? [{:keys [local with-stack]} index]
  (or (contains? local index) (stack-contains? with-stack index)))

(defn dissoc-local [{:keys [local] :as state} index]
  (assoc state :local (dissoc local index)))

(defn get-string [{:keys [static]} index]
  (get-in static ["strings" index "value"]))

(defn contains-string? [state index]
  (contains? (get-in state [:static "strings"]) index))

(defn set-local [{:keys [local] :as state} index value]
  (assoc state :local (assoc local index value)))

(defn get-value [state {key-type "type" key-value "value"}]
  (case key-type
    "local" (get-local state key-value)
    "string_index" (get-string state key-value)
    "bool" key-value
    (throw (ex-info (str"unknown value type '" key-type "'")
                    {:type :unknown-value-type}))))

(defn contains-value? [state {key-type "type" key-value "value"}]
  (case key-type
    "local" (contains-local? state key-value)
    "string_index" (contains-string? state key-value)
    "bool" true
    (throw (ex-info (str "unknown value type '" key-type "'")
                    {:type :unknown-value-type}))))

(defn must-get-value [state key]
  (if-not (contains-value? state key)
    (throw (ex-info (str "value with key '" key "' is undefined")
                    {:type :undefined-exception}))
    (get-value state key)))

(defn get-static-string [state index]
  (get-value state {"type" "string_index" "value" index}))

(defn get-func [state name]
  (get-in state [:funcs name]
          (get-in state [:builtin-funcs name])))

(defn add-result [{:keys [result] :as state} value]
  (assoc state :result-set (conj result value)))

(defn push-with-stack [state local-index path value]
  (let [stack (get state :with-stack [])
        frame [local-index path value]]
    (assoc state :with-stack (conj stack frame))))

(defn pop-with-stack [{stack :with-stack :or {stack []} :as state}]
  (let [stack (vec (butlast stack))]
    (if (empty? stack)
      (dissoc state :with-stack)
      (assoc state :with-stack stack))))

(defn init-state [info input data]
  (cond-> info
          ; if builtin-context has been provided already, use that
          (not (contains? info :builtin-context)) (assoc :builtin-context {:time-now-ns (time/now-ns)
                                                                           :env         environment/*env*})
          true (assoc :local (cond-> {}
                                     (some? input) (assoc 0 input)
                                     (some? data) (assoc 1 data)))))

(defn ->location [file_index row col state]
  {:file (get-in state [:static "files" file_index "value"]) :row row :col col})

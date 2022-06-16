(ns jarl.state
  (:require [jarl.utils :as utils])
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log])
  (:import (se.fylling.jarl UndefinedException)))

(defn- upsert-local [value stack index]
  (if (or (nil? stack) (empty? stack))
    value
    (loop [stack stack                                      ; we process the stack bottom->top
           aggregate value]
      (if (empty? stack)
        aggregate
        (let [[stack-index stack-path stack-value] (first stack)
              aggregate (if (= stack-index index)
                          (do (log/tracef "with-stack hit for <%d>" index)
                              (if (empty? stack-path)
                                stack-value
                                (utils/indiscriminate-assoc-in aggregate stack-path stack-value)))
                          aggregate)]
          (recur (next stack) aggregate))))))

(defn get-local
  ([state index]
   (let [stack (get state :with-stack)
         local (get state :local)
         value (get local index)]
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
    (throw (Exception. (format "unknown value type '%s'" key-type)))))

(defn contains-value? [state {key-type "type" key-value "value"}]
  (case key-type
    "local" (contains-local? state key-value)
    "string_index" (contains-string? state key-value)
    "bool" true
    (throw (Exception. (format "unknown value type '%s'" key-type)))))

(defn must-get-value [state key]
  (if-not (contains-value? state key)
    (throw (UndefinedException. (format "value with key '%s' is undefined" key)))
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

(defn pop-with-stack [state]
  (let [stack (get state :with-stack [])
        stack (vec (butlast stack))]
    (if (empty? stack)
      (dissoc state :with-stack)
      (assoc state :with-stack stack))))

; the data document seems to be expected to be a hierarchy of maps resembling the entry-point path (plan name).
(defn- data-from-plan-info [plan-info]
  (let [plan-name (get plan-info "name")]
    (if (pos? (count plan-name))
      (loop [components (reverse (str/split plan-name #"/"))
             result {}]
        (if (empty? components)
          result
          (recur (next components) {(first components) result})))
      {})))

(defn- make-data [plan-info data]
  (merge data (data-from-plan-info plan-info)))

(defn init-state [info input data]
  (cond-> info
          ; if builtin-context has been provided already, use that
          (not (contains? info :builtin-context)) (assoc :builtin-context {:time-now-ns (utils/time-now-ns)
                                                                           :env (System/getenv)})
          true (assoc :local (cond-> {}
                                     (some? input) (assoc 0 input)
                                     (some? data) (assoc 1 (make-data info data))))))

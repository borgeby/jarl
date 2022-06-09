(ns jarl.state
  (:import (se.fylling.jarl UndefinedException)))

(defn get-local [state index]
  (get (get state :local) index))

(defn contains-local? [{:keys [local]} index]
  (contains? local index))

(defn dissoc-local [{:keys [local] :as state} index]
  (assoc state :local (dissoc local index)))

(defn get-string [{:keys [static]} index]
  (get-in static ["strings" index "value"]))

(defn contains-string? [state index]
  (contains? (get (get state :static) "strings") index))

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
  (let [func (get-in state [:funcs name])]
    (if-not (nil? func)
      func
      (get-in state [:builtin-funcs name]))))

(defn add-result [{:keys [result] :as state} value]
  (assoc state :result-set (conj result value)))

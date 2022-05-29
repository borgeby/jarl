(ns jarl.state
  (:require [jarl.utils :as utils])
  (:import (se.fylling.jarl UndefinedException)))

(defn- upsert-local [value stack index]
  (if (or (nil? stack) (empty? stack))
    value
    (loop [stack (reverse stack)
           aggregate value]
      (if (empty? stack)
        aggregate
        (let [[stack-index stack-path stack-value] (first stack)
              aggregate (if (= stack-index index)
                          (utils/indiscriminate-assoc-in aggregate stack-path stack-value)
                          aggregate)]
          (recur (next stack) aggregate))))))

(defn get-local
  ([state index]
   (let [stack (get state :while-stack)
         local (get state :local)
         value (get local index)]
     (upsert-local value stack index)))
  ([state index not-found]
   (let [value (get-local state index)]
     (if (nil? value)
       not-found
       value))))

(defn contains-local? [state index]
  (contains? (get state :local) index))

(defn dissoc-local [state index]
  (let [local (get state :local)]
    (assoc state :local (dissoc local index))))

(defn get-string [state index]
  (get (get (get (get state :static) "strings") index) "value"))

(defn contains-string? [state index]
  (contains? (get (get state :static) "strings") index))

(defn set-local [state index value]
  (let [local (get state :local)]
    (assoc state :local (assoc local index value))))

(defn get-value [state key]
  (let [key-type (get key "type")
        key-value (get key "value")]
    (case key-type
      "local" (get-local state key-value)
      "string_index" (get-string state key-value)
      "bool" key-value
      (throw (Exception. (format "unknown value type '%s'" key-type))))))

(defn contains-value? [state key]
  (let [key-type (get key "type")
        key-value (get key "value")]
    (case key-type
      "local" (contains-local? state key-value)
      "string_index" (contains-string? state key-value)
      "bool" true
      (throw (Exception. (format "unknown value type '%s'" key-type))))))

(defn must-get-value [state key]
  (if-not (contains-value? state key)
    (throw (UndefinedException. (format "value with key '%s' is undefined" key)))
    (get-value state key)))

(defn get-static-string [state index]
  (get-value state {"type" "string_index" "value" index}))

(defn get-func [state name]
  (let [func (get (get state :funcs) name)]
    (if-not (nil? func)
      func
      (get (get state :builtin-funcs) name))))

(defn add-result [state value]
  (let [result-set (get state :result)]
    (assoc state :result-set (conj result-set value))))

(defn push-while-stack [state local-index path value]
  (let [stack (get state :while-stack [])
        frame [local-index path value]]
    (assoc state :while-stack (conj stack frame))))

(defn pop-while-stack [state]
  (let [stack (get state :while-stack [])
        stack (butlast stack)]
    (if (empty? stack)
      (dissoc state :while-stack)
      (assoc state :while-stack stack))))

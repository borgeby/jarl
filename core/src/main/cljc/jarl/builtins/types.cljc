(ns jarl.builtins.types
  (:require [jarl.exceptions :as errors]
            [jarl.types :as types]))

(defn builtin-is-number
  [{[x] :args}]
  (number? x))

(defn builtin-is-string
  [{[x] :args}]
  (string? x))

(defn builtin-is-boolean
  [{[x] :args}]
  (boolean? x))

(defn builtin-is-array
  [{[x] :args}]
  (vector? x))

(defn builtin-is-set
  [{[x] :args}]
  (set? x))

(defn builtin-is-object
  [{[x] :args}]
  (map? x))

(defn builtin-is-null
  [{[x] :args}]
  (nil? x))

(defn builtin-type-name
  [{[x] :args}]
  (let [type-name (types/->rego x)]
    (if (= type-name "floating-point number")
      "number"
      type-name)))

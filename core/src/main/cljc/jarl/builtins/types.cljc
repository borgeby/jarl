(ns jarl.builtins.types
  (:require [jarl.exceptions :as errors]
            [jarl.types :as types]))

(defn builtin-is-number
  [{[x] :args}]
  (or (number? x)
      (throw (errors/undefined-ex "is_number: %s is not a number" x))))

(defn builtin-is-string
  [{[x] :args}]
  (or (string? x)
      (throw (errors/undefined-ex "is_string: %s is not a string" x))))

(defn builtin-is-boolean
  [{[x] :args}]
  (or (boolean? x)
      (throw (errors/undefined-ex "is_boolean: %s is not a boolean" x))))

(defn builtin-is-array
  [{[x] :args}]
  (or (vector? x)
      (throw (errors/undefined-ex "is_array: %s is not an array" x))))

(defn builtin-is-set
  [{[x] :args}]
  (or (set? x)
      (throw (errors/undefined-ex "is_set: %s is not a set" x))))

(defn builtin-is-object
  [{[x] :args}]
  (or (map? x)
      (throw (errors/undefined-ex "is_object: %s is not an object" x))))

(defn builtin-is-null
  [{[x] :args}]
  (or (nil? x)
      (throw (errors/undefined-ex "is_null: %s is not null" x))))

(defn builtin-type-name
  [{[x] :args}]
  (let [type-name (types/->rego x)]
    (if (= type-name "floating-point number")
      "number"
      type-name)))

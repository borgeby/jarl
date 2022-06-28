(ns jarl.builtins.types
  (:require [jarl.exceptions :as errors]
            [jarl.types :as types]))

(defn builtin-is-number
  [{[x] :args}]
  (if (number? x)
    true
    (throw (errors/undefined-ex "is_number: %s is not a number" x))))

(defn builtin-is-string
  [{[x] :args}]
  (if (string? x)
    true
    (throw (errors/undefined-ex "is_string: %s is not a string" x))))

(defn builtin-is-boolean
  [{[x] :args}]
  (if (boolean? x)
    true
    (throw (errors/undefined-ex "is_boolean: %s is not a boolean" x))))

(defn builtin-is-array
  [{[x] :args}]
  (if (vector? x)
    true
    (throw (errors/undefined-ex "is_array: %s is not an array" x))))

(defn builtin-is-set
  [{[x] :args}]
  (if (set? x)
    true
    (throw (errors/undefined-ex "is_set: %s is not a set" x))))

(defn builtin-is-object
  [{[x] :args}]
  (if (map? x)
    true
    (throw (errors/undefined-ex "is_object: %s is not an object" x))))

(defn builtin-is-null
  [{[x] :args}]
  (if (nil? x)
    true
    (throw (errors/undefined-ex "is_null: %s is not null" x))))

(defn builtin-type-name
  [{[x] :args}]
  (let [type-name (types/java->rego x)]
    (if (= type-name "floating-point number")
      "number"
      type-name)))

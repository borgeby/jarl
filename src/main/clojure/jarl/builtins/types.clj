(ns jarl.builtins.types
  (:require [jarl.builtins.utils :refer [java->rego undefined-ex]]))

(defn builtin-is-number
  "Implementation of is_number built-in"
  {:builtin "is_number" :args-types ["any"]}
  [x]
  (if (number? x)
    true
    (throw (undefined-ex "%s is not a number" x))))

(defn builtin-is-string
  "Implementation of is_string built-in"
  {:builtin "is_string" :args-types ["any"]}
  [x]
  (if (string? x)
    true
    (throw (undefined-ex "%s is not a string" x))))

(defn builtin-is-boolean
  "Implementation of is_boolean built-in"
  {:builtin "is_boolean" :args-types ["any"]}
  [x]
  (if (boolean? x)
    true
    (throw (undefined-ex "%s is not a boolean" x))))

(defn builtin-is-array
  "Implementation of is_array built-in"
  {:builtin "is_array" :args-types ["any"]}
  [x]
  (if (vector? x)
    true
    (throw (undefined-ex "%s is not an array" x))))

(defn builtin-is-set
  "Implementation of is_set built-in"
  {:builtin "is_set" :args-types ["any"]}
  [x]
  (if (set? x)
    true
    (throw (undefined-ex "%s is not a set" x))))

(defn builtin-is-object
  "Implementation of is_object built-in"
  {:builtin "is_object" :args-types ["any"]}
  [x]
  (if (map? x)
    true
    (throw (undefined-ex "%s is not an object" x))))

(defn builtin-is-null
  "Implementation of is_null built-in"
  {:builtin "is_null" :args-types ["any"]}
  [x]
  (if (nil? x)
    true
    (throw (undefined-ex "%s is not null" x))))

(defn builtin-type-name
  "Implementation of type_name built-in"
  {:builtin "type_name" :args-types ["any"]}
  [x]
  (let [type-name (java->rego x)]
    (if (= type-name "floating-point number")
      "number"
      type-name)))

(ns jarl.builtins.types
  (:require [jarl.builtins.utils :refer [java->rego]])
  (:import (se.fylling.jarl UndefinedException)))

(defn builtin-is-number
  "Implementation of is_number built-in"
  {:builtin "is_number" :args-types ["any"]}
  [x]
  (if (number? x)
    true
    (throw (UndefinedException. (str x " is not a number")))))

(defn builtin-is-string
  "Implementation of is_string built-in"
  {:builtin "is_string" :args-types ["any"]}
  [x]
  (if (string? x)
    true
    (throw (UndefinedException. (str x " is not a string")))))

(defn builtin-is-boolean
  "Implementation of is_boolean built-in"
  {:builtin "is_boolean" :args-types ["any"]}
  [x]
  (if (boolean? x)
    true
    (throw (UndefinedException. (str x " is not a boolean")))))

(defn builtin-is-array
  "Implementation of is_array built-in"
  {:builtin "is_array" :args-types ["any"]}
  [x]
  (if (vector? x)
    true
    (throw (UndefinedException. (str x " is not an array")))))

(defn builtin-is-set
  "Implementation of is_set built-in"
  {:builtin "is_set" :args-types ["any"]}
  [x]
  (if (set? x)
    true
    (throw (UndefinedException. (str x " is not a set")))))

(defn builtin-is-object
  "Implementation of is_object built-in"
  {:builtin "is_object" :args-types ["any"]}
  [x]
  (if (map? x)
    true
    (throw (UndefinedException. (str x " is not an object")))))

(defn builtin-is-null
  "Implementation of is_null built-in"
  {:builtin "is_null" :args-types ["any"]}
  [x]
  (if (nil? x)
    true
    (throw (UndefinedException. (str x " is not null")))))

(defn builtin-type-name
  "Implementation of type_name built-in"
  {:builtin "type_name" :args-types ["any"]}
  [x]
  (let [type-name (java->rego x)]
    (if (= type-name "floating-point number")
      "number"
      type-name)))

(ns jarl.builtins.utils
  (:require [clojure.string :as str])
  (:import (se.fylling.jarl BuiltinException UndefinedException)
           (clojure.lang PersistentVector PersistentHashSet PersistentArrayMap PersistentHashMap)))

(defn builtin-ex [message & args]
  (BuiltinException. (apply format message args)))

(defn undefined-ex [message & args]
  (UndefinedException. (apply format message args)))

(defn java->rego
  "Translates provided Java type to equivalent Rego type name"
  [value]
  (if (nil? value)
    "null"
    (condp instance? value
      String "string"
      Boolean "boolean"
      Double "floating-point number"
      Float "floating-point number"
      Integer "number"
      Long "number"
      Number "number"
      PersistentVector "array"
      PersistentHashSet "set"
      PersistentArrayMap "object"
      PersistentHashMap "object"
      (str "unknown type: " (type value) " from value: " value))))

(defn possibly-int
  "present e.g. 1.0 as 1"
  [num]
  (if (zero? (mod num 1))
    (int num)
    num))

(defn str->int
  "Converts a numeric string to int, or returns the string if not a number"
  [s]
  (let [numeric? (fn [s] (some? (re-matches #"[\d]+" s)))]
    (if (numeric? s)
      (.intValue (bigdec s))
      s)))

(defn check-args
  "Check types of provided values, and ensure they match the type names provided in the function metadata"
  [builtin-meta & values]
  (let [name (:builtin builtin-meta)
        types (:args-types builtin-meta)
        operands (vec (range 1 (inc (count types))))
        zipped (mapv vector operands types values)]
    (doseq [entry zipped]
      (let [pos (first entry)
            expected-type (second entry)
            value (nth entry 2)
            provided-type (java->rego value)]
        (when-not (or (= expected-type "any") (= expected-type provided-type)
                      (and (= expected-type "number") (= provided-type "floating-point number")))
          (throw (builtin-ex "%s: operand %s must be %s but got %s" name pos expected-type provided-type)))))))

(defn type-sort-order
  "Return the sort order value for any given Rego type - lower value means higher precedence"
  [val]
  (let [rego-type (java->rego val)
        precedence-table {"null"   0 "boolean" 1
                          "number" 2 "floating-point number" 2
                          "string" 3 "array" 4
                          "object" 5 "set" 6}
        precedence (get precedence-table rego-type)]
    (if (nil? precedence)
      (throw (builtin-ex "unknown Rego type: %s" rego-type))
      precedence)))

(defn typed-seq
  "Ensure that array/set only contains allowed Rego types"
  [arr-or-set allowed-types]
  (let [allowed-set (set allowed-types)
        allow (if (contains? allowed-set "number") (conj allowed-set "floating-point number") allowed-set)
        forbidden (fn [x] (not (contains? allow (java->rego x))))]
    (when-let [violation (first (filter forbidden arr-or-set))]
      (throw (builtin-ex "operand must be array or set of %s but got array or set containing %s"
                         (str/join "," allowed-types)
                         (java->rego violation))))))

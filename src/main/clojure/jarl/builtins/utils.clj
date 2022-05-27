(ns jarl.builtins.utils
  (:require [clojure.string :as str]
            [jarl.exceptions :as errors]
            [jarl.types :as types]))


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
            provided-type (types/java->rego value)]
        (when-not (or (= expected-type "any") (= expected-type provided-type)
                      (and (= expected-type "number") (= provided-type "floating-point number")))
          (throw (errors/builtin-ex "%s: operand %s must be %s but got %s" name pos expected-type provided-type)))))))

(defn typed-seq
  "Ensure that array/set only contains allowed Rego types"
  [arr-or-set allowed-types]
  (let [allowed-set (set allowed-types)
        allow (if (contains? allowed-set "number") (conj allowed-set "floating-point number") allowed-set)
        forbidden (fn [x] (not (contains? allow (types/java->rego x))))]
    (when-let [violation (first (filter forbidden arr-or-set))]
      (throw (errors/builtin-ex "operand must be array or set of %s but got array or set containing %s"
                                (str/join "," allowed-types)
                                (types/java->rego violation))))))

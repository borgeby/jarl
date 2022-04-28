(ns jarl.builtins.utils
  (:import (se.fylling.jarl BuiltinException)
           (clojure.lang PersistentVector)))

(defn java->rego
  "Translates provided Java type to equivalent Rego type name"
  [value]
  (condp instance? value
    String "string"
    Boolean "boolean"
    Double "floating-point number"
    Float "floating-point number"
    Integer "number"
    Long "number"
    Number "number"
    PersistentVector "array"
    (str "unknown type: " (type value) " from value: " value)))

(defn check-args
  "Check types of provided values, and ensure they match the type names provided in the function metadata"
  [builtin-meta & values]
  (let [name (:builtin builtin-meta)
        types (:args-types builtin-meta)
        operands (vec (range 1 (inc (count types))))
        zipped (vec (map vector operands types values))]
    (doseq [entry zipped]
      (let [pos (first entry)
            expected-type (second entry)
            value (nth entry 2)
            provided-type (java->rego value)]
        (when-not (= expected-type provided-type)
          (throw (BuiltinException.
                   (format "%s: operand %s must be %s but got %s", name, pos, expected-type, provided-type))))))))

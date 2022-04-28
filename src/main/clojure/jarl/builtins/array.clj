(ns jarl.builtins.array
  (:import (se.fylling.jarl BuiltinException)
           (clojure.lang PersistentVector)))

; TODO: Move somewhere else for reuse
(defn java->rego [value]
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

(defn builtin-concat [a b]
  (when-not (vector? a)
    (throw (BuiltinException. (format "array.concat: operand 1 must be array but got %s", (java->rego a)))))
  (when-not (vector? b)
    (throw (BuiltinException. (format "array.concat: operand 2 must be array but got %s", (java->rego b)))))
  (concat a b))

(defn builtin-reverse [arr]
  (when-not (vector? arr)
    (throw (BuiltinException. (format "array.reverse: operand 1 must be array but got %s", (java->rego arr)))))
  (reverse arr))

(defn builtin-slice [arr start stop]
  (when-not (vector? arr)
    (throw (BuiltinException. (format "array.slice: operand 1 must be array but got %s", (java->rego arr)))))
  (when-not (int? start)
    (throw (BuiltinException. (format "array.slice: operand 2 must be number but got %s", (java->rego start)))))
  (when-not (int? stop)
    (throw (BuiltinException. (format "array.slice: operand 3 must be number but got %s", (java->rego stop)))))
  (let [start (max start 0)
        stop (min stop (count arr))]
    (if (> start stop)
      []
      (subvec arr start stop))))

(ns jarl.builtins.bits
  (:require [jarl.exceptions :as errors]
            [jarl.types :as types]))

(defn- non-integer? [num]
  (not (zero? (mod num 1))))

(defn- ensure-pos [builtin-name a b]
  (when (neg? a)
    (throw (errors/type-ex "%s: operand 1 must be an unsigned integer number but got a negative %s"
                           builtin-name
                           (if (integer? a) "integer" (types/java->rego a)))))
  (when (neg? b)
    (throw (errors/type-ex "%s: operand 2 must be an unsigned integer number but got a negative %s"
                           builtin-name
                           (if (integer? b) "integer" (types/java->rego b))))))

(defn- ensure-ints [builtin-name a b]
  (when (non-integer? a)
    (throw (errors/type-ex "%s: operand 1 must be integer number but got %s"
                           builtin-name (types/java->rego a))))
  (when (non-integer? b)
    (throw (errors/type-ex "%s: operand 2 must be integer number but got %s"
                           builtin-name (types/java->rego b)))))

(defn- ensure-pos-ints [builtin-name a b]
  (ensure-ints builtin-name a b)
  (ensure-pos  builtin-name a b))

(defn builtin-bits-or
  [{[a b] :args}]
  (ensure-ints "bits.or" a b)
  (bit-or a b))

(defn builtin-bits-and
  [{[a b] :args}]
  (ensure-ints "bits.and" a b)
  (bit-and a b))

(defn builtin-bits-negate
  [{[a] :args}]
  (when (non-integer? a)
    (throw (errors/type-ex "eval_type_error: bits.negate: operand 1 must be integer number but got %s"
                           (types/java->rego a))))
  (bit-not a))

(defn builtin-bits-xor
  [{[a b] :args}]
  (ensure-ints "bits.xor" a b)
  (bit-xor a b))

(defn builtin-bits-lsh
  [{[a b] :args}]
  (ensure-pos-ints "bits.lsh" a b)
  (.shiftLeft (biginteger a) b))

(defn builtin-bits-rsh
  [{[a b] :args}]
  (ensure-pos-ints "bits.rsh" a b)
  (.shiftRight (biginteger a) b))

(ns jarl.builtins.bits
  (:require [jarl.exceptions :as errors]
            [jarl.types :as types]))

(defn- non-integer? [num]
  (not (zero? (mod num 1))))

(defn- ensure-pos [builtin-name a b]
  (when (neg? a)
    (throw (errors/undefined-ex "eval_type_error: %s: operand 1 must be an unsigned integer number but got a negative %s"
                         builtin-name
                         (types/java->rego a))))
  (when (neg? b)
    (throw (errors/undefined-ex "eval_type_error: %s: operand 2 must be an unsigned integer number but got a negative %s"
                         builtin-name
                         (types/java->rego b)))))

(defn- ensure-ints [builtin-name a b]
  (when (non-integer? a)
    (throw (errors/undefined-ex "eval_type_error: %s: operand 1 must be integer number but got %s"
                         builtin-name
                         (types/java->rego a))))
  (when (non-integer? b)
    (throw (errors/undefined-ex "eval_type_error: %s: operand 2 must be integer number but got %s"
                         builtin-name
                         (types/java->rego b)))))

(defn- ensure-pos-ints [builtin-name a b]
  (ensure-ints builtin-name a b)
  (ensure-pos  builtin-name a b))

(defn builtin-bits-or
  "Implementation of bits.or built-in"
  {:builtin "bits.or" :args-types ["number" "number"]}
  [a b]
  (ensure-ints "bits.or" a b)
  (bit-or a b))

(defn builtin-bits-and
  "Implementation of bits.and built-in"
  {:builtin "bits.and" :args-types ["number" "number"]}
  [a b]
  (ensure-ints "bits.and" a b)
  (bit-and a b))

(defn builtin-bits-negate
  "Implementation of bits.negate built-in"
  {:builtin "bits.negate" :args-types ["number"]}
  [a]
  (when (non-integer? a)
    (throw (errors/undefined-ex "eval_type_error: bits.negate: operand 1 must be integer number but got %s"
                                (types/java->rego a))))
  (bit-not a))

(defn builtin-bits-xor
  "Implementation of bits.xor built-in"
  {:builtin "bits.xor" :args-types ["number" "number"]}
  [a b]
  (ensure-ints "bits.xor" a b)
  (bit-xor a b))

(defn builtin-bits-lsh
  "Implementation of bits.lsh built-in"
  {:builtin "bits.lsh" :args-types ["number" "number"]}
  [a b]
  (ensure-pos-ints "bits.lsh" a b)
  (bit-shift-left a b))

(defn builtin-bits-rsh
  "Implementation of bits.rsh built-in"
  {:builtin "bits.rsh" :args-types ["number" "number"]}
  [a b]
  (ensure-pos-ints "bits.rsh" a b)
  (bit-shift-right a b))

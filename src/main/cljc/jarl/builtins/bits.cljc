(ns jarl.builtins.bits
  (:require [jarl.exceptions :as errors]
            [jarl.types :as types]
            #?(:cljs [jarl.utils :as utils])))

(defn- non-integer? [num]
  (not (zero? (mod num 1))))

(defn- ensure-pos [a b]
  (when (neg? a)
    (throw (errors/type-ex "operand 1 must be an unsigned integer number but got a negative %s"
                           (if (integer? a) "integer" (types/->rego a)))))
  (when (neg? b)
    (throw (errors/type-ex "operand 2 must be an unsigned integer number but got a negative %s"
                           (if (integer? b) "integer" (types/->rego b))))))

(defn- ensure-ints [a b]
  (when (non-integer? a)
    (throw (errors/type-ex "operand 1 must be integer number but got %s" (types/->rego a))))
  (when (non-integer? b)
    (throw (errors/type-ex "operand 2 must be integer number but got %s" (types/->rego b)))))

(defn- ensure-pos-ints [a b]
  (ensure-ints a b)
  (ensure-pos  a b))

(defn builtin-bits-or
  [{[a b] :args}]
  (ensure-ints a b)
  (bit-or a b))

(defn builtin-bits-and
  [{[a b] :args}]
  (ensure-ints a b)
  (bit-and a b))

(defn builtin-bits-negate
  [{[a] :args}]
  (when (non-integer? a)
    (throw (errors/type-ex "operand 1 must be integer number but got %s" (types/->rego a))))
  (bit-not a))

(defn builtin-bits-xor
  [{[a b] :args}]
  (ensure-ints a b)
  (bit-xor a b))

(defn builtin-bits-lsh
  [{[a b] :args}]
  (ensure-pos-ints a b)
  #?(:clj  (.shiftLeft (biginteger a) b)
     :cljs (utils/bigint->number (bit-shift-left (js/BigInt a) (js/BigInt b)))))

(defn builtin-bits-rsh
  [{[a b] :args}]
  (ensure-pos-ints a b)
  #?(:clj  (.shiftRight (biginteger a) b)
     :cljs (utils/bigint->number (bit-shift-right (js/BigInt a) (js/BigInt b)))))

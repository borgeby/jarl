(ns jarl.builtins.numbers
  (:require #?(:clj  [clojure.math :as math]
               :cljs [cljs.math    :as math])
            [jarl.exceptions :as errors]
            [jarl.builtins.utils :refer [possibly-int]]
            [jarl.types :as types]
            [clojure.set :as set]))

(defn builtin-plus
  [{[a b] :args}]
  (possibly-int #?(:clj (+' a b) :cljs (+ a b))))

(defn builtin-minus
  [{[a b] :args}]
  (if (and (set? a) (set? b))
    (set/difference a b)
    (if (and (number? a) (number? b))
      (possibly-int #?(:clj (-' a b) :cljs (- a b)))
      (throw (errors/type-ex "operand 2 must be %s but got %s" (types/->rego a) (types/->rego b))))))

(defn builtin-mul
  [{[a b] :args}]
  (possibly-int #?(:clj (*' a b) :cljs (* a b))))

(defn builtin-div
  [{[a b] :args}]
  (if (zero? b)
    (throw (errors/builtin-ex "divide by zero"))
    (possibly-int (double (/ a b)))))

(defn builtin-rem
  [{[a b] :args}]
  (when (zero? b)
    (throw (errors/builtin-ex "modulo by zero")))
  (when (or (types/non-int-float? a) (types/non-int-float? b))
    (throw (errors/builtin-ex "modulo on floating-point number")))
  (let [res (rem a b)]
    (if (types/non-int-float? res)
      ; OPA returns undefined for rem resulting in floating point numbers
      (throw (errors/builtin-ex "remainder is not an integer"))
      (possibly-int res))))

(defn builtin-round
  [{[x] :args}]
  (cond
    (int? x) x
    (double? x) (math/round ^Double x)
    (float? x) (math/round ^Float x)))

(defn builtin-ceil
  [{[x] :args}]
  (cond
    (int? x) x
    (double? x) (math/round (math/ceil ^Double x))
    (float? x) (math/round (math/ceil ^Float x))))

(defn builtin-floor
  [{[x] :args}]
  (cond
    (int? x) x
    (double? x) (math/round (math/floor ^Double x))
    (float? x) (math/round (math/floor ^Float x))))

(defn builtin-abs
  [{[x] :args}]
  (abs x))

(defn builtin-numbers-range
  [{[a b] :args}]
  (if-not (and (integer? a) (integer? b))
    (throw (errors/type-ex "operand %d must be integer number but got floating-point number"
                           (if (integer? a) 2 1)))
    (if (> a b)
      (rseq (vec (range b (inc a))))
      (vec (range a (inc b))))))

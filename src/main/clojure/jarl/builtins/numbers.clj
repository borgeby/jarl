(ns jarl.builtins.numbers
  (:require [jarl.exceptions :as errors]
            [jarl.builtins.utils :refer [possibly-int]]
            [jarl.types :as types]
            [clojure.set :as set]))

(defn builtin-plus
  [{[a b] :args}]
  (possibly-int (+' a b)))

(defn builtin-minus
  [{[a b] :args}]
  ; Disable args check until we have a way to express union types
  (if (and (set? a) (set? b))
    (set/difference a b)
    (if (and (number? a) (number? b))
      (possibly-int (-' a b))
      (throw (errors/type-ex "minus: operand 2 must be %s but got %s" (types/->rego a) (types/->rego b))))))

(defn builtin-mul
  [{[a b] :args}]
  (possibly-int (*' a b)))

(defn builtin-div
  [{[a b] :args}]
  (if (zero? b)
    (throw (errors/builtin-ex "div: divide by zero"))
    (possibly-int (double (/ a b)))))

(defn builtin-rem
  [{[a b] :args}]
  (when (zero? b)
    (throw (errors/builtin-ex "modulo by zero")))
  (when (or (types/non-int-float? a) (types/non-int-float? b))
    (throw (errors/builtin-ex "rem: modulo on floating-point number")))
  (let [res (rem a b)]
    (if (types/non-int-float? res)
      ; OPA returns undefined for rem resulting in floating point numbers
      (throw (errors/builtin-ex "rem: remainder is not an integer"))
      (possibly-int res))))

(defn builtin-round
  [{[x] :args}]
  (cond
    (int? x) x
    (double? x) (Math/round ^Double x)
    (float? x) (Math/round ^Float x)))

(defn builtin-ceil
  [{[x] :args}]
  (cond
    (int? x) x
    (double? x) (Math/round (Math/ceil ^Double x))
    (float? x) (Math/round (Math/ceil ^Float x))))

(defn builtin-floor
  [{[x] :args}]
  (cond
    (int? x) x
    (double? x) (Math/round (Math/floor ^Double x))
    (float? x) (Math/round (Math/floor ^Float x))))

(defn builtin-abs
  [{[x] :args}]
  ; Clojure 1.11 has `abs` in core, but lein clj-kondo seems to depend on an older version, which fails
  ; on calling abs as "unresolved". I've asked the maintainers to have that bumped, and will replace this once fixed.
  (let [abs (fn [n] (max n (-' n)))]
    (abs x)))

(defn builtin-numbers-range
  [{[a b] :args}]
  (if-not (and (integer? a) (integer? b))
    (throw (errors/type-ex "numbers.range: operand %d must be integer number but got floating-point number"
                           (if (integer? a) 2 1)))
    (if (> a b)
      (rseq (vec (range b (inc a))))
      (vec (range a (inc b))))))

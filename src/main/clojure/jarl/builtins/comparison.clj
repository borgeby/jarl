(ns jarl.builtins.comparison
  (:require [jarl.builtins.utils :refer [type-sort-order]]))

; From the OPA go docs on ast.Compare:
;
; If a is less than b, the return value is negative. If a is greater than b,
; the return value is positive. If a is equal to b, the return value is zero.
;
; Different types are never equal to each other. For comparison purposes, types
; are sorted as follows:
;
; nil < Null < Boolean < Number < String < Var < Ref < Array < Object < Set <
; ArrayComprehension < ObjectComprehension < SetComprehension < Expr < SomeDecl
; < With < Body < Rule < Import < Package < Module.

(declare rego-compare)

(defn- first-diff-pair [a b]
  (let [pairs (partition 2 (interleave a b))
        diff-pairs (filter #(not= (first %) (second %)) pairs)]
    (first diff-pairs)))

; Arrays and Refs are equal if and only if both a and b have the same length
; and all corresponding elements are equal. If one element is not equal, the
; return value is the same as for the first differing element. If all elements
; are equal but a and b have different lengths, the shorter is considered less
; than the other.
(defn- vector-compare [a b]
  (let [ca (count a)
        cb (count b)]
    (if (= ca cb)
      (let [[x y] (first-diff-pair a b)]
        (rego-compare x y))
      (let [[x y] (first-diff-pair a b)]
        (if (nil? x)
          (compare ca cb)
          (rego-compare x y))))))

; Objects are considered equal if and only if both a and b have the same sorted
; (key, value) pairs and are of the same length. Other comparisons are
; consistent but not defined.
(defn- map-compare [a b]
  (let [sa (into (sorted-map) a)
        sb (into (sorted-map) b)
        keys-comp (vector-compare (keys sa) (keys sb))]
    (if-not (zero? keys-comp)
      keys-comp
      (let [values-comp (vector-compare (vals sa) (vals sb))]
        (if (zero? values-comp)
          (let [ca (count sa)
                cb (count sb)]
            (if (= ca cb)
              0
              (if (< ca cb)
                -1
                1)))
          values-comp)))))

; Sets are considered equal if and only if the symmetric difference of a and b
; is empty.
; Other comparisons are consistent but not defined.
(defn- set-compare [a b]
  (let [vc (vector-compare a b)]
    (if-not (zero? vc)
      vc
      (if (< (count a) (count b))
        -1
        1))))

(defn rego-compare [a b]
  (cond
    (= a b) 0
    (and (number? a) (number? b)) (compare (double a) (double b))
    (not= (class a) (class b))    (compare (type-sort-order a) (type-sort-order b))
    (and (vector? a) (vector? b)) (vector-compare a b)
    (and (map? a) (map? b))       (map-compare a b)
    (and (set? a) (set? b))       (set-compare a b)
    :else                         (compare a b)))

(defn builtin-equal
  "Implementation of equal (==) built-in"
  {:builtin "==" :args-types ["any" "any"]}
  [a b]
  (zero? (rego-compare a b)))

(defn builtin-neq
  "Implementation of neq (!=) built-in"
  {:builtin "!=" :args-types ["any" "any"]}
  [a b]
  (not (zero? (rego-compare a b))))

(defn builtin-lt
  "Implementation of lt (<) built-in"
  {:builtin "<" :args-types ["any" "any"]}
  [a b]
  (neg? (rego-compare a b)))

(defn builtin-lte
  "Implementation of lte (<=) built-in"
  {:builtin "<=" :args-types ["any" "any"]}
  [a b]
  (let [rc (rego-compare a b)]
    (or (neg? rc) (zero? rc))))

(defn builtin-gt
  "Implementation of gt (>) built-in"
  {:builtin "<" :args-types ["any" "any"]}
  [a b]
  (pos? (rego-compare a b)))

(defn builtin-gte
  "Implementation of gte (>=) built-in"
  {:builtin ">=" :args-types ["any" "any"]}
  [a b]
  (let [rc (rego-compare a b)]
    (or (pos? rc) (zero? rc))))

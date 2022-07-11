(ns jarl.types
  (:require [jarl.exceptions :as errors])
  #?(:clj (:import (clojure.lang BigInt))))

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

#_:clj-kondo/ignore
(defn bigint? [x]
  #?(:clj  (instance? BigInt x)
     :cljs false)) ; TODO

(defn non-int-float? [x]
  (and (number? x)
       (not (zero? (mod x 1)))))

(defn ->rego
  "Translates provided type from host to equivalent Rego type name"
  [value]
  (cond
    (nil?            value) "null"
    (string?         value) "string"
    (non-int-float?  value) "floating-point number"
    (number?         value) "number"
    (boolean?        value) "boolean"
    (vector?         value) "array"
    (set?            value) "set"
    (map?            value) "object"
    :else                   (str "unknown type: " (type value) " from value: " value)))


(defn type-sort-order
  "Return the sort order value for any given Rego type - lower value means higher precedence"
  [val]
  (let [rego-type (->rego val)
        precedence-table {"null"   0 "boolean" 1
                          "number" 2 "floating-point number" 2
                          "string" 3 "array" 4
                          "object" 5 "set" 6}
        precedence (get precedence-table rego-type)]
    (if (nil? precedence)
      (throw (errors/builtin-ex "unknown Rego type: %s" rego-type))
      precedence)))

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
; is empty. Other comparisons are consistent but not defined.
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
    (every? bigint? [a b]) (compare a b)
    (every? number? [a b]) (compare (double a) (double b))
    (every? vector? [a b]) (vector-compare a b)
    (every? map?    [a b]) (map-compare a b)
    (every? set?    [a b]) (set-compare a b)

    (not= (type a) (type b)) (compare (type-sort-order a) (type-sort-order b))
    :else                    (compare a b)))

(defn rego-equal? [a b]
  (zero? (rego-compare a b)))

(ns jarl.builtins.aggregates
  (:require [jarl.exceptions :as errors]
            [jarl.types :as types]
            [jarl.builtins.strings :refer [cp-count]]
            [jarl.builtins.utils :refer [possibly-int typed-seq]]))

(defn builtin-count
  [{[x] :args}]
  (if (string? x)
    (cp-count {:args [x]})
    (count x)))

(defn builtin-sum
  [{[coll] :args}]
  (typed-seq "sum" coll #{"number"})
  (possibly-int (apply + coll)))

(defn builtin-product
  [{[coll] :args}]
  (typed-seq "product" coll #{"number"})
  (possibly-int (apply * coll)))

(defn builtin-max
  [{[coll] :args}]
  (if (empty? coll)
    (throw (errors/undefined-ex "max on empty collection"))
    (last (sort types/rego-compare coll))))

(defn builtin-min
  [{[coll] :args}]
  (if (empty? coll)
    (throw (errors/undefined-ex "min on empty collection"))
    (first (sort types/rego-compare coll))))

(defn builtin-sort
  [{[coll] :args}]
  (sort types/rego-compare coll))

(defn seq-contains? [coll target]
  (or (some #(= target %) coll) false))

(defn in
  ([x coll]
    (cond
      (set?    coll) (contains? coll x)
      (vector? coll) (seq-contains? coll x)
      (map?    coll) (seq-contains? (vals coll) x)
      :else           false))
  ([k v coll]
   ; sets aren't supported by this construct
   (= v (get coll k :not-found))))

; x in coll
(defn builtin-internal-member-2
  [{[x coll] :args}]
  (in x coll))

; x, y in coll
(defn builtin-internal-member-3
  [{[x y coll] :args}]
  (in x y coll))

; Deprecated

(defn builtin-all
  [{[coll] :args}]
  (every? true? coll))

(defn builtin-any
  [{[coll] :args}]
  (or (some true? coll) false))

(ns jarl.builtins.aggregates
  (:require [jarl.exceptions :as errors]
            [jarl.types :as types]
            [jarl.builtins.strings :refer [cp-count]]
            [jarl.builtins.utils :refer [check-args possibly-int typed-seq]]))

(defn builtin-count
  "Implementation of count built-in"
  {:builtin "count" :args-types [#{"string", "object", "set", "array"}]}
  [{[x] :args}]
  (check-args (meta #'builtin-count) x)
  (if (string? x)
    (cp-count {:args [x]})
    (count x)))

(defn builtin-sum
  "Implementation of sum built-in"
  {:builtin "sum" :args-types ["any"]}
  [{[coll] :args}]
  (typed-seq "sum" coll #{"number"})
  (possibly-int (apply + coll)))

(defn builtin-product
  "Implementation of product built-in"
  {:builtin "product" :args-types ["any"]}
  [{[coll] :args}]
  (typed-seq "product" coll #{"number"})
  (possibly-int (apply * coll)))

(defn builtin-max
  "Implementation of max built-in"
  {:builtin "max" :args-types ["any"]}
  [{[coll] :args}]
  (if (empty? coll)
    (throw (errors/undefined-ex "max on empty collection"))
    (last (sort types/rego-compare coll))))

(defn builtin-min
  "Implementation of min built-in"
  {:builtin "min" :args-types ["any"]}
  [{[coll] :args}]
  (if (empty? coll)
    (throw (errors/undefined-ex "min on empty collection"))
    (first (sort types/rego-compare coll))))

(defn builtin-sort
  "Implementation of sort built-in"
  {:builtin "sort" :args-types ["any"]}
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
  "Implementation of internal.member_2 built-in"
  {:builtin "internal.member_2" :args-types ["any" "any"]}
  [{[x coll] :args}]
  (in x coll))

; x, y in coll
(defn builtin-internal-member-3
  "Implementation of internal.member_3 built-in"
  {:builtin "internal.member_3" :args-types ["any" "any", "any"]}
  [{[x y coll] :args}]
  (in x y coll))

(defn -main []
  (println (some #(= {"foo" {"baz" 2000}} %) [{"foo" {"baz" 2000}}])))

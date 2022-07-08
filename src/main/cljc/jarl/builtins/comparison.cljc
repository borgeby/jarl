(ns jarl.builtins.comparison
  (:require [jarl.types :as types]))

(defn builtin-equal
  [{[a b] :args}]
  (zero? (types/rego-compare a b)))

(defn builtin-neq
  [{[a b] :args}]
  (not (zero? (types/rego-compare a b))))

(defn builtin-lt
  [{[a b] :args}]
  (neg? (types/rego-compare a b)))

(defn builtin-lte
  [{[a b] :args}]
  (let [rc (types/rego-compare a b)]
    (or (neg? rc) (zero? rc))))

(defn builtin-gt
  [{[a b] :args}]
  (pos? (types/rego-compare a b)))

(defn builtin-gte
  [{[a b] :args}]
  (let [rc (types/rego-compare a b)]
    (or (pos? rc) (zero? rc))))

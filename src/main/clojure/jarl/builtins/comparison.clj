(ns jarl.builtins.comparison
  (:require [jarl.types :as types]))

(defn builtin-equal
  "Implementation of equal (==) built-in"
  {:builtin "==" :args-types ["any" "any"]}
  [a b]
  (zero? (types/rego-compare a b)))

(defn builtin-neq
  "Implementation of neq (!=) built-in"
  {:builtin "!=" :args-types ["any" "any"]}
  [a b]
  (not (zero? (types/rego-compare a b))))

(defn builtin-lt
  "Implementation of lt (<) built-in"
  {:builtin "<" :args-types ["any" "any"]}
  [a b]
  (neg? (types/rego-compare a b)))

(defn builtin-lte
  "Implementation of lte (<=) built-in"
  {:builtin "<=" :args-types ["any" "any"]}
  [a b]
  (let [rc (types/rego-compare a b)]
    (or (neg? rc) (zero? rc))))

(defn builtin-gt
  "Implementation of gt (>) built-in"
  {:builtin "<" :args-types ["any" "any"]}
  [a b]
  (pos? (types/rego-compare a b)))

(defn builtin-gte
  "Implementation of gte (>=) built-in"
  {:builtin ">=" :args-types ["any" "any"]}
  [a b]
  (let [rc (types/rego-compare a b)]
    (or (pos? rc) (zero? rc))))

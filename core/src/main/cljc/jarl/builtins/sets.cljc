(ns jarl.builtins.sets
  (:require [clojure.set :as set]))

(defn builtin-and
  [{[a b] :args}]
  (set/intersection a b))

(defn builtin-or
  [{[a b] :args}]
  (set/union a b))

; NOTE: the set difference function implemented in jarl.builtins.numbers/builtin-minus

(defn builtin-intersection
  [{[s] :args}]
  (if (empty? s)
    s
    (apply set/intersection s)))

(defn builtin-union
  [{[s] :args}]
  (apply set/union s))

; deprecated
(defn builtin-set-diff
  [{[a b] :args}]
  (set/difference a b))

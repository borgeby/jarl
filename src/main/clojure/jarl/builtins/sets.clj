(ns jarl.builtins.sets
  (:require [jarl.builtins.utils :refer [check-args]]
            [clojure.set :as set]))

(defn builtin-and
  "Implementation of and (&) built-in"
  {:builtin "and" :args-types ["set" "set"]}
  [a b]
  (check-args (meta #'builtin-and) a b)
  (set/intersection a b))

(defn builtin-or
  "Implementation of or (|) built-in"
  {:builtin "or" :args-types ["set" "set"]}
  [a b]
  (check-args (meta #'builtin-or) a b)
  (set/union a b))

; NOTE: the set difference function implemented in jarl.builtins.numbers/builtin-minus

(defn builtin-intersection
  "Implementation of intersection built-in"
  {:builtin "intersection" :args-types ["set"]}
  [s]
  (check-args (meta #'builtin-intersection) s)
  (if (empty? s)
    s
    (apply set/intersection s)))

(defn builtin-union
  "Implementation of union built-in"
  {:builtin "union" :args-types ["set"]}
  [s]
  (check-args (meta #'builtin-union) s)
  (apply set/union s))

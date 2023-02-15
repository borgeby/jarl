(ns jarl.builtins.array
  (:require [jarl.exceptions :as errors]
            [jarl.types :as types]))

(defn builtin-array-concat
  [{[a b] :args}]
  (concat a b))

(defn builtin-array-reverse
  [{[arr] :args}]
  (vec (reverse arr)))

(defn builtin-array-slice
  [{[arr start stop] :args}]
  (cond
    (not (int? start)) (throw (errors/type-ex "operand 2 must be integer but got %s" (types/->rego start)))
    (not (int? stop))  (throw (errors/type-ex "operand 3 must be integer but got %s" (types/->rego stop)))
    :else (let [start (max start 0)
                stop (min stop (count arr))]
            (if (> start stop)
              []
              (subvec arr start stop)))))

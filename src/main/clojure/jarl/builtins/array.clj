(ns jarl.builtins.array
  (:require [jarl.exceptions :as errors]
            [jarl.types :as types]
            [jarl.builtins.utils :refer [check-args]]))

(defn builtin-concat
  "Implementation of array.concat built-in"
  {:builtin "array.concat" :args-types ["array" "array"]}
  [a b]
  (check-args (meta #'builtin-concat) a b)
  (concat a b))

(defn builtin-reverse
  "Implementation of array.reverse built-in"
  {:builtin "array.reverse" :args-types ["array"]}
  [arr]
  (check-args (meta #'builtin-reverse) arr)
  (reverse arr))

(defn builtin-slice
  "Implementation of array.slice built-in"
  {:builtin "array.slice" :args-types ["array", "number", "number"]}
  [arr start stop]
  (check-args (meta #'builtin-slice) arr start stop)
  (let [start (max start 0)
        stop (min stop (count arr))]
    (cond
      (not (int? start)) (throw (errors/builtin-ex "array.slice: operand 2 must be integer but got %s" (types/java->rego start)))
      (not (int? stop)) (throw (errors/builtin-ex "array.slice: operand 3 must be integer but got %s" (types/java->rego stop)))
      :else (if (> start stop)
              []
              (subvec arr start stop)))))

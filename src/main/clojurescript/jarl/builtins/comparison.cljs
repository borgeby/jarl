(ns jarl.builtins.comparison)

(defn builtin-equal
  [{[a b] :args}]
  (= a b))

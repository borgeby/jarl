(ns jarl.builtins.registry
  (:require [jarl.builtins.array :as array]))

(def builtins {
               "array.concat" array/builtin-concat
               "array.reverse" array/builtin-reverse
               "array.slice" array/builtin-slice
               })

(defn get-builtin [name]
  (get builtins name))

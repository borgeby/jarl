(ns jarl.builtins.glob
  (:require [clojure.string :as str]))

(defn builtin-glob-quote-meta
  [{[pattern] :args}]
  (str/replace pattern #"\*|\?|\\\|\[|\]|\{|\}" (fn [s] (str "\\" s))))

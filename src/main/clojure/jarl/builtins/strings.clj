(ns jarl.builtins.strings
  (:require [jarl.builtins.utils :refer [builtin-ex check-args]]
            [clojure.string :as str])
  (:import (java.util.regex Pattern)))

(defn- trim-left [s cutset]
  (let [s-vec (str/split s #"")
        chars (set (str/split cutset #""))]
    (str/join "" (drop-while #(contains? chars %) s-vec))))

(defn- trim-right [s cutset]
  (let [s-vec (str/split (str/reverse s) #"")
        chars (set (str/split cutset #""))]
    (str/reverse (str/join "" (drop-while #(contains? chars %) s-vec)))))

(defn builtin-contains
  "Implementation of contains built-in"
  {:builtin "contains" :args-types ["string" "string"]}
  [^String s ^String search]
  (check-args (meta #'builtin-contains) s search)
  (.contains s search))

(defn builtin-endswith
  "Implementation of endswith built-in"
  {:builtin "endswith" :args-types ["string" "string"]}
  [s search]
  (check-args (meta #'builtin-endswith) s search)
  (str/ends-with? s search))

(defn builtin-format-int
  "Implementation of format_int built-in"
  {:builtin "format_int" :args-types ["number" "number"]}
  [number base]
  (check-args (meta #'builtin-format-int) number base)
  (Integer/toString number base))

(defn builtin-indexof
  "Implementation of indexof built-in"
  {:builtin "indexof" :args-types ["string" "string"]}
  [s search]
  (check-args (meta #'builtin-indexof) s search)
  (let [result (str/index-of s search)]
    (if (nil? result)
      -1
      result)))

(defn builtin-indexof-n
  "Implementation of indexof_n built-in"
  {:builtin "indexof_n" :args-types ["string" "string"]}
  [s search]
  (check-args (meta #'builtin-indexof-n) s search)
  (loop [index (str/index-of s search)
         indices []]
    (if (nil? index)
      indices
      (recur (str/index-of s search (inc index)) (conj indices index)))))

(defn builtin-lower
  "Implementation of lower built-in"
  {:builtin "lower" :args-types ["string"]}
  [^String s]
  (check-args (meta #'builtin-lower) s)
  (.toLowerCase s))

(defn builtin-replace
  "Implementation of replace built-in"
  {:builtin "replace" :args-types ["string" "string" "string"]}
  [s old new]
  (check-args (meta #'builtin-replace) s old new)
  (str/replace s old new))

(defn builtin-strings-reverse
  "Implementation of strings.reverse built-in"
  {:builtin "strings.reverse" :args-types ["string"]}
  [s]
  (check-args (meta #'builtin-strings-reverse) s)
  (str/reverse s))

(defn builtin-split
  "Implementation of split built-in"
  {:builtin "split" :args-types ["string" "string"]}
  [s delim]
  (check-args (meta #'builtin-split) s)
  (if (= s delim)
    ; Rego quirk? Not sure why this is so, or if there are more cases like this,
    ; but we'll try to mimic this here as well
    ["" ""]
    (str/split s (re-pattern (Pattern/quote delim)))))

(defn builtin-startswith
  "Implementation of startswith built-in"
  {:builtin "startswith" :args-types ["string" "string"]}
  [s search]
  (check-args (meta #'builtin-startswith) s search)
  (str/starts-with? s search))

; Note: Rego `substring` uses length as the second argument, while
; Clojure `subs` uses the position in the string
(defn builtin-substring
  "Implementation of substring built-in"
  {:builtin "substring" :args-types ["string" "number" "number"]}
  [s start len]
  (check-args (meta #'builtin-substring) s start len)
  (if (neg-int? start)
    (throw (builtin-ex "eval_builtin_error: substring: negative offset"))
    (let [end (+ start len)]
      (if (or (> end (count s)) (neg-int? len))
        (subs s start (count s))
        (subs s start end)))))

(defn builtin-trim
  "Implementation of trim built-in"
  {:builtin "trim" :args-types ["string" "string"]}
  [s cutset]
  (check-args (meta #'builtin-trim) s cutset)
  (-> s (trim-left cutset) (trim-right cutset)))

(defn builtin-trim-left
  "Implementation of trim_left built-in"
  {:builtin "trim_left" :args-types ["string" "string"]}
  [s cutset]
  (check-args (meta #'builtin-trim-left) s cutset)
  (trim-left s cutset))

(defn builtin-trim-prefix
  "Implementation of trim_prefix built-in"
  {:builtin "trim_prefix" :args-types ["string" "string"]}
  [s prefix]
  (check-args (meta #'builtin-trim-prefix) s prefix)
  (if (str/starts-with? s prefix)
    (subs s (count prefix))
    s))

(defn builtin-trim-right
  "Implementation of trim_right built-in"
  {:builtin "trim_right" :args-types ["string" "string"]}
  [s cutset]
  (check-args (meta #'builtin-trim-right) s cutset)
  (trim-right s cutset))

(defn builtin-trim-suffix
  "Implementation of trim_suffix built-in"
  {:builtin "trim_suffix" :args-types ["string" "string"]}
  [s suffix]
  (check-args (meta #'builtin-trim-suffix) s suffix)
  (if (str/ends-with? s suffix)
    (subs s 0 (- (count s) (count suffix)))
    s))

(defn builtin-trim-space
  "Implementation of trim_space built-in"
  {:builtin "trim_space" :args-types ["string"]}
  [s]
  (check-args (meta #'builtin-trim-space) s)
  (str/trim s))

(defn builtin-upper
  "Implementation of upper built-in"
  {:builtin "upper" :args-types ["string"]}
  [^String s]
  (check-args (meta #'builtin-upper) s)
  (.toUpperCase s))

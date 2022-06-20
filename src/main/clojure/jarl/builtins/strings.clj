(ns jarl.builtins.strings
  (:require [jarl.exceptions :as errors]
            [jarl.builtins.utils :refer [check-args]]
            [clojure.string :as str])
  (:import (java.util.regex Pattern)
           (java.util Locale)
           (clojure.lang PersistentVector)))

(defn cp-count
  "Count using code points rather than characters"
  [{[^String s] :args}]
  (.codePointCount s 0 (.length s)))

(defn- cp-seq
  "Return seq of codepoints (ints) from string"
  [^String s]
  (iterator-seq (.iterator (.codePoints s))))

(defn- cp-seq->str
  "Turn sequence of code points (ints) back to string"
  [cps]
  (str/join (map #(Character/toString ^int %) cps)))

(defn- cp-substring
  "Substring using codepoints"
  [{[^String s start len] :args}]
  (->> (cp-seq s)
       (drop start)
       (take len)
       (cp-seq->str)))

; https://stackoverflow.com/a/15224865/11849243
(defn subseq-pos
  "Find position of all sub-sequences in seq"
  [sq sub]
  (->>
    (partition (count sub) 1 sq)
    (map-indexed vector)
    (filter #(= (second %) sub))
    (map first)))

(defn cp-indexof-n
  [s search]
  (subseq-pos (cp-seq s) (cp-seq search)))

(defn cp-indexof
  [s search]
  (first (cp-indexof-n s search)))

(defn- trim-left [s cutset]
  (let [s-vec (str/split s #"")
        chars (set (str/split cutset #""))]
    (str/join "" (drop-while #(contains? chars %) s-vec))))

(defn- trim-right [s cutset]
  (let [s-vec (str/split (str/reverse s) #"")
        chars (set (str/split cutset #""))]
    (str/reverse (str/join "" (drop-while #(contains? chars %) s-vec)))))

(defn builtin-concat
  "Implementation of concat built-in"
  {:builtin "concat" :args-types ["string" #{"array", "set"}]}
  [{[^String delim coll] :args}]
  (check-args (meta #'builtin-concat) delim coll)
  (str/join delim coll))

(defn builtin-contains
  "Implementation of contains built-in"
  {:builtin "contains" :args-types ["string" "string"]}
  [{[^String s ^String search] :args}]
  (check-args (meta #'builtin-contains) s search)
  (.contains s search))

(defn builtin-endswith
  "Implementation of endswith built-in"
  {:builtin "endswith" :args-types ["string" "string"]}
  [{[s search] :args}]
  (check-args (meta #'builtin-endswith) s search)
  (str/ends-with? s search))

(defn builtin-format-int
  "Implementation of format_int built-in"
  {:builtin "format_int" :args-types ["number" "number"]}
  [{[number base] :args}]
  (check-args (meta #'builtin-format-int) number base)
  (if-not (contains? #{2 8 10 16} base)
    (throw (errors/type-ex "format_int: operand 2 must be one of {2, 8, 10, 16}"))
    (Integer/toString number base)))


(defn builtin-indexof
  "Implementation of indexof built-in"
  {:builtin "indexof" :args-types ["string" "string"]}
  [{[s search] :args}]
  (check-args (meta #'builtin-indexof) s search)
  (or (cp-indexof s search) -1))

(defn builtin-indexof-n
  "Implementation of indexof_n built-in"
  {:builtin "indexof_n" :args-types ["string" "string"]}
  [{[s search] :args}]
  (check-args (meta #'builtin-indexof-n) s search)
  (cp-indexof-n s search))

(defn builtin-lower
  "Implementation of lower built-in"
  {:builtin "lower" :args-types ["string"]}
  [{[^String s] :args}]
  (check-args (meta #'builtin-lower) s)
  (.toLowerCase s))

(defn builtin-replace
  "Implementation of replace built-in"
  {:builtin "replace" :args-types ["string" "string" "string"]}
  [{[s old new] :args}]
  (check-args (meta #'builtin-replace) s old new)
  (str/replace s old new))

(defn builtin-strings-reverse
  "Implementation of strings.reverse built-in"
  {:builtin "strings.reverse" :args-types ["string"]}
  [{[s] :args}]
  (check-args (meta #'builtin-strings-reverse) s)
  (str/reverse s))

(defn builtin-split
  "Implementation of split built-in"
  {:builtin "split" :args-types ["string" "string"]}
  [{[s delim] :args}]
  (check-args (meta #'builtin-split) s)
  (if (= s delim)
    ; Rego quirk? Not sure why this is so, or if there are more cases like this,
    ; but we'll try to mimic this here as well
    ["" ""]
    (str/split s (re-pattern (Pattern/quote delim)))))

(defn builtin-startswith
  "Implementation of startswith built-in"
  {:builtin "startswith" :args-types ["string" "string"]}
  [{[s search] :args}]
  (check-args (meta #'builtin-startswith) s search)
  (str/starts-with? s search))

; Note: Rego `substring` uses length as the second argument, while
; Clojure `subs` uses the position in the string
(defn builtin-substring
  "Implementation of substring built-in"
  {:builtin "substring" :args-types ["string" "number" "number"]}
  [{[s start len] :args}]
  (check-args (meta #'builtin-substring) s start len)
  (if (neg-int? start)
    (throw (errors/builtin-ex "substring: negative offset"))
    (let [cpc (cp-count {:args [s]})]
      (if (>= start cpc)
        ""
        (if (neg-int? len)
          (cp-substring {:args [s start cpc]})
          (cp-substring {:args [s start len]}))))))

(defn builtin-trim
  "Implementation of trim built-in"
  {:builtin "trim" :args-types ["string" "string"]}
  [{[s cutset] :args}]
  (check-args (meta #'builtin-trim) s cutset)
  (-> s (trim-left cutset) (trim-right cutset)))

(defn builtin-trim-left
  "Implementation of trim_left built-in"
  {:builtin "trim_left" :args-types ["string" "string"]}
  [{[s cutset] :args}]
  (check-args (meta #'builtin-trim-left) s cutset)
  (trim-left s cutset))

(defn builtin-trim-prefix
  "Implementation of trim_prefix built-in"
  {:builtin "trim_prefix" :args-types ["string" "string"]}
  [{[s prefix] :args}]
  (check-args (meta #'builtin-trim-prefix) s prefix)
  (if (str/starts-with? s prefix)
    (subs s (count prefix))
    s))

(defn builtin-trim-right
  "Implementation of trim_right built-in"
  {:builtin "trim_right" :args-types ["string" "string"]}
  [{[s cutset] :args}]
  (check-args (meta #'builtin-trim-right) s cutset)
  (trim-right s cutset))

(defn builtin-trim-suffix
  "Implementation of trim_suffix built-in"
  {:builtin "trim_suffix" :args-types ["string" "string"]}
  [{[s suffix] :args}]
  (check-args (meta #'builtin-trim-suffix) s suffix)
  (if (str/ends-with? s suffix)
    (subs s 0 (- (count s) (count suffix)))
    s))

(defn builtin-trim-space
  "Implementation of trim_space built-in"
  {:builtin "trim_space" :args-types ["string"]}
  [{[s] :args}]
  (check-args (meta #'builtin-trim-space) s)
  (str/trim s))

(defn builtin-upper
  "Implementation of upper built-in"
  {:builtin "upper" :args-types ["string"]}
  [{[^String s] :args}]
  (check-args (meta #'builtin-upper) s)
  (.toUpperCase s))

(defn format-value [el]
  (condp instance? el
    BigDecimal       (str/lower-case (str/replace (str el) #"\+" ""))
    PersistentVector (str "[" (str/join ", " (for [x el] (if (string? x) (str "\"" x "\"") x))) "]")
    el))

; TODO: Not yet in registry. WIP hacked together to first pass the compliance test.
; Definitely needs a more robust implementation!
(defn builtin-sprintf
  "Implementation of sprintf built-in"
  {:builtin "sprintf" :args-types ["string" "array"]}
  [{[^String s arr] :args}]
  (check-args (meta #'builtin-sprintf) s arr)
  (let [as-java (map format-value arr)]
    (String/format Locale/US
                   (str/replace s #"%v" "%s")
                   (to-array as-java))))

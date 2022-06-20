(ns jarl.builtins.strings
  (:require [jarl.exceptions :as errors]
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
(defn- subseq-pos
  "Find position of all sub-sequences in seq"
  [sq sub]
  (->>
    (partition (count sub) 1 sq)
    (map-indexed vector)
    (filter #(= (second %) sub))
    (map first)))

(defn- cp-indexof-n
  [s search]
  (subseq-pos (cp-seq s) (cp-seq search)))

(defn- cp-indexof
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
  [{[^String delim coll] :args}]
  (str/join delim coll))

(defn builtin-contains
  [{[^String s ^String search] :args}]
  (.contains s search))

(defn builtin-endswith
  [{[s search] :args}]
  (str/ends-with? s search))

(defn builtin-format-int
  [{[number base] :args}]
  (if-not (contains? #{2 8 10 16} base)
    (throw (errors/type-ex "format_int: operand 2 must be one of {2, 8, 10, 16}"))
    (Integer/toString number base)))


(defn builtin-indexof
  [{[s search] :args}]
  (or (cp-indexof s search) -1))

(defn builtin-indexof-n
  [{[s search] :args}]
  (cp-indexof-n s search))

(defn builtin-lower
  [{[^String s] :args}]
  (.toLowerCase s))

(defn builtin-replace
  [{[s old new] :args}]
  (str/replace s old new))

(defn builtin-strings-reverse
  [{[s] :args}]
  (str/reverse s))

(defn builtin-split
  [{[s delim] :args}]
  (if (= s delim)
    ; Rego quirk? Not sure why this is so, or if there are more cases like this,
    ; but we'll try to mimic this here as well
    ["" ""]
    (str/split s (re-pattern (Pattern/quote delim)))))

(defn builtin-startswith
  [{[s search] :args}]
  (str/starts-with? s search))

; Note: Rego `substring` uses length as the second argument, while
; Clojure `subs` uses the position in the string
(defn builtin-substring
  [{[s start len] :args}]
  (if (neg-int? start)
    (throw (errors/builtin-ex "substring: negative offset"))
    (let [cpc (cp-count {:args [s]})]
      (if (>= start cpc)
        ""
        (if (neg-int? len)
          (cp-substring {:args [s start cpc]})
          (cp-substring {:args [s start len]}))))))

(defn builtin-trim
  [{[s cutset] :args}]
  (-> s (trim-left cutset) (trim-right cutset)))

(defn builtin-trim-left
  [{[s cutset] :args}]
  (trim-left s cutset))

(defn builtin-trim-prefix
  [{[s prefix] :args}]
  (if (str/starts-with? s prefix)
    (subs s (count prefix))
    s))

(defn builtin-trim-right
  [{[s cutset] :args}]
  (trim-right s cutset))

(defn builtin-trim-suffix
  [{[s suffix] :args}]
  (if (str/ends-with? s suffix)
    (subs s 0 (- (count s) (count suffix)))
    s))

(defn builtin-trim-space
  [{[s] :args}]
  (str/trim s))

(defn builtin-upper
  [{[^String s] :args}]
  (.toUpperCase s))

(defn format-value [el]
  (condp instance? el
    BigDecimal       (str/lower-case (str/replace (str el) #"\+" ""))
    PersistentVector (str "[" (str/join ", " (for [x el] (if (string? x) (str "\"" x "\"") x))) "]")
    el))

; TODO: Not yet in registry. WIP hacked together to first pass the compliance test.
; Definitely needs a more robust implementation!
(defn builtin-sprintf
  [{[^String s arr] :args}]
  (let [as-java (map format-value arr)]
    (String/format Locale/US
                   (str/replace s #"%v" "%s")
                   (to-array as-java))))

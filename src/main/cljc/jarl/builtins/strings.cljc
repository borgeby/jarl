(ns jarl.builtins.strings
  (:require [clojure.string :as str]
            [jarl.exceptions :as errors]
            #?(:clj [jarl.utils :refer [count-str]])
            #?(:cljs [goog.string :as gstring])
            #?(:cljs [cljs.math :as math]))
  #?(:clj (:import (java.util.regex Pattern)
                   (java.util Locale))))

#?(:clj (defn- cp-seq
  "Return seq of codepoints (ints) from string"
  [^String s]
  (iterator-seq (.iterator (.codePoints s)))))

#?(:clj (defn- cp-seq->str
  "Turn sequence of code points (ints) back to string"
  [cps]
  (str/join (map #(Character/toString ^int %) cps))))

#?(:clj (defn- cp-substring
  "Substring using codepoints"
  [{[^String s start len] :args}]
  (->> (cp-seq s)
       (drop start)
       (take len)
       cp-seq->str)))

; https://stackoverflow.com/a/15224865/11849243
#?(:clj (defn- subseq-pos
  "Find position of all sub-sequences in seq"
  [sq sub]
  (->>
    (partition (count sub) 1 sq)
    (map-indexed vector)
    (filter #(= (second %) sub))
    (map first))))

#?(:clj (defn- cp-indexof-n
  [s search]
  (subseq-pos (cp-seq s) (cp-seq search))))

#?(:clj (defn- cp-indexof
  [s search]
  (first (cp-indexof-n s search))))

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
  (str/includes? s search))

(defn builtin-endswith
  [{[s search] :args}]
  (str/ends-with? s search))

(defn builtin-format-int
  [{[number base] :args}]
  (if-not (contains? #{2 8 10 16} base)
    (throw (errors/type-ex "operand 2 must be one of {2, 8, 10, 16}"))
    #?(:clj  (Integer/toString number base)
       :cljs (.toString (math/floor number) base))))

#?(:clj (defn builtin-indexof
  [{[s search] :args}]
  (or (cp-indexof s search) -1)))

#?(:clj (defn builtin-indexof-n
  [{[s search] :args}]
  (cp-indexof-n s search)))

(defn builtin-lower
  [{[^String s] :args}]
  (str/lower-case s))

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
    #?(:clj  (str/split s (re-pattern (Pattern/quote delim)))
       :cljs (str/split s (js/RegExp (gstring/regExpEscape delim))))))

(defn builtin-startswith
  [{[s search] :args}]
  (str/starts-with? s search))

; Note: Rego `substring` uses length as the second argument, while
; Clojure `subs` uses the position in the string
#?(:clj (defn builtin-substring
  [{[s start len] :args}]
  (if (neg-int? start)
    (throw (errors/builtin-ex "negative offset"))
    (let [cpc (count-str s)]
      (if (>= start cpc)
        ""
        (if (neg-int? len)
          (cp-substring {:args [s start cpc]})
          (cp-substring {:args [s start len]})))))))

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
  (str/upper-case s))

#?(:clj (defn format-value [el]
  (cond
    (instance? BigDecimal el) (str/lower-case (str/replace (str el) #"\+" ""))
    (vector? el)              (str "[" (str/join ", " (for [x el] (if (string? x) (str "\"" x "\"") x))) "]")
    :else el)))

; TODO: Not yet in registry. WIP hacked together to first pass the compliance test.
; Definitely needs a more robust implementation!
#?(:clj (defn builtin-sprintf
  [{[^String s arr] :args}]
  (let [as-java (map format-value arr)]
    (String/format Locale/US
                   (str/replace s #"%v" "%s")
                   (to-array as-java)))))

(ns jarl.utils
  (:require [clojure.walk :as walk]
            #?(:cljs [clojure.string :as str])
            #?(:cljs [cljs.math])
            #?(:cljs [jarl.types :as types]))
  #?(:clj (:import (java.nio.charset StandardCharsets)
                   (java.net URLDecoder URLEncoder))))

(defn url-decode [^String s]
  #?(:clj  (URLDecoder/decode s StandardCharsets/UTF_8)
     :cljs (js/decodeURIComponent (str/replace s #"\+" " "))))

(defn url-encode [^String s]
  #?(:clj  (URLEncoder/encode s StandardCharsets/UTF_8)
     :cljs (str/replace (js/encodeURIComponent s) #"%20" "+")))

 (defn indexed-map->vector
  "Created array from map values, with nil values in places for missing indices"
  [map]
  (if (empty? map)
    []
    (mapv #(get map %) (range (inc (key (apply max-key key map)))))))

(defn indiscriminate-assoc-in
  "Works like [clojure.core/assoc-in], but allows any nested component under `m` to be a non-map type,
  which will then be replaced in case `[k & ks]` overlap."
  [m [k & ks] v]
  (let [m (if (map? m) m {})]
    (if ks
      (update m k indiscriminate-assoc-in ks v)
      (assoc m k v))))

; Some tricks / hacks for unicode handling in Javascript / ClojureScript:
; https://dev.to/coolgoose/quick-and-easy-way-of-counting-utf-8-characters-in-javascript-23ce
; https://gist.github.com/galdolber/1568e767fe69f9439874cc20c755b80e

#?(:cljs
   (defn cp-seq [txt]
     (when txt
       (let [reg (js* "/(\\P{Mark}\\p{Mark}*)/u")]
         (map first (re-seq reg txt))))))

#?(:cljs
  (defn cp-subs
    ([txt from] (subs txt from (count txt)))
    ([txt from to]
     (apply str (take (- to from) (drop from (cp-seq txt)))))))

(defn count-str [s]
  #?(:clj  (.codePointCount ^String s 0 (.length ^String s))
     :cljs (count (cp-seq s))))

#?(:cljs
   (def bigint-max-int (js/BigInt js/Number.MAX_SAFE_INTEGER)))

#?(:cljs
   (def bigint-zero (js/BigInt 0)))

#?(:cljs
   (defn bigint->number
     "Since the BigInt JS type isn't well-supported in CLJS, coerce to Number when possible"
     [x]
     (if (> x bigint-max-int)
       x
       (js/Number x))))

#?(:cljs
   (defn bigint-or-round [x]
     (if (types/bigint? x) x (cljs.math/round x))))

(defn transform-map-vals
  "Recursively transform all values in map using function t"
  [t coll]
  (letfn [(transform [[k v]] [k (t v)])]
    (walk/postwalk (fn [x] (if (map? x) (with-meta (into {} (map transform x)) (meta x)) x)) coll)))

; Courtesy of Marin Atanasov Nikolov: https://dnaeon.github.io/recursively-merging-maps-in-clojure/
(defn deep-merge
  "Recursively merges maps."
  [& maps]
  (letfn [(m [& xs]
            (if (some #(and (map? %) (not (record? %))) xs)
              (apply merge-with m xs)
              (last xs)))]
    (reduce m {} maps)))

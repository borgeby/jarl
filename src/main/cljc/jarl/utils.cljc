(ns jarl.utils
  (:require [clojure.string :as str]
            #?(:cljs [cljs.nodejs :as nodejs]))
  #?(:clj (:import (java.nio.charset StandardCharsets)
                   (java.time Instant)
                   (java.util Base64)
                   (java.net URLDecoder URLEncoder))))

(defn base64-encode [^String s]
  #?(:clj  (.encodeToString (Base64/getEncoder) (.getBytes s StandardCharsets/UTF_8))
     :cljs (js/btoa s)))

(defn int->hex [i]
  #?(:clj (Integer/toHexString i)
     :cljs (.toString i 16)))

(defn hex-encode [^String s]
  #?(:clj  (str/join (map #(format "%02x" %) (.getBytes s StandardCharsets/UTF_8)))
     :cljs (str/join (map #(-> % (.charCodeAt 0) int->hex (.padStart 2 "0")) (-> s (.split ""))))))

(defn hex-decode [^String s]
  #?(:clj  (let [from-hex (fn [[x y]] (unchecked-byte (Integer/parseInt (str x y) 16)))
                 bytes ^bytes (into-array Byte/TYPE (map from-hex (partition 2 s)))]
             (String. bytes StandardCharsets/UTF_8))
     :cljs (map #(-> % str/join (js/parseInt 16) String/fromCharCode)  (partition 2 s))))


(defn url-decode [^String s]
  #?(:clj  (URLDecoder/decode s StandardCharsets/UTF_8)
     :cljs (js/decodeURI s)))

(defn url-encode [^String s]
  #?(:clj  (URLEncoder/encode s StandardCharsets/UTF_8)
     :cljs (js/encodeURI s)))

 (defn indexed-map->vector
  "Created array from map values, with nil values in places for missing indices"
  [map]
  (if (empty? map)
    []
    (mapv #(get map %) (range (inc (key (apply max-key key map)))))))

; Inspired by clojure.core/assoc-in
(defn indiscriminate-assoc-in
  "Works like [clojure.core/assoc-in], but allows any nested component under `m` to be a non-map type,
  which will then be replaced in case `[k & ks]` overlap."
  [m [k & ks] v]
  (let [m (if (map? m) m {})]
    (if ks
      (update m k indiscriminate-assoc-in ks v)
      (assoc m k v))))

#?(:clj (defn instant-to-ns [^Instant instant]
          (+' (*' (.getEpochSecond instant) 1000000000) (.getNano instant))))

#?(:clj (defn ns-to-instant ^Instant [ns]
          (Instant/ofEpochSecond 0 ns)))

#?(:cljs (def process (nodejs/require "process")))

(defn time-now-ns
  "Current time nanos — not as precise as its OPA/Go equivalent function, but not in any way that matters"
  []
  #?(:clj  (instant-to-ns (Instant/now))
     :cljs (process.hrtime.bigint))) ;TODO add something that'll work in browser contexts too

(defn env []
  #?(:clj (System/getenv)
     :cljs {})) ; TODO - Add Node implementation

(defn count-str [s]
  #?(:clj  (.codePointCount ^String s 0 (.length ^String s))
     ; TODO
     :cljs (count s)))

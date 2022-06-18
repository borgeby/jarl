(ns jarl.utils
  (:require [clojure.string :as str])
  (:import (java.nio.charset StandardCharsets)
           (java.time Instant)
           (java.util Base64)
           (java.net URLDecoder URLEncoder)))

(defn base64-encode [^String s]
  (.encodeToString (Base64/getEncoder) (.getBytes s StandardCharsets/UTF_8)))

(defn hex-encode [^String s]
  (str/join (map #(format "%02x" %) (.getBytes s StandardCharsets/UTF_8))))

(defn url-decode [^String s]
  (URLDecoder/decode s StandardCharsets/UTF_8))

(defn url-encode [^String s]
  (URLEncoder/encode s StandardCharsets/UTF_8))

 (defn indexed-map->array
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

(defn instant-to-ns [^Instant instant]
  (+' (*' (.getEpochSecond instant) 1000000000) (.getNano instant)))

(defn ns-to-instant ^Instant [ns]
  (Instant/ofEpochSecond 0 ns))

(defn time-now-ns
  "Current time nanos — not as precise as its OPA/Go equivalent function, but not in any way that matters"
  []
  (instant-to-ns (Instant/now)))

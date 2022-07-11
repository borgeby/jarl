(ns jarl.encoding.hex
  (:require #?(:clj [clojure.string :as str])
            #?(:cljs [goog.crypt :as crypt]))
  #?(:clj (:import (java.nio.charset StandardCharsets))))

(defn hex? [^String s]
  (some? (re-matches #"[0-9a-fA-F]+" s)))

(defn int->hex [i]
  #?(:clj (Integer/toHexString i)
     :cljs (.toString i 16)))

(defn encode [^String s]
  #?(:clj  (str/join (map #(format "%02x" %) (.getBytes s StandardCharsets/UTF_8)))
     :cljs (crypt/byteArrayToHex (crypt/stringToUtf8ByteArray s))))

(defn decode [^String s]
  #?(:clj  (let [from-hex (fn [[x y]] (unchecked-byte (Integer/parseInt (str x y) 16)))
                 bytes ^bytes (into-array Byte/TYPE (map from-hex (partition 2 s)))]
             (String. bytes StandardCharsets/UTF_8))
     :cljs (crypt/utf8ByteArrayToString (crypt/hexToByteArray s))))

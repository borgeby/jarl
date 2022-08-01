(ns jarl.encoding.hex
  (:require [jarl.builtins.utils :refer [bytes->str str->bytes]]
            #?(:clj [clojure.string :as str])
            #?(:cljs [goog.crypt :as crypt])))

(defn hex? [^String s]
  (some? (re-matches #"[0-9a-fA-F]+" s)))

(defn int->hex [i]
  #?(:clj (Integer/toHexString i)
     :cljs (.toString i 16)))

(defn encode-bytes [^bytes bytes]
  #?(:clj  (str/join (map #(format "%02x" %) bytes))
     :cljs (crypt/byteArrayToHex bytes)))

(defn encode [^String s]
  (encode-bytes (str->bytes s)))

(defn decode [^String s]
  #?(:clj  (let [from-hex (fn [[x y]] (unchecked-byte (Integer/parseInt (str x y) 16)))
                 bytes ^bytes (into-array Byte/TYPE (map from-hex (partition 2 s)))]
             (bytes->str bytes))
     :cljs (bytes->str (crypt/hexToByteArray s))))

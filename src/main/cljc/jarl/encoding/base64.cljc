(ns jarl.encoding.base64
  (:require [clojure.string :as str]
            #?(:cljs [goog.crypt :as crypt])
            #?(:cljs [goog.crypt.base64 :as base64])
            [jarl.exceptions :as errors])
  #?(:clj (:import (java.nio.charset StandardCharsets)
                   (java.util Base64))))

(defn base64? [^String s]
  (some? (re-matches #"[0-9a-zA-Z\+\/]+=?=?" s)))

(defn base64-url? [^String s]
  (some? (re-matches #"[0-9a-zA-Z-_]+=?=?" s)))

(defn index-of-first-non-match [^String s predicate]
  (let [first-violation (first (remove predicate (map str s)))]
    (str/index-of s first-violation)))

(defn encode ^String [^String s]
  #?(:clj  (.encodeToString (Base64/getEncoder) (.getBytes s StandardCharsets/UTF_8))
     :cljs (base64/encodeByteArray (crypt/stringToUtf8ByteArray s))))

(defn decode-bytes ^bytes [^String s]
  (if-not (base64? s)
    (throw (errors/builtin-ex "illegal base64 data at input byte %s" (index-of-first-non-match s base64?)))
    #?(:clj  (.decode (Base64/getDecoder) s)
       :cljs (base64/decodeStringToByteArray s))))

(defn decode ^String [^String s]
  #?(:clj  (String. (decode-bytes s))
     :cljs (crypt/utf8ByteArrayToString (decode-bytes s))))

(defn url-encode ^String [^String s]
  #?(:clj  (.encodeToString (Base64/getUrlEncoder) (.getBytes s StandardCharsets/UTF_8))
     :cljs (base64/encodeByteArray (crypt/stringToUtf8ByteArray s) base64/Alphabet.WEBSAFE)))

(defn url-decode-bytes ^bytes [^String s]
  (if-not (base64-url? s)
    (throw (errors/builtin-ex "illegal base64 data at input byte %s" (index-of-first-non-match s base64-url?)))
    #?(:clj  (.decode (Base64/getUrlDecoder) s)
       :cljs (decode-bytes s))))

(defn url-decode ^String [^String s]
  #?(:clj  (String. (url-decode-bytes s))
     :cljs (crypt/utf8ByteArrayToString (url-decode-bytes s))))

(defn url-encode-no-pad ^String [^String s]
  #?(:clj  (-> (Base64/getUrlEncoder) (.withoutPadding) (.encodeToString (.getBytes s StandardCharsets/UTF_8)))
     :cljs (base64/encodeByteArray (crypt/stringToUtf8ByteArray s) base64/Alphabet.WEBSAFE_NO_PADDING)))

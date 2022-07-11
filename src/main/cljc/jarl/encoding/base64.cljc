(ns jarl.encoding.base64
  #?(:cljs (:require [goog.crypt :as crypt]
                     [goog.crypt.base64 :as base64]))
  #?(:clj (:import (java.nio.charset StandardCharsets)
                   (java.util Base64))))

(defn encode ^String [^String s]
  #?(:clj  (.encodeToString (Base64/getEncoder) (.getBytes s StandardCharsets/UTF_8))
     :cljs (base64/encodeByteArray (crypt/stringToUtf8ByteArray s))))

(defn decode ^String [^String s]
  #?(:clj  (-> (Base64/getDecoder) (.decode s) (String.))
     :cljs (crypt/utf8ByteArrayToString (base64/decodeStringToByteArray s))))

(defn url-encode ^String [^String s]
  #?(:clj  (.encodeToString (Base64/getUrlEncoder) (.getBytes s StandardCharsets/UTF_8))
     :cljs (base64/encodeByteArray (crypt/stringToUtf8ByteArray s) base64/Alphabet.WEBSAFE)))

(defn url-decode ^String [^String s]
  #?(:clj  (-> (Base64/getUrlDecoder) (.decode s) (String.))
     :cljs (decode s)))

(defn url-encode-no-pad ^String [^String s]
  #?(:clj  (-> (Base64/getUrlEncoder) (.withoutPadding) (.encodeToString (.getBytes s StandardCharsets/UTF_8)))
     :cljs (base64/encodeByteArray (crypt/stringToUtf8ByteArray s) base64/Alphabet.WEBSAFE_NO_PADDING)))

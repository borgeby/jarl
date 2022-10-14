(ns jarl.encoding.base64
  (:require [clojure.string :as str]
            #?(:cljs [goog.string :as gstring])
            #?(:cljs [goog.crypt.base64 :as base64])
            [jarl.exceptions :as errors]
            [jarl.builtins.utils :refer [bytes->str str->bytes]])
  #?(:clj (:import (java.util Base64))))

#?(:cljs
   (defn- b64url->b64 [s]
     (let [fixed (-> s (str/replace "-" "+") (str/replace "_" "/"))
           m (mod (count fixed) 4)
           req-padding (if (zero? m) 0 (- 4 m))]
       (cond-> fixed
               (pos? req-padding) (str (gstring/repeat "=" req-padding))))))

(defn base64? [^String s]
    (and (zero? (mod (count s) 4))
         (some? (re-matches #"[0-9a-zA-Z\+\/]+=?=?" s))))

(defn base64-url? [^String s]
  (some? (re-matches #"[0-9a-zA-Z-_]+=?=?" s)))

(defn index-of-first-non-match [^String s predicate]
  (let [first-violation (first (remove predicate (map str s)))]
    (str/index-of s first-violation)))

(defn encode-bytes [b]
  #?(:clj  (.encodeToString (Base64/getEncoder) b)
     :cljs (base64/encodeByteArray b)))

(defn encode ^String [^String s]
  (encode-bytes (str->bytes s)))

(defn decode-bytes ^bytes [^String s]
  (if-not (base64? s)
    (throw (errors/builtin-ex "illegal base64 data at input byte %s" (index-of-first-non-match s base64?)))
    #?(:clj  (.decode (Base64/getDecoder) s)
       :cljs (base64/decodeStringToByteArray s))))

(defn decode ^String [^String s]
  (bytes->str (decode-bytes s)))

(defn url-encode ^String [^String s]
  #?(:clj  (.encodeToString (Base64/getUrlEncoder) (str->bytes s))
     :cljs (base64/encodeByteArray (str->bytes s) base64/Alphabet.WEBSAFE)))

(defn url-decode-bytes ^bytes [^String s]
  (if-not (base64-url? s)
    (throw (errors/builtin-ex "illegal base64 data at input byte %s" (index-of-first-non-match s base64-url?)))
    #?(:clj  (.decode (Base64/getUrlDecoder) s)
       :cljs (decode-bytes (b64url->b64 s)))))

(defn url-decode ^String [^String s]
  (bytes->str (url-decode-bytes s)))

(defn url-encode-no-pad ^String [^String s]
  #?(:clj  (-> (Base64/getUrlEncoder) (.withoutPadding) (.encodeToString (str->bytes s)))
     :cljs (base64/encodeByteArray (str->bytes s) base64/Alphabet.WEBSAFE_NO_PADDING)))

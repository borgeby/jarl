(ns jarl.builtins.crypto
  (:require [jarl.encoding.hex :as hex]
            [jarl.builtins.utils :refer [str->bytes]]
            #?(:clj  [jarl.encoding.base64 :as base64])
            #?(:clj  [clojure.string :as str])
            #?(:cljs [goog.crypt :as crypt]))
  #?(:cljs
     (:import (goog.crypt Md5 Sha1 Sha256 Sha512 Hmac)))
  #?(:clj
     (:import (javax.crypto.spec SecretKeySpec)
              (javax.crypto Mac)
              (java.security MessageDigest KeyFactory Key)
              (org.jose4j.jwk JsonWebKey$Factory JsonWebKey$OutputControlLevel)
              (java.security.spec PKCS8EncodedKeySpec)
              (java.security.interfaces RSAPrivateKey))))

#?(:clj
   (defn hmac-str
     "Calculate HMAC signature for given data"
     [^String algorithm ^String data ^String key]
     (let [signing-key (SecretKeySpec. (str->bytes key) algorithm)
           mac (doto (Mac/getInstance algorithm) (.init signing-key))]
       (hex/encode-bytes (.doFinal mac (str->bytes data))))))

; https://gist.github.com/jizhang/4325757
#?(:clj
   (defn hash-str
     "Hash string using provided algorithm"
     [^String algorithm ^String data]
     (let [algorithm (MessageDigest/getInstance algorithm)
           size (* 2 (.getDigestLength algorithm))
           raw (.digest algorithm (.getBytes data))
           sig (.toString (BigInteger. 1 raw) 16)
           padding (str/join (repeat (- size (count sig)) "0"))]
       (str padding sig))))

(defn builtin-crypto-hmac-md5
  [{[x key] :args}]
  #?(:clj  (hmac-str "HmacMD5" x key)
     :cljs (-> (Hmac. (Md5.) (str->bytes key))
               (.getHmac x)
               (hex/encode-bytes))))

(defn builtin-crypto-hmac-sha1
  [{[x key] :args}]
  #?(:clj  (hmac-str "HmacSHA1" x key)
     :cljs (-> (Hmac. (Sha1.) (str->bytes key))
               (.getHmac x)
               (hex/encode-bytes))))

(defn builtin-crypto-hmac-sha256
  [{[x key] :args}]
  #?(:clj  (hmac-str "HmacSHA256" x key)
     :cljs (-> (Hmac. (Sha256.) (str->bytes key))
               (.getHmac x)
               (hex/encode-bytes))))

(defn builtin-crypto-hmac-sha512
  [{[x key] :args}]
  #?(:clj  (hmac-str "HmacSHA512" x key)
     :cljs (-> (Hmac. (Sha512.) (str->bytes key))
               (.getHmac x)
               (hex/encode-bytes))))

(defn builtin-crypto-md5
  [{[x] :args}]
  #?(:clj  (hash-str "MD5" x)
     :cljs (hex/encode-bytes (.digest (doto (Md5.) (.update (str->bytes x)))))))

(defn builtin-crypto-sha1
  [{[x] :args}]
  #?(:clj  (hash-str "SHA-1" x)
     :cljs (hex/encode-bytes (.digest (doto (Sha1.) (.update (str->bytes x)))))))

(defn builtin-crypto-sha256
  [{[x] :args}]
  #?(:clj  (hash-str "SHA-256" x)
     :cljs (hex/encode-bytes (.digest (doto (Sha256.) (.update (str->bytes x)))))))

#?(:clj
   (defn- pem->rsa-private ^Key [pem]
     (let [encoded     (base64/decode-bytes pem)
           key-factory (KeyFactory/getInstance "RSA")
           key-spec    (PKCS8EncodedKeySpec. encoded)]
       (cast RSAPrivateKey (.generatePrivate key-factory key-spec)))))

#?(:clj
   (defn builtin-crypto-x509-parse-rsa-private-key
     [{[^String pem] :args}]
     (let [key (pem->rsa-private (-> pem
                                     (str/replace "-----BEGIN PRIVATE KEY-----" "")
                                     (str/replace "-----END PRIVATE KEY-----" "")
                                     (str/replace (System/lineSeparator) "")))
           jwk (JsonWebKey$Factory/newJwk key)]
      (.toJson jwk JsonWebKey$OutputControlLevel/INCLUDE_PRIVATE))))

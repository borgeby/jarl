(ns jarl.builtins.crypto
  (:require [clojure.string :as str]
            [jarl.builtins.utils :refer [check-args]])
  (:import (javax.crypto.spec SecretKeySpec)
           (javax.crypto Mac)
           (java.nio.charset StandardCharsets)
           (java.security MessageDigest)))

(defn hmac-string
  "Calculate HMAC signature for given data"
  [^String algorithm ^String data ^String key]
  (let [signing-key (SecretKeySpec. (.getBytes key StandardCharsets/UTF_8) algorithm)
        mac (doto (Mac/getInstance algorithm) (.init signing-key))]
    (str/join (map #(format "%02x" %) (.doFinal mac (.getBytes data StandardCharsets/UTF_8))))))

; https://gist.github.com/jizhang/4325757
(defn hash-string
  "Hash string using provided algorithm"
  [^String algorithm ^String data]
  (let [algorithm (MessageDigest/getInstance algorithm)
        size (* 2 (.getDigestLength algorithm))
        raw (.digest algorithm (.getBytes data))
        sig (.toString (BigInteger. 1 raw) 16)
        padding (str/join (repeat (- size (count sig)) "0"))]
    (str padding sig)))

(defn builtin-crypto-hmac-md5
  "Implementation of crypto.hmac.md5 built-in"
  {:builtin "crypto.hmac.md5" :args-types ["string" "string"]}
  [{[x key] :args}]
  (check-args (meta #'builtin-crypto-hmac-md5) x key)
  (hmac-string "HmacMD5" x key))

(defn builtin-crypto-hmac-sha1
  "Implementation of crypto.hmac.sha1 built-in"
  {:builtin "crypto.hmac.sha1" :args-types ["string" "string"]}
  [{[x key] :args}]
  (check-args (meta #'builtin-crypto-hmac-sha1) x key)
  (hmac-string "HmacSHA1" x key))

(defn builtin-crypto-hmac-sha256
  "Implementation of crypto.hmac.sha256 built-in"
  {:builtin "crypto.hmac.sha256" :args-types ["string" "string"]}
  [{[x key] :args}]
  (check-args (meta #'builtin-crypto-hmac-sha256) x key)
  (hmac-string "HmacSHA256" x key))

(defn builtin-crypto-hmac-sha512
  "Implementation of crypto.hmac.sha512 built-in"
  {:builtin "crypto.hmac.sha512" :args-types ["string" "string"]}
  [{[x key] :args}]
  (check-args (meta #'builtin-crypto-hmac-sha512) x key)
  (hmac-string "HmacSHA512" x key))

(defn builtin-crypto-md5
  "Implementation of crypto.md5 built-in"
  {:builtin "crypto.md5" :args-types ["string"]}
  [{[x] :args}]
  (check-args (meta #'builtin-crypto-md5) x)
  (hash-string "MD5" x))

(defn builtin-crypto-sha1
  "Implementation of crypto.sha1 built-in"
  {:builtin "crypto.sha1" :args-types ["string"]}
  [{[x] :args}]
  (check-args (meta #'builtin-crypto-sha1) x)
  (hash-string "SHA-1" x))

(defn builtin-crypto-sha256
  "Implementation of crypto.sha256 built-in"
  {:builtin "crypto.sha256" :args-types ["string"]}
  [{[x] :args}]
  (check-args (meta #'builtin-crypto-sha256) x)
  (hash-string "SHA-256" x))

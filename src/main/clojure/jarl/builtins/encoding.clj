(ns jarl.builtins.encoding
  (:require [jarl.exceptions :as errors]
            [jarl.builtins.utils :refer [check-args]]
            [clojure.data.json :as json]
            [clojure.string :as str])
  (:import (java.util Base64)
           (java.net URLEncoder URLDecoder)
           (java.nio.charset StandardCharsets)
           (java.io EOFException)))

(defn builtin-base64-encode
  "Implementation of base64.encode built-in"
  {:builtin "base64.encode" :args-types ["string"]}
  [^String s]
  (check-args (meta #'builtin-base64-encode) s)
  (.encodeToString
    (Base64/getEncoder)
    (.getBytes s StandardCharsets/UTF_8)))

(defn builtin-base64-decode
  "Implementation of base64.decode built-in"
  {:builtin "base64.decode" :args-types ["string"]}
  [^String s]
  (check-args (meta #'builtin-base64-decode) s)
  (-> (Base64/getDecoder)
      (.decode s)
      (String.)))

(defn builtin-base64-url-encode
  "Implementation of base64url.encode built-in"
  {:builtin "base64url.encode" :args-types ["string"]}
  [^String s]
  (check-args (meta #'builtin-base64-url-encode) s)
  (.encodeToString
    (Base64/getUrlEncoder)
    (.getBytes s StandardCharsets/UTF_8)))

(defn builtin-base64-url-encode-no-pad
  "Implementation of base64url.encode_no_pad built-in"
  {:builtin "base64url.encode_no_pad" :args-types ["string"]}
  [^String s]
  (check-args (meta #'builtin-base64-url-encode-no-pad) s)
  (-> (Base64/getUrlEncoder)
      (.withoutPadding)
      (.encodeToString (.getBytes s StandardCharsets/UTF_8))))

(defn builtin-base64-url-decode
  "Implementation of base64url.decode built-in"
  {:builtin "base64url.decode" :args-types ["string"]}
  [^String s]
  (check-args (meta #'builtin-base64-url-decode) s)
  (-> (Base64/getUrlDecoder)
      (.decode s)
      (String.)))

(defn builtin-url-query-encode
  "Implementation of urlquery.encode built-in"
  {:builtin "urlquery.encode" :args-types ["string"]}
  [^String s]
  (check-args (meta #'builtin-url-query-encode) s)
  (URLEncoder/encode s StandardCharsets/UTF_8))

(defn builtin-url-query-decode
  "Implementation of urlquery.decode built-in"
  {:builtin "urlquery.decode" :args-types ["string"]}
  [^String s]
  (check-args (meta #'builtin-url-query-decode) s)
  (URLDecoder/decode s StandardCharsets/UTF_8))

(defn builtin-json-unmarshal
  "Implementation of json.unmarshal built-in"
  {:builtin "json.unmarshal" :args-types ["string"]}
  [^String s]
  (check-args (meta #'builtin-json-unmarshal) s)
  (try
    (json/read-str s)
    (catch EOFException _ (throw (errors/builtin-ex "eval_builtin_error: json.unmarshal: unexpected EOF")))
    (catch Exception e (throw (errors/builtin-ex "eval_builtin_error: json.unmarshal: %s" (.getMessage e))))))

(defn builtin-json-is-valid
  "Implementation of json.is_valid built-in"
  {:builtin "json.is_valid" :args-types ["string"]}
  [^String s]
  (check-args (meta #'builtin-json-is-valid) s)
  (try
    (let [_ (json/read-str s)]
      true)
    (catch Exception _ false)))

; both hex function implementations from:
; https://stackoverflow.com/a/10087740/11849243

(defn builtin-hex-encode
  "Implementation of hex.encode built-in"
  {:builtin "hex.encode" :args-types ["string"]}
  [^String s]
  (check-args (meta #'builtin-hex-encode) s)
  (str/join (map #(format "%02x" %) (.getBytes s StandardCharsets/UTF_8))))

(defn builtin-hex-decode
  "Implementation of hex.decode built-in"
  {:builtin "hex.decode" :args-types ["string"]}
  [^String s]
  (check-args (meta #'builtin-hex-decode) s)
  (let [from-hex (fn [[x y]] (unchecked-byte (Integer/parseInt (str x y) 16)))
        bytes ^bytes (into-array Byte/TYPE (map from-hex (partition 2 s)))]
    (String. bytes StandardCharsets/UTF_8)))

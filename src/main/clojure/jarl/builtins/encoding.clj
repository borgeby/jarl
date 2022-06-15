(ns jarl.builtins.encoding
  (:require [jarl.exceptions :as errors]
            [jarl.builtins.utils :refer [check-args]]
            [jarl.utils :refer [base64-encode hex-encode url-decode url-encode]]
            [clojure.data.json :as json]
            [clj-yaml.core :as yaml]
            [clojure.string :as str])
  (:import (java.util Base64)
           (java.nio.charset StandardCharsets)
           (java.io EOFException)))

(defn builtin-base64-encode
  "Implementation of base64.encode built-in"
  {:builtin "base64.encode" :args-types ["string"]}
  [{[^String s] :args}]
  (check-args (meta #'builtin-base64-encode) s)
  (base64-encode s))

(defn builtin-base64-decode
  "Implementation of base64.decode built-in"
  {:builtin "base64.decode" :args-types ["string"]}
  [{[^String s] :args}]
  (check-args (meta #'builtin-base64-decode) s)
  (-> (Base64/getDecoder)
      (.decode s)
      (String.)))

(defn builtin-base64-url-encode
  "Implementation of base64url.encode built-in"
  {:builtin "base64url.encode" :args-types ["string"]}
  [{[^String s] :args}]
  (check-args (meta #'builtin-base64-url-encode) s)
  (.encodeToString
    (Base64/getUrlEncoder)
    (.getBytes s StandardCharsets/UTF_8)))

(defn builtin-base64-url-encode-no-pad
  "Implementation of base64url.encode_no_pad built-in"
  {:builtin "base64url.encode_no_pad" :args-types ["string"]}
  [{[^String s] :args}]
  (check-args (meta #'builtin-base64-url-encode-no-pad) s)
  (-> (Base64/getUrlEncoder)
      (.withoutPadding)
      (.encodeToString (.getBytes s StandardCharsets/UTF_8))))

(defn builtin-base64-url-decode
  "Implementation of base64url.decode built-in"
  {:builtin "base64url.decode" :args-types ["string"]}
  [{[^String s] :args}]
  (check-args (meta #'builtin-base64-url-decode) s)
  (-> (Base64/getUrlDecoder)
      (.decode s)
      (String.)))

(defn builtin-url-query-encode
  "Implementation of urlquery.encode built-in"
  {:builtin "urlquery.encode" :args-types ["string"]}
  [{[^String s] :args}]
  (check-args (meta #'builtin-url-query-encode) s)
  (url-encode s))

(defn builtin-url-query-encode-object
  "Implementation of urlquery.encode_object built-in"
  {:builtin "urlquery.encode_object" :args-types ["object"]}
  [{[o] :args}]
  (check-args (meta #'builtin-url-query-encode-object) o)
  (let [kv-str (fn [[k vals]]
                 (let [vals (if (string? vals) [vals] vals)]
                   (str/join "&" (for [v vals] (str (url-encode k) "=" (url-encode v))))))]
    (str/join "&" (map kv-str o))))

(defn builtin-url-query-decode
  "Implementation of urlquery.decode built-in"
  {:builtin "urlquery.decode" :args-types ["string"]}
  [{[^String s] :args}]
  (check-args (meta #'builtin-url-query-decode) s)
  (url-decode s))

(defn builtin-url-query-decode-object
  "Implementation of urlquery.decode_object built-in"
  {:builtin "urlquery.decode_object" :args-types ["string"]}
  [{[^String s] :args}]
  (check-args (meta #'builtin-url-query-decode-object) s)
  (let [assoc-param (fn [map key val] (assoc map key (conj (get map key []) val)))]
    (reduce
      (fn [param-map encoded-param]
        (if-let [[_ key val] (re-matches #"([^=]+)=?(.*)" encoded-param)]
          (assoc-param param-map (url-decode key) (url-decode (or val "")))
          param-map))
      {}
      (str/split s #"&"))))

(defn builtin-json-marshal
  "Implementation of json.marshal built-in"
  {:builtin "json.marshal" :args-types ["any"]}
  [{[x] :args}]
  (try
    (json/json-str x)
    (catch Exception e (throw (errors/builtin-ex "eval_builtin_error: json.marshal: %s" (.getMessage e))))))

(defn builtin-json-unmarshal
  "Implementation of json.unmarshal built-in"
  {:builtin "json.unmarshal" :args-types ["string"]}
  [{[^String s] :args}]
  (check-args (meta #'builtin-json-unmarshal) s)
  (try
    (json/read-str s)
    (catch EOFException _ (throw (errors/builtin-ex "eval_builtin_error: json.unmarshal: unexpected EOF")))
    (catch Exception e (throw (errors/builtin-ex "eval_builtin_error: json.unmarshal: %s" (.getMessage e))))))

(defn builtin-json-is-valid
  "Implementation of json.is_valid built-in"
  {:builtin "json.is_valid" :args-types ["string"]}
  [{[^String s] :args}]
  (check-args (meta #'builtin-json-is-valid) s)
  (not (errors/throws? #(json/read-str s))))

; both hex function implementations from:
; https://stackoverflow.com/a/10087740/11849243

(defn builtin-hex-encode
  "Implementation of hex.encode built-in"
  {:builtin "hex.encode" :args-types ["string"]}
  [{[^String s] :args}]
  (check-args (meta #'builtin-hex-encode) s)
  (hex-encode s))

(defn- hex? [s]
  (some? (re-matches #"[0-9a-fA-F]+" s)))

(defn- non-hex-error-msg [s]
  (let [first-non-hex-char (.charAt ^String (first (remove hex? (map str s))) 0)
        unicode-str (str "U+" (subs (Integer/toHexString (bit-or (int first-non-hex-char) 0x10000)) 1))]
    (str "invalid byte: " unicode-str " '" first-non-hex-char "'")))

(defn builtin-hex-decode
  "Implementation of hex.decode built-in"
  {:builtin "hex.decode" :args-types ["string"]}
  [{[^String s] :args}]
  (check-args (meta #'builtin-hex-decode) s)
  (if (hex? s)
    (let [from-hex (fn [[x y]] (unchecked-byte (Integer/parseInt (str x y) 16)))
          bytes ^bytes (into-array Byte/TYPE (map from-hex (partition 2 s)))]
      (String. bytes StandardCharsets/UTF_8))
    (throw (errors/builtin-ex (str "hex.decode: " (non-hex-error-msg s))))))

(defn builtin-yaml-marshal
  "Implementation of yaml.marshal built-in"
  {:builtin "yaml.marshal" :args-types ["any"]}
  [{[x] :args}]
  (check-args (meta #'builtin-yaml-marshal) x)
  (try
    ; Notable difference from OPA: this YAML marshaller does not re-order the output, so we'll sort it beforehand
    (yaml/generate-string (cond->> x (map? x) (into (sorted-map))) :dumper-options {:flow-style :block})
    (catch Exception e (throw (errors/builtin-ex "eval_builtin_error: yaml.marshal: %s" (.getMessage e))))))

; Mimic the error message from OPA
(defn- yaml-unmarshal-err-msg [^String s]
  (when (.contains s "expected ',' or ']'")
    (when-let [line (re-find #"line [\d]+" s)]
      (str "yaml: " line  ": did not find expected ',' or ']'"))))

(defn builtin-yaml-unmarshal
  "Implementation of yaml.unmarshal built-in"
  {:builtin "yaml.unmarshal" :args-types ["string"]}
  [{[s] :args}]
  (check-args (meta #'builtin-yaml-unmarshal) s)
  (try
    (yaml/parse-string s :keywords false)
    (catch Exception e (let [msg (.getMessage e)]
                         (throw (errors/builtin-ex "eval_builtin_error: yaml.unmarshal: %s"
                                                 (or (yaml-unmarshal-err-msg msg) msg)))))))

(defn builtin-yaml-is-valid
  "Implementation of yaml.is_valid built-in"
  {:builtin "yaml.is_valid" :args-types ["string"]}
  [{[s] :args}]
  (check-args (meta #'builtin-yaml-is-valid) s)
  (not (errors/throws? #(yaml/parse-string s))))

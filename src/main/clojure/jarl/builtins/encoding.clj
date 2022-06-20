(ns jarl.builtins.encoding
  (:require [jarl.exceptions :as errors]
            [jarl.utils :refer [base64-encode hex-encode url-decode url-encode]]
            [clojure.data.json :as json]
            [clj-yaml.core :as yaml]
            [clojure.string :as str])
  (:import (java.util Base64)
           (java.nio.charset StandardCharsets)
           (java.io EOFException)))

(defn builtin-base64-encode
  [{[^String s] :args}]
  (base64-encode s))

(defn builtin-base64-decode
  [{[^String s] :args}]
  (-> (Base64/getDecoder)
      (.decode s)
      (String.)))

(defn builtin-base64-url-encode
  [{[^String s] :args}]
  (.encodeToString
    (Base64/getUrlEncoder)
    (.getBytes s StandardCharsets/UTF_8)))

(defn builtin-base64-url-encode-no-pad
  [{[^String s] :args}]
  (-> (Base64/getUrlEncoder)
      (.withoutPadding)
      (.encodeToString (.getBytes s StandardCharsets/UTF_8))))

(defn builtin-base64-url-decode
  [{[^String s] :args}]
  (-> (Base64/getUrlDecoder)
      (.decode s)
      (String.)))

(defn builtin-url-query-encode
  [{[^String s] :args}]
  (url-encode s))

(defn builtin-url-query-encode-object
  [{[o] :args}]
  (let [kv-str (fn [[k vals]]
                 (let [vals (if (string? vals) [vals] vals)]
                   (str/join "&" (for [v vals] (str (url-encode k) "=" (url-encode v))))))]
    (str/join "&" (map kv-str o))))

(defn builtin-url-query-decode
  [{[^String s] :args}]
  (url-decode s))

(defn builtin-url-query-decode-object
  [{[^String s] :args}]
  (let [assoc-param (fn [map key val] (assoc map key (conj (get map key []) val)))]
    (reduce
      (fn [param-map encoded-param]
        (if-let [[_ key val] (re-matches #"([^=]+)=?(.*)" encoded-param)]
          (assoc-param param-map (url-decode key) (url-decode (or val "")))
          param-map))
      {}
      (str/split s #"&"))))

(defn builtin-json-marshal
  [{[x] :args}]
  (try
    (json/json-str x)
    (catch Exception e (throw (errors/builtin-ex "eval_builtin_error: json.marshal: %s" (.getMessage e))))))

(defn builtin-json-unmarshal
  [{[^String s] :args}]
  (try
    (json/read-str s)
    (catch EOFException _ (throw (errors/builtin-ex "eval_builtin_error: json.unmarshal: unexpected EOF")))
    (catch Exception e (throw (errors/builtin-ex "eval_builtin_error: json.unmarshal: %s" (.getMessage e))))))

(defn builtin-json-is-valid
  [{[^String s] :args}]
  (not (errors/throws? #(json/read-str s))))

; both hex function implementations from:
; https://stackoverflow.com/a/10087740/11849243

(defn builtin-hex-encode
  [{[^String s] :args}]
  (hex-encode s))

(defn- hex? [s]
  (some? (re-matches #"[0-9a-fA-F]+" s)))

(defn- non-hex-error-msg [s]
  (let [first-non-hex-char (.charAt ^String (first (remove hex? (map str s))) 0)
        unicode-str (str "U+" (subs (Integer/toHexString (bit-or (int first-non-hex-char) 0x10000)) 1))]
    (str "invalid byte: " unicode-str " '" first-non-hex-char "'")))

(defn builtin-hex-decode
  [{[^String s] :args}]
  (if (hex? s)
    (let [from-hex (fn [[x y]] (unchecked-byte (Integer/parseInt (str x y) 16)))
          bytes ^bytes (into-array Byte/TYPE (map from-hex (partition 2 s)))]
      (String. bytes StandardCharsets/UTF_8))
    (throw (errors/builtin-ex (str "hex.decode: " (non-hex-error-msg s))))))

(defn builtin-yaml-marshal
  [{[x] :args}]
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
  [{[s] :args}]
  (try
    (yaml/parse-string s :keywords false)
    (catch Exception e (let [msg (.getMessage e)]
                         (throw (errors/builtin-ex "eval_builtin_error: yaml.unmarshal: %s"
                                                 (or (yaml-unmarshal-err-msg msg) msg)))))))

(defn builtin-yaml-is-valid
  [{[s] :args}]
  (not (errors/throws? #(yaml/parse-string s))))

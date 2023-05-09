(ns jarl.builtins.encoding
  (:require [jarl.encoding.base64 :as base64]
            [jarl.encoding.json :as json]
            [jarl.encoding.hex :as hex]
            [jarl.encoding.yaml :as yaml]
            [jarl.exceptions :as errors]
            [jarl.utils :refer [url-decode url-encode]]
            [clojure.string :as str])
  #?(:clj (:import (java.io EOFException))))

(defn builtin-base64-encode
  [{[^String s] :args}]
  (base64/encode s))

(defn builtin-base64-decode
  [{[^String s] :args}]
  (base64/decode s))

(defn builtin-base64-is-valid
  [{[^String s] :args}]
  (try
    (base64/decode s)
    true
    #?(:clj  (catch Exception _ false)
       :cljs (catch :default  _ false))))

(defn builtin-base64-url-encode
  [{[^String s] :args}]
  (base64/url-encode s))

(defn builtin-base64-url-encode-no-pad
  [{[^String s] :args}]
  (base64/url-encode-no-pad s))

(defn builtin-base64-url-decode
  [{[^String s] :args}]
  (base64/url-decode s))

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
  #?(:clj
     (try
       (json/write-str x)
       (catch Exception e (throw (errors/builtin-ex (ex-message e)))))
     :cljs
     (try
       (json/write-str x)
       (catch js/Error e (throw (errors/builtin-ex (ex-message e)))))))

(defn builtin-json-unmarshal
  [{[^String s] :args}]
  #?(:clj
     (try
       (json/read-str s)
       (catch EOFException _ (throw (errors/builtin-ex "unexpected EOF")))
       (catch Exception e (throw (errors/builtin-ex (ex-message e)))))
     :cljs
     (try
       (json/read-str s)
       (catch js/Error e (throw (errors/builtin-ex (ex-message e)))))))

(defn builtin-json-is-valid
  [{[^String s] :args}]
  (not (errors/throws? #(json/read-str s))))

; both hex function implementations from:
; https://stackoverflow.com/a/10087740/11849243

(defn builtin-hex-encode
  [{[^String s] :args}]
  (hex/encode s))

(defn- non-hex-error-msg [s]
  #?(:clj
     (let [first-non-hex-char (.charAt ^String (first (remove hex/hex? (map str s))) 0)
           unicode-str (str "U+" (subs (hex/int->hex (bit-or (int first-non-hex-char) 0x10000)) 1))]
       (str "invalid byte: " unicode-str " '" first-non-hex-char "'"))
     :cljs
     (let [first-non-hex-char (.charCodeAt (first (remove hex/hex? (map str s))) 0)
           unicode-str (str "U+" (subs (hex/int->hex (bit-or first-non-hex-char 0x10000)) 1))]
       (str "invalid byte: " unicode-str " '" (String/fromCharCode first-non-hex-char) "'"))))

(defn builtin-hex-decode
  [{[^String s] :args}]
  (if (hex/hex? s)
    (hex/decode s)
    (throw (errors/builtin-ex (non-hex-error-msg s)))))

(defn builtin-yaml-marshal
  [{[x] :args}]
  (try
    (yaml/write-str x)
    #?(:clj  (catch Exception e (throw (errors/builtin-ex "eval_builtin_error: yaml.marshal: %s" (ex-message e))))
       :cljs (catch js/Error  e (throw (errors/builtin-ex "eval_builtin_error: yaml.marshal: %s" (ex-message e)))))))

; Mimic the error message from OPA
(defn- yaml-unmarshal-err-msg [^String s]
  #?(:clj
     (when (str/includes? s "expected ',' or ']'")
       (when-let [line (re-find #"line [\d]+" s)]
         (str "yaml: " line  ": did not find expected ',' or ']'")))
     :cljs
     (when (str/includes? s "unexpected end of the stream within a flow collection")
       (when-let [line (second (re-find #"collection \(\d+:(\d)+\)" s))]
         (str "yaml: line " line  ": did not find expected ',' or ']'")))))

(defn builtin-yaml-unmarshal
  [{[s] :args}]
  (try
    (yaml/read-str s)
    #?(:clj
       (catch Exception e (let [msg (ex-message e)]
                            (throw (errors/builtin-ex (or (yaml-unmarshal-err-msg msg) msg)))))
       :cljs
       (catch js/Error e (let [msg (ex-message e)]
                           (throw (errors/builtin-ex (or (yaml-unmarshal-err-msg msg) msg))))))))

(defn builtin-yaml-is-valid
  [{[s] :args}]
  (not (errors/throws? #(yaml/read-str s))))

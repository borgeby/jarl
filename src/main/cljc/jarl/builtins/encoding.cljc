(ns jarl.builtins.encoding
  (:require [jarl.encoding.base64 :as base64]
            [jarl.encoding.json :as json]
            [jarl.encoding.hex :as hex]
            [jarl.exceptions :as errors]
            [jarl.utils :refer [url-decode url-encode]]
            [clojure.string :as str]
            #?(:clj [clj-yaml.core :as yaml]))
  #?(:clj (:import (java.io EOFException))))

(defn builtin-base64-encode
  [{[^String s] :args}]
  (base64/encode s))

(defn builtin-base64-decode
  [{[^String s] :args}]
  (base64/decode s))

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
       (catch Exception e (throw (errors/builtin-ex "eval_builtin_error: json.marshal: %s" (ex-message e)))))
     :cljs
     (try
       (json/write-str x)
       (catch js/Error e (throw (errors/builtin-ex "eval_builtin_error: json.marshal: %s" (ex-message e)))))))

(defn builtin-json-unmarshal
  [{[^String s] :args}]
  #?(:clj
     (try
       (json/read-str s)
       (catch EOFException _ (throw (errors/builtin-ex "eval_builtin_error: json.unmarshal: unexpected EOF")))
       (catch Exception e (throw (errors/builtin-ex "eval_builtin_error: json.unmarshal: %s" (ex-message e)))))
     :cljs
     (try
       (json/read-str s)
       (catch js/Error e (throw (errors/builtin-ex "eval_builtin_error: json.unmarshal: %s" (ex-message e)))))))

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
    (throw (errors/builtin-ex (str "hex.decode: " (non-hex-error-msg s))))))

#_{:clj-kondo/ignore #?(:clj [] :cljs [:unused-binding])}
(defn builtin-yaml-marshal
  [{[x] :args}]
  #?(:clj (try
            ; Notable difference from OPA: this YAML marshaller does not re-order the output, so we'll sort it beforehand
            (yaml/generate-string (cond->> x (map? x) (into (sorted-map))) :dumper-options {:flow-style :block})
            (catch Exception e (throw (errors/builtin-ex "eval_builtin_error: yaml.marshal: %s" (.getMessage e)))))
     :cljs (throw (ex-info "yaml.marshal not implemented" {:type :not-implemented}))))

; Mimic the error message from OPA
#_{:clj-kondo/ignore #?(:clj [] :cljs [:unused-private-var])}
(defn- yaml-unmarshal-err-msg [^String s]
  (when (str/includes? s "expected ',' or ']'")
    (when-let [line (re-find #"line [\d]+" s)]
      (str "yaml: " line  ": did not find expected ',' or ']'"))))

#_{:clj-kondo/ignore #?(:clj [] :cljs [:unused-binding])}
(defn builtin-yaml-unmarshal
  [{[s] :args}]
  #?(:clj  (try
             (yaml/parse-string s :keywords false)
             (catch Exception e (let [msg (.getMessage e)]
                                  (throw (errors/builtin-ex "eval_builtin_error: yaml.unmarshal: %s"
                                                            (or (yaml-unmarshal-err-msg msg) msg))))))
     :cljs (throw (ex-info "yaml.unmarshal not implemented" {:type :not-implemented}))))

#_{:clj-kondo/ignore #?(:clj [] :cljs [:unused-binding])}
(defn builtin-yaml-is-valid
  [{[s] :args}]
  #?(:clj  (not (errors/throws? #(yaml/parse-string s)))
     :cljs (throw (ex-info "yaml.is_valid not implemented" {:type :not-implemented}))))

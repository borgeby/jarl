(ns jarl.builtins.jwt
  (:require [clojure.string :as str]
            [jarl.builtins.utils :refer [str->bytes]]
            [jarl.encoding.base64 :as base64]
            [jarl.encoding.json :as json]
            [jarl.encoding.hex :as hex]
            [jarl.exceptions :as errors])
  (:import (org.jose4j.jwk JsonWebKeySet JsonWebKey$Factory JsonWebKey RsaJsonWebKey EllipticCurveJsonWebKey)
           (org.jose4j.keys.resolvers JwksVerificationKeyResolver VerificationKeyResolver X509VerificationKeyResolver)
           (org.jose4j.keys X509Util HmacKey)
           (java.util List Collections Map)
           (org.jose4j.jws JsonWebSignature)
           (java.security KeyFactory)
           (java.security.spec X509EncodedKeySpec)
           (java.security.interfaces RSAPublicKey ECPublicKey)
           (clojure.lang ExceptionInfo)
           (org.jose4j.lang UnresolvableKeyException JoseException)))

(def supported-algs #{"HS256" "HS384" "HS512" "ES256" "ES384" "ES512" "RS256" "RS384" "RS512" "PS256" "PS384" "PS512"})

(defn alg-str->alg [s]
  (when (some? s)
    (if (keyword? s) s (keyword (str/lower-case s)))))

(defn- split-jwt [jwt]
  (let [parts (str/split jwt #"\.") num-parts (count parts)]
    (if (= 3 num-parts)
      parts
      (if (= 1 num-parts)
        (throw (errors/builtin-ex "encoded JWT had no period separators"))
        (throw (errors/builtin-ex "encoded JWT must have 3 sections, found %s" num-parts))))))

(defn- strip-pem-wrapper [^String s]
  (if (str/starts-with? s "---")
    (str/join "\n" (-> (str/split-lines s) rest butlast))
    s))

(defn- base64-decode-bytes ^bytes [^String s part]
  (try
    (base64/url-decode-bytes s)
    (catch ExceptionInfo e
      (if (errors/builtin-ex? e)
        (throw (errors/builtin-ex "JWT %s had invalid encoding: %s" part (ex-message e)))
        (throw e)))))

(defn jwk-key-resolver ^VerificationKeyResolver [^String s]
  (reify VerificationKeyResolver
    (resolveKey [_ _ _]
      (try
        (.getKey (JsonWebKey$Factory/newJwk s))
        (catch Throwable _
          (throw (errors/builtin-ex "failed to parse a JWK key (set)")))))))

(defn strict-jwks-key-resolver ^VerificationKeyResolver [^String s]
  (-> (JsonWebKeySet. s)
      (.getJsonWebKeys)
      (JwksVerificationKeyResolver.)
      (doto (.setDisambiguateWithVerifySignature true))))

(defn valid? [^JsonWebSignature jws ^JsonWebKey key]
  (and (= (.getAlgorithmHeaderValue jws) (.getAlgorithm key))
       (.verifySignature (doto jws (.setKey (.getKey key))))))

(defn- try-strict-resolve [^String s jws nesting-context]
  (try
    (.resolveKey (strict-jwks-key-resolver s) jws nesting-context)
    (catch UnresolvableKeyException _ false)))

(defn jwks-key-resolver [^String s]
  (reify VerificationKeyResolver
    (resolveKey [_ jws nesting-context]
      (if-let [key (try-strict-resolve s jws nesting-context)]
        (.getKey ^JsonWebKey key)
        (.getKey ^JsonWebKey (first (filter #(valid? jws %)
                                            (-> s (JsonWebKeySet.) (.getJsonWebKeys)))))))))

(defn- es-alg? [alg]
  (contains? #{:es256 :es384 :es512} alg))

(defn- hs-alg? [alg]
  (contains? #{:hs256 :hs384 :hs512} alg))

(defn pem-key-resolver [^String s alg]
  (let [key-spec (-> s
                     (str/replace "-----BEGIN PUBLIC KEY-----" "")
                     (str/replace "-----END PUBLIC KEY-----" "")
                     (str/replace (System/lineSeparator) "")
                     (base64/decode-bytes)
                     (X509EncodedKeySpec.))
        public-key (.generatePublic (KeyFactory/getInstance (if (es-alg? alg) "EC" "RSA")) key-spec)]
    (reify VerificationKeyResolver
      (resolveKey [_ _ _]
        (if (es-alg? alg)
          (cast ECPublicKey public-key)
          (cast RSAPublicKey public-key))))))

(defn x509-key-resolver [^String s]
  (if-not (or (str/ends-with? s "-----END CERTIFICATE-----")
              (str/ends-with? s "-----END CERTIFICATE-----\n"))
    (throw (errors/builtin-ex "extra data after a PEM certificate block"))
    (try
      (let [der (strip-pem-wrapper s)]
        (-> (X509Util.)
            (.fromBase64Der der)
            (List/of)
            (X509VerificationKeyResolver.)
            (doto (.setTryAllOnNoThumbHeader true))))
      (catch Throwable _
        (throw (errors/builtin-ex "failed to parse a PEM certificate"))))))

(defn key-resolver-name [^String certificate]
  (if (str/starts-with?     certificate "{")
    (if (str/includes?      certificate "\"keys\"") "jwks" "jwk")
    (if (str/starts-with?   certificate "-----BEGIN PUBLIC KEY-----")
      "pem"
      (if (str/starts-with? certificate "-----BEGIN CERTIFICATE-----")
        "x509"
        (throw (errors/builtin-ex "failed to extract a Key from the PEM certificate"))))))

(defn key-resolver ^VerificationKeyResolver [certificate alg]
  (condp = (key-resolver-name certificate)
    "jwks" (jwks-key-resolver certificate)
    "jwk"  (jwk-key-resolver  certificate)
    "pem"  (pem-key-resolver  certificate alg)
    "x509" (x509-key-resolver certificate)
    (throw (errors/builtin-ex "no resolver found for %s" certificate))))

(defn verify-hmac [jwt ^String secret]
  (let [jws (doto (JsonWebSignature.)
                  (.setDoKeyValidation false)
                  (.setCompactSerialization jwt)
                  (.setKey (HmacKey. (str->bytes secret))))]
    (.verifySignature jws)))

(defn- verify-no-jwe [header]
  (if (contains? header "enc")
    (throw (errors/builtin-ex "JWT is a JWE object, which is not supported"))
    header))

(defn- strip-quotes [s]
  (cond-> s (and (str/starts-with? s "\"") (str/ends-with? s "\"")) (subs 1 (dec (count s)))))

(defn decode [jwt]
  (let [parts       (split-jwt jwt)
        header      (-> (first  parts) (base64-decode-bytes "header") (String.) json/read-str verify-no-jwe)
        payload-str (-> (second parts) (base64-decode-bytes "payload") (String.))
        sig         (-> (last   parts) (base64-decode-bytes "signature") hex/encode-bytes)]
    (if (= "JWT" (get header "cty"))
      (decode (strip-quotes payload-str)) ; nested JWT
      [header (json/read-str payload-str) sig])))

(defn verify-sig [jwt certificate alg]
  (let [parts  (split-jwt jwt)
        header (-> (first  parts) (base64-decode-bytes "header") (String.) json/read-str verify-no-jwe)]
    (if-not (= alg (alg-str->alg (get header "alg")))
      false
      (if (hs-alg? alg)
        (verify-hmac jwt certificate)
        (let [resolver (key-resolver certificate alg)
              jws (doto (JsonWebSignature.) (.setCompactSerialization jwt))]
          (try
            (.verifySignature (doto jws (.setKey (.resolveKey resolver jws (Collections/emptyList)))))
            (catch UnresolvableKeyException _ false)))))))

; Since not only the contents/data of a JWT decides the signature, but also the JSON _formatting_,
; we need to ensure that the JSON produced here looks the same as that produced by OPA.
; We should probably replace this with something more robust..
(defn opa-json-fmt [json-str]
  (-> json-str
      (str/replace #"(\":)(\S)" "$1 $2")   ; add whitespace after colon
      (str/replace #"(,)(\")"   "$1 $2"))) ; add whitespace after comma

(defn signing-key [^JsonWebKey jwk-key]
  (condp instance? jwk-key
    RsaJsonWebKey           (.getRsaPrivateKey ^RsaJsonWebKey           jwk-key)
    EllipticCurveJsonWebKey (.getEcPrivateKey  ^EllipticCurveJsonWebKey jwk-key)
    (.getKey jwk-key)))

(defn builtin-io-jwt-encode-sign [{[headers payload ^Map key] :args}]
  (let [encoded-payload (-> payload json/write-str opa-json-fmt base64/url-encode-no-pad)
        json-header     (-> headers json/write-str opa-json-fmt)
        jwk (try
              (JsonWebKey$Factory/newJwk key)
              (catch Throwable _
                (throw (errors/builtin-ex "failed to parse a JWK key"))))
        jws (doto (JsonWebSignature.)
                  (.setKey (signing-key jwk))
                  (.setEncodedPayload encoded-payload))]
    (.setFullHeaderAsJsonString (.getHeaders jws) json-header)
    (.getCompactSerialization jws)))

(defn parse-headers [headers-raw]
  (when (str/blank? headers-raw)
    (throw (errors/builtin-ex "unexpected end of JSON input")))
  (let [headers (try
                  (json/read-str headers-raw)
                  (catch Throwable e
                    (when (or (str/includes? (ex-message e) "JSON error (missing")
                              (str/includes? (ex-message e) "JSON error (unexpected character):"))
                      (throw (errors/builtin-ex "invalid character")))
                    (throw e)))]
    (when-not (get headers "alg")
      (throw (errors/builtin-ex "failed to create signer: unsupported signature algorithm")))
    (when-not (contains? supported-algs (get headers "alg"))
      (throw (errors/builtin-ex "unknown signature algorithm")))
    headers))

(defn builtin-io-jwt-encode-sign-raw [{[^String headers ^String payload ^String key] :args}]
  (let [headers-parsed (parse-headers headers)
        typ (get headers-parsed "typ")
        _ (when (or (and (not= typ "text/plain") (str/blank? payload))
                    (and (= typ "JWT") (= :error (errors/try-or #(json/read-str payload) :error))))
            (throw (errors/builtin-ex "type is JWT but payload is not JSON")))
        jwk (try
              (JsonWebKey$Factory/newJwk key)
              (catch Throwable _
                (throw (errors/builtin-ex "failed to parse a JWK key"))))
        jws (doto (JsonWebSignature.)
                  (.setKey (signing-key jwk))
                  (.setEncodedPayload (base64/url-encode-no-pad payload)))]
    (try
      (-> jws (.getHeaders) (.setFullHeaderAsJsonString headers))
      (catch JoseException e
        (let [match-unexpected (re-find #"Unexpected character \((.+)\)" (ex-message e))]
          (if (= 2 (count match-unexpected))
            (throw (errors/builtin-ex "invalid character '%s' looking for beginning of value"
                                      (last match-unexpected)))
            (throw e)))))
      (.getCompactSerialization jws)))

(defn builtin-io-jwt-verify-es256 [{[jwt certificate] :args}]
  (verify-sig jwt certificate :es256))

(defn builtin-io-jwt-verify-es384 [{[jwt certificate] :args}]
  (verify-sig jwt certificate :es384))

(defn builtin-io-jwt-verify-es512 [{[jwt certificate] :args}]
  (verify-sig jwt certificate :es512))

(defn builtin-io-jwt-verify-hs256 [{[jwt secret] :args}]
  (verify-sig jwt secret :hs256))

(defn builtin-io-jwt-verify-hs384 [{[jwt secret] :args}]
  (verify-sig jwt secret :hs384))

(defn builtin-io-jwt-verify-hs512 [{[jwt secret] :args}]
  (verify-sig jwt secret :hs512))

(defn builtin-io-jwt-verify-ps256 [{[jwt certificate] :args}]
  (verify-sig jwt certificate :ps256))

(defn builtin-io-jwt-verify-ps384 [{[jwt certificate] :args}]
  (verify-sig jwt certificate :ps384))

(defn builtin-io-jwt-verify-ps512 [{[jwt certificate] :args}]
  (verify-sig jwt certificate :ps512))

(defn builtin-io-jwt-verify-rs256 [{[jwt certificate] :args}]
  (verify-sig jwt certificate :rs256))

(defn builtin-io-jwt-verify-rs384 [{[jwt certificate] :args}]
  (verify-sig jwt certificate :rs384))

(defn builtin-io-jwt-verify-rs512 [{[jwt certificate] :args}]
  (verify-sig jwt certificate :rs512))

(defn builtin-io-jwt-decode [{[jwt] :args}]
  (decode jwt))

(defn verify-issuer [payload constraints]
  (if-let [iss (get constraints "iss")]
    (= iss (get payload "iss"))
    true))

(defn verify-aud [{:strs [aud]} constraints]
  (let [required-aud (get constraints "aud")]
    (if (nil? required-aud)
      (nil? aud)
      (if (string? aud)
        (= aud required-aud)
        ; aud may also be a vector
        (some #{required-aud} aud)))))

;  The time in nanoseconds to verify the token at. If this is present then the exp and nbf claims are compared against
;  this value. If it is absent then they are compared against the current time.
(defn verify-time [payload constraints]
  (let [time (double (/ (get constraints "time") 1000000000))
        nbf  (some-> (get payload "nbf") (double))
        exp  (some-> (get payload "exp") (double))]
    (and (or (nil? nbf) (<= nbf time))
         (or (nil? exp) (>  exp time)))))

(defn builtin-io-jwt-decode-verify [{[jwt constraints] :args bctx :builtin-context}]
  (split-jwt jwt)
  (let [[header payload _] (decode jwt)
        constraints (assoc constraints "time" (get constraints "time" (:time-now-ns bctx)))
        alg (alg-str->alg (or (get constraints "alg") (get header "alg")))]
    (if (some? alg)
      (if-let [certificate (get constraints (if (hs-alg? alg) "secret" "cert"))]
        (if (and (verify-sig jwt certificate alg)
                 (verify-issuer payload constraints)
                 (verify-aud payload constraints)
                 (verify-time payload constraints))
          [true  header payload]
          [false {} {}])
        [false {} {}])
      ; no alg
      [false {} {}])))

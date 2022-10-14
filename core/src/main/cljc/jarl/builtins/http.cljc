(ns jarl.builtins.http
  (:require [clojure.math :as math]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [jarl.builtins.utils :as utils]
            [jarl.exceptions :as errors]
            [jarl.encoding.json :as json]
            [jarl.encoding.yaml :as yaml]
            [jarl.http-client :as http-client]))

(def ^:private default-req-timeout 5000)

(defn status-code->status-text [code]
  (case code
    100 "100 Continue"
    101 "101 Switching Protocols"
    103 "103 Early Hints"
    200 "200 OK"
    201 "201 Created"
    202 "202 Accepted"
    203 "203 Non-Authoritative Information"
    204 "204 No Content"
    205 "205 Reset Content"
    206 "206 Partial Content"
    300 "300 Multiple Choices"
    301 "301 Moved Permanently"
    302 "302 Found"
    304 "304 Not Modified"
    307 "307 Temporary Redirect"
    308 "308 Permanent Redirect"
    400 "400 Bad Request"
    402 "402 Payment Required"
    404 "404 Not Found"
    405 "405 Method Not Allowed"
    406 "406 Not Acceptable"
    409 "409 Conflict"
    410 "410 Gone"
    411 "411 Length Required"
    412 "412 Precondition Failed"
    413 "413 Payload Too Large"
    414 "414 URI Too Long"
    416 "416 Range Not Satisfiable"
    417 "417 Expectation Failed"
    425 "425 Too Early"
    426 "426 Upgrade Required"
    428 "428 Precondition Required"
    429 "429 Too Many Requests"
    431 "431 Request Header Fields Too Large"
    451 "451 Unavailable For Legal Reasons"
    500 "500 Internal Server Error"
    501 "501 Not Implemented"
    502 "502 Bad Gateway"
    503 "503 Service Unavailable"
    504 "504 Gateway Timeout"
    505 "505 HTTP Version Not Supported"
    506 "506 Variant Also Negotiates"
    510 "510 Not Extended"
    511 "511 Network Authentication Required"
    (throw (ex-info "unknown http status code" {:code code}))))

(def allowed-keys
  #{"method"
    "url"
    "body"
    "enable_redirect"
    "force_json_decode"
    "force_yaml_decode"
    "headers"
    "raw_body"
    "tls_use_system_certs"
    "tls_ca_cert"
    "tls_ca_cert_file"
    "tls_ca_cert_env_variable"
    "tls_client_cert"
    "tls_client_cert_file"
    "tls_client_cert_env_variable"
    "tls_client_key"
    "tls_client_key_file"
    "tls_client_key_env_variable"
    "tls_insecure_skip_verify"
    "tls_server_name"
    "timeout"
    "cache"
    "force_cache"
    "force_cache_duration_seconds"
    "raise_error"
    "caching_mode"})

(def required-keys #{"method" "url"})

(defn- format-set [s]
  (str (str/join ", " (map #(str "\"" % "\"") s))))

; TODO: verify correct type of each provided value
(defn validate-request [request]
  (let [keys (set (keys request))
        invalid-keys (set/difference keys allowed-keys)
        missing-keys (set/difference required-keys keys)
        raise-error (get request "raise_error")]
    (when (seq invalid-keys)
      (throw (errors/type-ex "http.send: operand 1 invalid request parameters(s): {%s}", (format-set invalid-keys))))
    (when (seq missing-keys)
      (throw (errors/type-ex "http.send: operand 1 missing required request parameters(s): {%s}", (format-set missing-keys))))
    (when (and (some? raise-error) (not (boolean? raise-error)))
      (throw (errors/type-ex "http.send: invalid value for raise_error field")))))

(defn verify-url [s]
  (if (nil? (re-find #"https?://(\S+)" s))
    ; note: does not mimic OPA error messages yet
    (throw (errors/builtin-ex "http.send: invalid URL: %s" s))
    s))

(defn- to-json [x]
  (if (string? x)
    x
    (json/write-str x)))

(def ^:private unit->nano-map
  {"h"  3600000000000
   "m"  60000000000
   "s"  1000000000
   "ms" 1000000
   "us" 1000
   "µs" 1000
   "ns" 1})

(defn time-unit->nanos [[time unit :as time-unit]]
  (let [time (parse-double time)]
    (if (or (nil? time) (not (contains? unit->nano-map unit)))
      (throw (errors/builtin-ex "http.send: invalid timeout value: %s" time-unit))
      (* time (get unit->nano-map unit)))))

(defn- nanos->millis [nanos]
  (max 1 (math/round (/ nanos 1000000))))

(defn parse-timeout [x]
  (if (number? x)
    x
    (if (and (string? x) (utils/numeric? x))
      (parse-long x)
      (if-let [time-unit-pairs (map rest (re-seq #"([\.\d]+)(\p{L}+)" x))]
        (nanos->millis (reduce + (map time-unit->nanos time-unit-pairs)))
        (throw (errors/builtin-ex "http.send: invalid timeout value %s" x))))))

(defn create-request [request]
  (let [req  (partial get request)
        has? (partial contains? request)]
    (cond-> {:method               (keyword (str/lower-case (req "method")))
             :url                  (verify-url (req "url"))
             :follow-redirects     false
             :force-json-decode    false
             :force-yaml-decode    false
             :insecure?            false
             :raise-error          true
             :conn-timeout         default-req-timeout}
            (has? "enable_redirect")          (assoc :follow-redirects (req "enable_redirect"))
            (has? "body")                     (assoc :body (to-json (req "body")))
            (has? "raw_body")                 (assoc :body (req "raw_body"))
            (has? "headers")                  (assoc :headers (req "headers"))
            (has? "timeout")                  (assoc :conn-timeout (parse-timeout (req "timeout"))) ; millis here, not OPA nanos
            (has? "force_json_decode")        (assoc :force-json-decode (req "force_json_decode"))
            (has? "force_yaml_decode")        (assoc :force-yaml-decode (req "force_yaml_decode"))
            (has? "raise_error")              (assoc :raise-error (req "raise_error"))
            (has? "tls_insecure_skip_verify") (assoc :insecure? (req "tls_insecure_skip_verify")))))

(defn content-type [response]
  (when-let [ct (get-in response [:headers "content-type"])]
    (let [stop (or (str/index-of ct ";") (count ct))]
      (str/lower-case (subs ct 0 stop)))))

(defn response-body-decoded [request response]
  (let [ct (content-type response)]
    (cond
      (:force-json-decode request) (json/read-str (:body response))
      (:force-yaml-decode request) (yaml/read-str (:body response))
      (= ct "application/json")    (json/read-str (:body response))
      (= ct "application/yaml")    (yaml/read-str (:body response))
      (= ct "application/x-yaml")  (yaml/read-str (:body response))
      :else nil)))

(defn create-response [request response]
  (let [status-code (or (:status response) (get response "status"))]
    (if (zero? status-code)
      ; client (currently) responsible for formatting error response correctly
      (walk/stringify-keys response)
      (let [resp {"status_code" status-code
                  "status"      (status-code->status-text (:status response))
                  "headers"     (:headers response)
                  "body"        (response-body-decoded request response)
                  "raw_body"    (:body response)}]
        resp))))

(defn builtin-http-send
  [{[request] :args}]
  (validate-request request)
  (let [req (create-request request)
        res (http-client/send-request req)]
    (create-response req res)))

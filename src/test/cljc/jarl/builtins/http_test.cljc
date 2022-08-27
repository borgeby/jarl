(ns jarl.builtins.http-test
    (:require [jarl.builtins.http  :refer [builtin-http-send content-type create-request parse-timeout]]
              [jarl.exceptions     :as errors]
              [jarl.types          :refer [rego-equal?]]
              [jarl.http-client    :as http-client]
            #?(:clj  [clojure.test :refer [deftest is testing]]
               :cljs [cljs.test    :refer [deftest is testing]])))

(deftest content-type-test
  (testing "parsing content type"
    (is (= (content-type {:headers {"content-type" "application/json"}}) "application/json"))
    (is (= (content-type {:headers {"content-type" "application/x-yaml; charset=ISO-8859-1"}}) "application/x-yaml"))
    (is (nil? (content-type {})))))

(deftest parse-timout-test
  (testing "parse timout"
    (is (rego-equal? (parse-timeout "1h")       3600000))
    (is (rego-equal? (parse-timeout "1h1ns")    3600000))
    (is (rego-equal? (parse-timeout "1h1ms")    3600001))
    (is (rego-equal? (parse-timeout "1ns")            1))
    (is (rego-equal? (parse-timeout "1.5m3.5s") 93500))))

(deftest create-request-test
  (testing "create request, default values"
    (is (= (create-request     {"method" "get" "url" "https://foo.bar"})
           {:follow-redirects  false
            :force-json-decode false
            :force-yaml-decode false
            :insecure?         false
            :raise-error       true
            :conn-timeout      5000
            :method            :get
            :url               "https://foo.bar"})))

  (testing "create request, modified values"
    (is (= (create-request
             {"method"                    "PUT"
              "url"                       "https://foo.bar"
              "enable_redirect"           true
              "force_yaml_decode"         true
              "tls_insecure_skip_verify"  true
              "timeout"                   "1s500ms"
              "raise_error"               false})
           {:follow-redirects  true
            :force-json-decode false
            :force-yaml-decode true
            :insecure?         true
            :raise-error       false
            :conn-timeout      1500
            :method            :put
            :url               "https://foo.bar"}))))

(deftest http-send-test
  (testing "basic response handling"
    (with-redefs [http-client/send-request (fn [_] {:status 200 :headers {} :body "no coercion"})]
      (is (= (builtin-http-send {:args [{"method" "get" "url" "http://borge.by"}]})
             {"body"        nil
              "headers"     {}
              "raw_body"    "no coercion"
              "status"      "200 OK"
              "status_code" 200}))))

  (testing "json decode from content-type"
    (with-redefs [http-client/send-request (fn [_] {:status 200
                                                    :headers {"content-type" "application/json"}
                                                    :body "{\"foo\":\"bar\"}"})]
      (is (= (builtin-http-send {:args [{"method" "get" "url" "http://borge.by"}]})
             {"body"        {"foo" "bar"}
              "headers"     {"content-type" "application/json"}
              "raw_body"    "{\"foo\":\"bar\"}"
              "status"      "200 OK"
              "status_code" 200}))))

  (testing "force json decode"
    (with-redefs [http-client/send-request (fn [_] {:status 200 :headers {} :body "{\"foo\":\"bar\"}"})]
      (is (= (builtin-http-send {:args [{"method" "get" "url" "http://borge.by" "force_json_decode" true}]})
             {"body"        {"foo" "bar"}
              "headers"     {}
              "raw_body"    "{\"foo\":\"bar\"}"
              "status"      "200 OK"
              "status_code" 200})))))

(deftest http-send-error-handling-test
  (testing "unknown host error"
    (is (= (builtin-http-send {:args [{"method" "get" "url" "http://foo.bar"}]})
           {"error"  {"code"    "eval_http_send_network_error"
                      "message" "Get \"http://foo.bar\": dial tcp: lookup foo.bar: no such host"}
            "status" 0})))

  (testing "error response from server"
    (with-redefs [http-client/send-request (fn [_] {:status 500 :headers {} :body "error!"})]
      (is (= (builtin-http-send {:args [{"method" "get" "url" "http://borge.by"}]})
             {"body"        nil
              "headers"     {}
              "raw_body"    "error!"
              "status"      "500 Internal Server Error"
              "status_code" 500}))))

  (testing "exception thrown in client, raise_error"
    (with-redefs [http-client/request-fn (fn [_] (throw (ex-info "failed!" {:no :context})))]
      (let [bt-send #(builtin-http-send {:args [{"method" "get" "url" "http://borge.by"}]})
            ex (errors/try-return bt-send)]
        (is (errors/builtin-ex? ex))
        (is (= (ex-message ex) "eval_builtin_error: http.send: failed!")))))

  (testing "exception thrown in client, raise_error: false"
    (with-redefs [http-client/request-fn (fn [_] (throw (ex-info "failed!" {:no :context})))]
      (is (= (builtin-http-send {:args [{"method" "get" "url" "http://borge.by" "raise_error" false}]})
             {"error"  {"code"    "eval_http_send_network_error"
                        "message" "failed!"}
              "status" 0})))))

(ns jarl.builtins.http-test
  (:require [jarl.builtins.http :refer [builtin-http-send content-type create-request parse-timeout]]
            [jarl.exceptions :as errors]
            [jarl.types :refer [rego-equal?]]
            [jarl.http-client :as http-client]
            [jarl.time :as time]
            #?(:clj  [clojure.test :refer [deftest is testing]]
               :cljs [cljs.test :refer [deftest is testing]])
            [tick.core :as t]))

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
    (is (= (create-request                {"method" "get" "url" "https://foo.bar"})
           {:follow-redirects             false
            :force-cache                  false
            :force-cache-duration-seconds 0
            :force-json-decode            false
            :force-yaml-decode            false
            :insecure?                    false
            :raise-error                  true
            :conn-timeout                 5000
            :method                       :get
            :url                          "https://foo.bar"})))

  (testing "create request, modified values"
    (is (= (create-request
             {"method"                        "PUT"
              "url"                           "https://foo.bar"
              "force_cache"                   true
              "force_cache_duration_seconds"  15
              "enable_redirect"               true
              "force_yaml_decode"             true
              "tls_insecure_skip_verify"      true
              "timeout"                       "1s500ms"
              "raise_error"                   false})
           {:follow-redirects             true
            :force-cache                  true
            :force-cache-duration-seconds 15
            :force-json-decode            false
            :force-yaml-decode            true
            :insecure?                    true
            :raise-error                  false
            :conn-timeout                 1500
            :method                       :put
            :url                          "https://foo.bar"}))))

(deftest http-send-test
  (testing "basic response handling"
    (with-redefs [http-client/send-request (fn [_] {:status 200 :headers {} :body "no coercion"})]
      (is (= (builtin-http-send {:args [{"method" "get" "url" "http://borge.by"}] :builtin-context {:time-now-ns 1}})
             {"body"        nil
              "headers"     {}
              "raw_body"    "no coercion"
              "status"      "200 OK"
              "status_code" 200}))))

  (testing "json decode from content-type"
    (with-redefs [http-client/send-request (fn [_] {:status 200
                                                    :headers {"content-type" "application/json"}
                                                    :body "{\"foo\":\"bar\"}"})]
      (is (= (builtin-http-send {:args [{"method" "get" "url" "http://borge.by"}] :builtin-context {:time-now-ns 1}})
             {"body"        {"foo" "bar"}
              "headers"     {"content-type" "application/json"}
              "raw_body"    "{\"foo\":\"bar\"}"
              "status"      "200 OK"
              "status_code" 200}))))

  (testing "force json decode"
    (with-redefs [http-client/send-request (fn [_] {:status 200 :headers {} :body "{\"foo\":\"bar\"}"})]
      (is (= (builtin-http-send {:args [{"method" "get" "url" "http://borge.by" "force_json_decode" true}]
                                 :builtin-context {:time-now-ns 1}})
             {"body"        {"foo" "bar"}
              "headers"     {}
              "raw_body"    "{\"foo\":\"bar\"}"
              "status"      "200 OK"
              "status_code" 200}))))

  (testing "force cache"
    (let [counter (atom 0)
          req {"method" "get" "url" "http://borge.by" "force_cache" true "force_cache_duration_seconds" 30}]
      (with-redefs [http-client/send-request
                    (fn [_]
                      (swap! counter inc)
                      {:status 200 :headers {"content-type" "application/json"} :body "{\"foo\":\"bar\"}"})]
        (let [res {"body" {"foo" "bar"} "headers" {"content-type" "application/json"}
                   "raw_body" "{\"foo\":\"bar\"}" "status" "200 OK" "status_code" 200}]
          (is (= (builtin-http-send {:args [req] :builtin-context {:time-now-ns 1}}) res))
          (is (= (builtin-http-send {:args [req] :builtin-context {:time-now-ns 1}}) res))
          (is (= (builtin-http-send {:args [req] :builtin-context {:time-now-ns 1}}) res))
          (is (= @counter 1)))))))

(deftest http-send-caching-test
  (testing "force cache"
    (let [counter (atom 0)
          req {"method" "get" "url" "http://borge.by/foo" "force_cache" true "force_cache_duration_seconds" 30}]
      (with-redefs [http-client/send-request
                    (fn [_]
                      (swap! counter inc)
                      {:status 200 :headers {"content-type" "application/json"} :body "{\"foo\":\"bar\"}"})]
        (let [res {"body" {"foo" "bar"} "headers" {"content-type" "application/json"}
                   "raw_body" "{\"foo\":\"bar\"}" "status" "200 OK" "status_code" 200}]
          (is (= (builtin-http-send {:args [req] :builtin-context {:time-now-ns 1}}) res))
          (is (= (builtin-http-send {:args [req] :builtin-context {:time-now-ns 1}}) res))
          (is (= @counter 1))))))

  (testing "force cache duration"
    (let [counter (atom 0)
          req {"method" "get" "url" "http://borge.by/bar" "force_cache" true "force_cache_duration_seconds" 30}]
      (with-redefs [http-client/send-request
                    (fn [_]
                      (swap! counter inc)
                      {:status 200 :headers {"content-type" "application/json"} :body "{\"foo\":\"bar\"}"})]
        (let [res {"body" {"foo" "bar"} "headers" {"content-type" "application/json"}
                   "raw_body" "{\"foo\":\"bar\"}" "status" "200 OK" "status_code" 200}
              now   (time/now-ns)
              exp   (time/instant->ns (t/>> (t/now) (t/new-duration 60 :seconds)))
              bctx {:time-now-ns now}]
          (is (= (builtin-http-send {:args [req] :builtin-context bctx}) res))
          (is (= @counter 1))
          (is (= (builtin-http-send {:args [req] :builtin-context bctx}) res))
          (is (= @counter 1))
          (is (= (builtin-http-send {:args [req] :builtin-context {:time-now-ns exp}}) res)) ; fast-forward 1 minute
          (is (= @counter 2))))))

  (testing "cache from response headers, cache-control"
    (let [counter (atom 0)
          req {"method" "get" "url" "http://borge.by" "cache" true}]
      (with-redefs [http-client/send-request
                    (fn [_]
                      (swap! counter inc)
                      {:status 200
                       ; note upper-case keys
                       :headers {"Content-Type" "application/json"
                                 "Cache-Control" "ignored, max-age=30, also=ignored"
                                 "Date" "Sat, 01 Jan 2022 15:10:05 GMT"}
                       :body "{\"foo\":\"bar\"}"})]
        (let [res {"body" {"foo" "bar"}
                   "headers" {"content-type" "application/json"
                              "cache-control" "ignored, max-age=30, also=ignored"
                              "date" "Sat, 01 Jan 2022 15:10:05 GMT"}
                   "raw_body" "{\"foo\":\"bar\"}" "status" "200 OK" "status_code" 200}
              now   (t/instant (t/zoned-date-time "2022-01-01T15:10:05Z"))
              exp   (time/instant->ns (t/>> now (t/new-duration 60 :seconds)))
              bctx {:time-now-ns (time/instant->ns now)}]
          (is (= (builtin-http-send {:args [req] :builtin-context bctx}) res))
          (is (= @counter 1))
          (is (= (builtin-http-send {:args [req] :builtin-context bctx}) res))
          (is (= @counter 1))
          (is (= (builtin-http-send {:args [req] :builtin-context {:time-now-ns exp}}) res)) ; fast-forward 1 minute
          (is (= @counter 2))))))

  (testing "cache from response headers, expires"
    (let [counter (atom 0)
          req {"method" "get" "url" "http://borge.by/jarl" "cache" true}]
      (with-redefs [http-client/send-request
                    (fn [_]
                      (swap! counter inc)
                      {:status 200
                       :headers {"content-type" "application/json"
                                 "date" "Sat, 01 Jan 2022 15:10:05 GMT"
                                 "expires" "Sat, 01 Jan 2022 15:40:05 GMT"}
                       :body "{\"foo\":\"bar\"}"})]
        (let [res {"body" {"foo" "bar"}
                   "headers" {"content-type" "application/json"
                              "date" "Sat, 01 Jan 2022 15:10:05 GMT"
                              "expires" "Sat, 01 Jan 2022 15:40:05 GMT"}
                   "raw_body" "{\"foo\":\"bar\"}" "status" "200 OK" "status_code" 200}
              now   (t/instant (t/zoned-date-time "2022-01-01T15:10:05Z"))
              exp   (time/instant->ns (t/>> now (t/new-duration 60 :minutes)))
              bctx {:time-now-ns (time/instant->ns now)}]
          (is (= (builtin-http-send {:args [req] :builtin-context bctx}) res))
          (is (= @counter 1))
          (is (= (builtin-http-send {:args [req] :builtin-context bctx}) res))
          (is (= @counter 1))
          (is (= (builtin-http-send {:args [req] :builtin-context {:time-now-ns exp}}) res)) ; fast-forward 1 hour
          (is (= @counter 2)))))))

(deftest http-send-error-handling-test
  (testing "unknown host error"
    (is (= (builtin-http-send {:args [{"method" "get" "url" "http://foo.bar"}] :builtin-context {:time-now-ns 1}})
           {"error"  {"code"    "eval_http_send_network_error"
                      "message" "Get \"http://foo.bar\": dial tcp: lookup foo.bar: no such host"}
            "status" 0})))

  (testing "error response from server"
    (with-redefs [http-client/send-request (fn [_] {:status 500 :headers {} :body "error!"})]
      (is (= (builtin-http-send {:args [{"method" "get" "url" "http://borge.by"}] :builtin-context {:time-now-ns 1}})
             {"body"        nil
              "headers"     {}
              "raw_body"    "error!"
              "status"      "500 Internal Server Error"
              "status_code" 500}))))

  (testing "exception thrown in client, raise_error"
    (with-redefs [http-client/request-fn (fn [_] (throw (ex-info "failed!" {:no :context})))]
      (let [bt-send #(builtin-http-send {:args [{"method" "get" "url" "http://borge.by"}]
                                         :builtin-context {:time-now-ns 1}})
            ex (errors/try-return bt-send)]
        (is (errors/builtin-ex? ex))
        (is (= (ex-message ex) "http.send: failed!")))))

  (testing "exception thrown in client, raise_error: false"
    (with-redefs [http-client/request-fn (fn [_] (throw (ex-info "failed!" {:no :context})))]
      (is (= (builtin-http-send {:args [{"method" "get" "url" "http://borge.by" "raise_error" false}]
                                 :builtin-context {:time-now-ns 1}})
             {"error"  {"code"    "eval_http_send_network_error"
                        "message" "failed!"}
              "status" 0})))))

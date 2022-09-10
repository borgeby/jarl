(ns jarl.config.schema-test
  (:require #?(:clj  [clojure.test    :refer [deftest testing is]]
               :cljs [cljs.test       :refer [deftest testing is]])
            [jarl.config.reader       :as reader]
            [jarl.config.schema       :as schema]
            [jarl.encoding.json       :as json]
            [jarl.runtime.environment :as environment]))

(deftest services-array-to-map-conversion-test
  (testing "services array is converted to map"
    (let [raw {"services"
               [{"name" "foo" "url" "https://example.com/bundles"}
                {"name" "bar" "url" "https://example.com/bundles"}]}
          cfg (reader/json->config (json/write-str raw))
          parsed (schema/parse cfg)]
      (is (= parsed {:services
                     {:bar {:response-header-timeout-seconds 10
                            :tls                             {:system-ca-required false}
                            :type                            ""
                            :url                             "https://example.com/bundles"}
                      :foo {:response-header-timeout-seconds 10
                            :tls                             {:system-ca-required false}
                            :type                            ""
                            :url                             "https://example.com/bundles"}}})))))

(deftest from-string-to-config-test
  (testing "full working config"
    (let [raw {"services"
               {"foo"
                {"url" "https://example.com/bundles"
                 "response_header_timeout_seconds" 5
                 "headers" {"X-Made-Up" "yes"
                            "accept-language" "en-US"}}
                "bar"
                {"url" "http://localhost:8282"
                 "credentials"
                 {"bearer" {"token" "${TOKEN}"}}}}
               "bundles"
               {"foo"
                {"service" "foo"
                 "resource" "/bundle.tar.gz"
                 "trigger" "manual"}
                "bar"
                {"service" "bar"}}
               "labels"
               {"region" "EU"
                "namespace" "foobars"}}
          cfg (with-redefs [environment/*env* {"TOKEN" "secret123"}] (reader/json->config (json/write-str raw)))
          parsed (schema/parse cfg)]
      (is (= parsed {:bundles  {:bar {:polling {:max-delay-seconds 120
                                                :min-delay-seconds 60}
                                      :service "bar"
                                      :trigger "periodic"}
                                :foo {:polling  {:max-delay-seconds 120
                                                 :min-delay-seconds 60}
                                      :resource "/bundle.tar.gz"
                                      :service  "foo"
                                      :trigger  "manual"}}
                     :labels   {:namespace "foobars"
                                :region    "EU"}
                     :services {:bar {:credentials                     {:bearer {:scheme "Bearer"
                                                                                 :token  "secret123"}}
                                      :response-header-timeout-seconds 10
                                      :tls                             {:system-ca-required false}
                                      :type                            ""
                                      :url                             "http://localhost:8282"}
                                :foo {:headers                         {"accept-language" "en-US"
                                                                        "x-made-up"       "yes"}
                                      :response-header-timeout-seconds 5
                                      :tls                             {:system-ca-required false}
                                      :type                            ""
                                      :url                             "https://example.com/bundles"}}})))))

(deftest validation-test
  (testing "simple valid"
    (let [cfg (reader/json->config (json/write-str {"labels" {"foo" "bar"}}))
          parsed (schema/parse cfg)]
      (is (= (schema/validate parsed) {:valid true}))))

  (testing "simple error"
    (let [cfg (reader/json->config (json/write-str {"services" 1}))
          parsed (schema/parse cfg)]
      (is (= (schema/validate parsed) {:errors {:services ["invalid type"]}
                                       :valid  false})))))

(deftest empty-config-test
  (testing "parsing empty config"
    (let [parsed (schema/parse (reader/json->config (json/write-str {})))]
      (is (= parsed {}))
      (is (= (schema/validate parsed) {:valid true})))))

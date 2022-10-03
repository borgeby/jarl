(ns jarl.bundle-test
  (:require [clojure.test :refer [deftest is testing]]
            [jarl.bundle :as bundle]
            [jarl.io :as jio]))

(deftest bundle-create-extract-round-trip-test
  (testing "create and extracting bundle from map"
    (let [bundle-map {"/data.json"   "{\"foo\": \"bar\"}"
                      "/policy.rego" "package policy\n\nallow := false"}]
      (is (= (-> bundle-map
                 bundle/output-stream-fn
                 jio/piped-input-stream
                 bundle/extract)
             bundle-map)))))

(deftest extract-plan-test
  (testing "extract plan.json file from a bundle"
    (let [bundle-map {"/data.json"   "{\"foo\": \"bar\"}"
                      "/policy.rego" "package policy\n\nallow := false"
                      "/plan.json"   "{\"plan\": \"yes!\"}"}]
      (is (= (-> bundle-map
                 bundle/output-stream-fn
                 jio/piped-input-stream
                 bundle/extract-plan)
             "{\"plan\": \"yes!\"}")))))

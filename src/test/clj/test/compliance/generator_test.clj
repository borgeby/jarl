(ns test.compliance.generator-test
  (:require [clojure.test :refer [deftest testing is]]
            [test.compliance.generator :refer [inc-note]]))

(deftest inc-note-test
  (testing "no previous note"
    (is (= (inc-note "my-test" {})                                              "my-test")))
  (testing "with previous note"
    (is (= (inc-note "my-test" {"my-test" "value"})                             "my-test-2"))
    (is (= (inc-note "my-test" {"my-test" "value" "my-test-2" "value"})         "my-test-3"))
    (is (= (inc-note "my-test" {"my-test" "1" "my-test-2" "2" "my-test-3" "3"}) "my-test-4"))))

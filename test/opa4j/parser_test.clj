(ns opa4j.parser-test
  (:require [clojure.test :refer [deftest is testing]]
            [opa4j.parser :refer [parse-file]]))

(deftest simple-test
  (testing "A simple policy"
    (let [data (parse-file "rego/simple/plan.json")
          [name plan] (first (get data :plans))]
      (is (= name "simple"))
      (let [result-set (plan data {"x" 42})]
        (is (= result-set '({"result" {"p" true}})))))))

(deftest set-composition-test
  (testing "A policy with set-composition"
    (let [data (parse-file "rego/set-composition/plan.json")
          [name plan] (first (get data :plans))]
      (is (= name "test"))
      (let [result-set (plan data {})]
        (is (= result-set '({"result" {"p" #{"a" "b" "c"}}})))))))
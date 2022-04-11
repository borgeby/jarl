(ns opa4j.parser-test
  (:require [clojure.test :refer :all]
            [opa4j.parser :refer :all]
            [opa4j.parser :as parser]))

(deftest simple-test
  (testing "A simple policy"
    (let [data (parser/parse-file "rego/simple/plan.json")]
      (let [[name plan] (first (get data :plans))]
        (is (= name "simple"))
        (let [result-set (plan data {"x" 42})]
          (is (= result-set '({"result" {"p" true}}))))))))

(deftest set-composition-test
  (testing "A policy with set-composition"
    (let [data (parser/parse-file "rego/set-composition/plan.json")]
      (let [[name plan] (first (get data :plans))]
        (is (= name "test"))
        (let [result-set (plan data {})]
          (is (= result-set '({"result" {"p" #{"a" "b" "c"}}}))))))))
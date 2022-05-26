(ns jarl.parser-test
  (:require [clojure.test :refer [deftest is testing]]
            [jarl.parser :refer [parse-file]]
            [clojure.java.io :as io]))

(deftest simple-test
  (testing "A simple policy"
    (let [info (parse-file (io/resource "rego/simple/plan.json"))
          plans (get info :plans)]
      (let [[name plan] (get plans 0)]
        (is (= name "simple/p"))
        (let [result-set (plan info {} {"x" 42})]
          (is (= result-set '({"result" true})))))
      (let [[name plan] (get plans 1)]
        (is (= name "simple/q"))
        (let [result-set (plan info {} {"x" 42})]
          (is (= result-set '({"result" "bar"}))))))))

(deftest simple-data-test
  (testing "A simple policy expecting a data document"
    (let [info (parse-file (io/resource "rego/data/plan.json"))
          plans (get info :plans)]
      (is (= (count plans) 1))
      (let [[name plan] (get plans 0)]
        (is (= name "dta"))
        (let [result-set (plan info {"foo" "bar"} {})]
          (is (= result-set '({"result" {"p" true}}))))))))

(deftest set-composition-test
  (testing "A policy with set-composition"
    (let [info (parse-file (io/resource "rego/set-composition/plan.json"))
          [name plan] (first (get info :plans))]
      (is (= name "set_composition"))
      (let [result-set (plan info {} {})]
        (is (= result-set '({"result" {"p" #{"a" "b" "c"}}})))))))

(deftest object-composition-test
  (testing "A policy with object-composition"
    (let [info (parse-file (io/resource "rego/object-composition/plan.json"))
          [name plan] (first (get info :plans))]
      (is (= name "object_composition"))
      (let [result-set (plan info {} {})]
        (is (= result-set '({"result" {"p" {0 "a"
                                            1 "b"
                                            2 "c"}}})))))))

(deftest array-comprehension-test
  (testing "A policy with array-comprehension"
    (let [info (parse-file (io/resource "rego/array-comprehension/plan.json"))
          [name plan] (first (get info :plans))]
      (is (= name "array_comprehension/p"))
      (let [result-set (plan info {} {})]
        (is (= result-set '({"result" ["a" "b" "b" "c"]})))))))

(deftest set-comprehension-test
  (testing "A policy with set-comprehension"
    (let [info (parse-file (io/resource "rego/set-comprehension/plan.json"))
          [name plan] (first (get info :plans))]
      (is (= name "set_comprehension/p"))
      (let [result-set (plan info {} {})]
        (is (= result-set '({"result" #{"a" "b" "c"}})))))))

(deftest object-comprehension-test
  (testing "A policy with object-comprehension"
    (let [info (parse-file (io/resource "rego/object-comprehension/plan.json"))
          [name plan] (first (get info :plans))]
      (is (= name "object_comprehension/p"))
      (let [result-set (plan info {} {})]
        (is (= result-set '({"result" {0 "a"
                                       1 "b"
                                       2 "c"}})))))))

(deftest array-built-ins
  (testing "A policy with array.* built-ins"
    (let [info (parse-file (io/resource "rego/array_built-ins/plan.json"))
          [name plan] (first (get info :plans))]
      (is (= name "array_built_ins/a"))
      (let [result-set (plan info {} {})]
        (is (= result-set '({"result" ["a" "b" "c" "d"]})))))))

(ns jarl.parser-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.java.io :as io]
            [jarl.parser :refer [parse-json]]))

; TODO: this is not compatible with ClojureScript unless using Node, since we're reading files from disk,
;       and to do this in the browser we'd need to launch a server to fetch from.. but, good enough for now

(deftest simple-test
  (testing "A simple policy"
    (let [info (parse-json (slurp (io/resource "rego/simple/plan.json")))
          plans (get info :plans)]
      (let [[name plan] (get plans 0)]
        (is (= name "simple/p"))
        (let [result-set (plan info {"x" 42} {})]
          (is (= result-set '({"result" true})))))
      (let [[name plan] (get plans 1)]
        (is (= name "simple/q"))
        (let [result-set (plan info {} {"x" 42})]
          (is (= result-set '({"result" "bar"}))))))))

(deftest simple-data-test
  (testing "A simple policy expecting a data document"
    (let [info (parse-json (slurp (io/resource "rego/data/plan.json")))
          plans (get info :plans)]
      (is (= (count plans) 1))
      (let [[name plan] (get plans 0)]
        (is (= name "dta"))
        (let [result-set (plan info {} {"foo" "bar"})]
          (is (= result-set '({"result" {"p" true}}))))))))

(deftest set-composition-test
  (testing "A policy with set-composition"
    (let [info (parse-json (slurp (io/resource "rego/set-composition/plan.json")))
          [name plan] (first (get info :plans))]
      (is (= name "set_composition"))
      (let [result-set (plan info {} {})]
        (is (= result-set '({"result" {"p" ["a" "b" "c"]}})))))))

(deftest object-composition-test
  (testing "A policy with object-composition"
    (let [info (parse-json (slurp (io/resource "rego/object-composition/plan.json")))
          [name plan] (first (get info :plans))]
      (is (= name "object_composition"))
      (let [result-set (plan info {} {})]
        (is (= result-set '({"result" {"p" {"0" "a"
                                            "1" "b"
                                            "2" "c"}}})))))))

(deftest array-comprehension-test
  (testing "A policy with array-comprehension"
    (let [info (parse-json (slurp (io/resource "rego/array-comprehension/plan.json")))
          [name plan] (first (get info :plans))]
      (is (= name "array_comprehension/p"))
      (let [result-set (plan info {} {})]
        (is (= result-set '({"result" ["a" "b" "b" "c"]})))))))

(deftest set-comprehension-test
  (testing "A policy with set-comprehension"
    (let [info (parse-json (slurp (io/resource "rego/set-comprehension/plan.json")))
          [name plan] (first (get info :plans))]
      (is (= name "set_comprehension/p"))
      (let [result-set (plan info {} {})]
        (is (= result-set '({"result" ["a" "b" "c"]})))))))

(deftest object-comprehension-test
  (testing "A policy with object-comprehension"
    (let [info (parse-json (slurp (io/resource "rego/object-comprehension/plan.json")))
          [name plan] (first (get info :plans))]
      (is (= name "object_comprehension/p"))
      (let [result-set (plan info {} {})]
        (is (= result-set '({"result" {"0" "a"
                                       "1" "b"
                                       "2" "c"}})))))))

(deftest array-built-ins-test
  (testing "A policy with array.* built-ins"
    (let [info (parse-json (slurp (io/resource "rego/array_built-ins/plan.json")))
          [name plan] (first (get info :plans))]
      (is (= name "array_built_ins/a"))
      (let [result-set (plan info {} {})]
        (is (= result-set '({"result" ["a" "b" "c" "d"]})))))))

(deftest aggregates-test
  (testing "A policy with aggregate"
    (let [info (parse-json (slurp (io/resource "rego/aggregates/plan.json")))
          [name plan] (first (get info :plans))]
      (is (= name "agg"))
      (let [result-set (plan info {} {"a" [1 2 3 4]})]
        (is (= result-set '({"result" {"p" [4]
                                       "q" [1 2 3 4]
                                       "r" true}})))))))

(deftest with-test
  (testing "A policy with while keyword"
    (let [info (parse-json (slurp (io/resource "rego/with/plan.json")))
          [name plan] (first (get info :plans))]
      (is (= name "w"))
      (let [result-set (plan info {} {"a" "bar"})]
        (is (= result-set '({"result" {"p" true}})))))))

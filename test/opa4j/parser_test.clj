;Copyright 2022 Johan Fylling, Anders Eknert
;
;Licensed under the Apache License, Version 2.0 (the "License");
;you may not use this file except in compliance with the License.
;You may obtain a copy of the License at
;
;       http://www.apache.org/licenses/LICENSE-2.0
;
;Unless required by applicable law or agreed to in writing, software
;distributed under the License is distributed on an "AS IS" BASIS,
;WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;See the License for the specific language governing permissions and
;limitations under the License.

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
      (is (= name "set_composition"))
      (let [result-set (plan data {})]
        (is (= result-set '({"result" {"p" #{"a" "b" "c"}}})))))))

(deftest object-composition-test
  (testing "A policy with object-composition"
    (let [data (parse-file "rego/object-composition/plan.json")
          [name plan] (first (get data :plans))]
      (is (= name "object_composition"))
      (let [result-set (plan data {})]
        (is (= result-set '({"result" {"p" {0 "a"
                                            1 "b"
                                            2 "c"}}})))))))

(deftest array-comprehension-test
  (testing "A policy with array-comprehension"
    (let [data (parse-file "rego/array-comprehension/plan.json")
          [name plan] (first (get data :plans))]
      (is (= name "array_comprehension/p"))
      (let [result-set (plan data {})]
        (is (= result-set '({"result" ["a" "b" "b" "c"]})))))))

(deftest set-comprehension-test
  (testing "A policy with set-comprehension"
    (let [data (parse-file "rego/set-comprehension/plan.json")
          [name plan] (first (get data :plans))]
      (is (= name "set_comprehension/p"))
      (let [result-set (plan data {})]
        (is (= result-set '({"result" #{"a" "b" "c"}})))))))

(deftest object-comprehension-test
  (testing "A policy with object-comprehension"
    (let [data (parse-file "rego/object-comprehension/plan.json")
          [name plan] (first (get data :plans))]
      (is (= name "object_comprehension/p"))
      (let [result-set (plan data {})]
        (is (= result-set '({"result" {0 "a"
                                       1 "b"
                                       2 "c"}})))))))
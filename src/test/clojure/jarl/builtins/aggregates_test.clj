(ns jarl.builtins.aggregates_test
  (:require [clojure.test :refer [deftest is testing]]
            [jarl.builtins.aggregates :refer [builtin-count builtin-sum builtin-product
                                              builtin-max builtin-min builtin-sort]])
  (:import (se.fylling.jarl BuiltinException)))

(deftest builtin-count-test
  (testing "count"
    (is (= (builtin-count []) 0))
    (is (= (builtin-count [1]) 1))
    (is (= (builtin-count [1 "a"]) 2))
    (is (= (builtin-count "abc") 3))
    (is (= (builtin-count "🍧🍨🧁🍰🍮") 5)))
  (testing "multiple codepoints"
    (is (= (builtin-count "🇩🇪") 2))))

(deftest builtin-sum-test
  (testing "sum"
    (is (= (builtin-sum []) 0))
    (is (= (builtin-sum [0]) 0))
    (is (= (builtin-sum [1 1]) 2))
    (is (= (builtin-sum [1 1.9]) 2.9))
    (is (= (builtin-sum [1.0 1.0]) 2))
    (is (= (builtin-sum #{3 2 1}) 6)))
  (testing "negative numbers"
    (is (= (builtin-sum [-1 -1 -1]) -3))
    (is (= (builtin-sum [-1.5 1.5 -1]) -1)))
  (testing "only numbers"
    (is (thrown-with-msg? BuiltinException
                          #"operand must be array or set of number but got array or set containing string"
                          (builtin-sum [1 "3"])))))

(deftest builtin-product-test
  (testing "product"
    (is (= (builtin-product []) 1))
    (is (= (builtin-product [2]) 2))
    (is (= (builtin-product [0]) 0))
    (is (= (builtin-product [1 1]) 1))
    (is (= (builtin-product [2.5 3]) 7.5))
    (is (= (builtin-product [10.0 10.000 -2]) -200))
    (is (= (builtin-product #{3 2 1}) 6)))
  (testing "only numbers"
    (is (thrown-with-msg? BuiltinException
                          #"operand must be array or set of number but got array or set containing string"
                          (builtin-sum [1 "3"])))))

(deftest builtin-max-test
  (testing "max"
    (is (= (builtin-max #{3 2 1}) 3))
    (is (= (builtin-max [2 1 nil]) 2))
    (is (= (builtin-max [#{1} "str" 10 {"x" 3} [2.2] 1 nil false]) #{1}))))

(deftest builtin-min-test
  (testing "min"
    (is (= (builtin-min #{3 2 1}) 1))
    (is (= (builtin-min [2 1 nil]) nil))
    (is (= (builtin-min [#{1} "str" 10 {"x" 3} [2.2] 1 nil false]) nil))))

(deftest builtin-sort-test
  (testing "sort"
    (is (= (builtin-sort #{3 2 1}) [1 2 3]))
    (is (= (builtin-sort [2 1 nil]) [nil 1 2]))
    (is (= (builtin-sort [#{1} "str" 10 {"x" 3} [2.2] 1 nil false])
           [nil false 1 10 "str" [2.2] {"x" 3} #{1}]))))

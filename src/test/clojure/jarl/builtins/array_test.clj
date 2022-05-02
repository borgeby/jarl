(ns jarl.builtins.array_test
  (:require [clojure.test :refer [deftest is testing]]
            [jarl.builtins.array :refer [builtin-concat builtin-reverse builtin-slice]])
  (:import (se.fylling.jarl BuiltinException)))

(deftest builtin-concat-test
  (testing "concat arrays"
    (is (= (builtin-concat [1 2] [3]) [1 2 3]))
    (is (= (builtin-concat [1 2 3] []) [1 2 3]))
    (is (= (builtin-concat [1] ["a"]) [1 "a"]))))

(deftest builtin-concat-test-exceptions
  (testing "concat non-arrays"
    (is (thrown-with-msg? BuiltinException #"array.concat: operand 1 must be array but got string" (builtin-concat "a" [3])))
    (is (thrown-with-msg? BuiltinException #"array.concat: operand 2 must be array but got string" (builtin-concat [3] "a")))))

(deftest builtin-reverse-test
  (testing "reverse arrays"
    (is (= (builtin-reverse [1 2 3]) [3 2 1]))
    (is (= (builtin-reverse ["c" "b" "a"]) ["a" "b" "c"]))
    (is (= (builtin-reverse []) []))))

(deftest builtin-reverse-test-exceptions
  (testing "reverse non-arrays"
    (is (thrown-with-msg? BuiltinException #"array.reverse: operand 1 must be array but got string" (builtin-reverse "abc")))
    (is (thrown-with-msg? BuiltinException #"array.reverse: operand 1 must be array but got number" (builtin-reverse 12345)))))

(deftest builtin-slice-test
  (testing "slice arrays"
    (is (= [1 2 3] (builtin-slice [1 2 3 4 5] 0 3)))
    (is (= [3 4] (builtin-slice [1 2 3 4 5] 2 4))))
  (testing "stop > start"
    (is (= [] (builtin-slice [1 2 3 4 5] 4 1))))
  (testing "out of bounds"
    (is (= [1 2 3] (builtin-slice [1 2 3] -10 10)))))

(deftest builtin-slice-test-exceptions
  (testing "slice type errors"
    (is (thrown-with-msg? BuiltinException #"array.slice: operand 1 must be array but got string" (builtin-slice "10" -10 10)))
    (is (thrown-with-msg? BuiltinException #"array.slice: operand 2 must be number but got floating-point number" (builtin-slice [10] 10.01 10)))
    (is (thrown-with-msg? BuiltinException #"array.slice: operand 3 must be number but got string" (builtin-slice [10] 0 "f")))))

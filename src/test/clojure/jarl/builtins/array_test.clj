(ns jarl.builtins.array-test
  (:require [clojure.test :refer [deftest]]
            [test.utils :refer [testing-builtin]])
  (:import (se.fylling.jarl TypeException)))

(deftest builtin-array-concat-test
  (testing-builtin "array.concat"
    [[1 2] [3]]   [1 2 3]
    [[1 2 3] []]  [1 2 3]
    [[1] ["a"]]   [1 "a"]))

(deftest builtin-array-reverse-test
  (testing-builtin "array.reverse"
    [[1 2 3]]         [3 2 1]
    [["c" "b" "a"]]   ["a" "b" "c"]
    [[]]              []))

(deftest builtin-array-slice-test
  (testing-builtin "array.slice"
    [[1 2 3 4 5] 0 3]     [1 2 3]
    [[1 2 3 4 5] 2 4]     [3 4]
    [[1 2 3 4 5] 4 1]     []
    [[1 2 3] -10 10]      [1 2 3]
    [[10] 10.01 10]       [TypeException "operand 2 must be integer but got floating-point number"]
    [[10] 10 10.10]       [TypeException "operand 3 must be integer but got floating-point number"]))

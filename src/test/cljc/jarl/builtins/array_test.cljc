(ns jarl.builtins.array-test
  (:require  [test.utils   :refer [testing-builtin]]
    #?(:clj  [clojure.test :refer [deftest]]
       :cljs [cljs.test    :refer [deftest]])))

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
    [[1 2 3] -10 10]      [1 2 3]))

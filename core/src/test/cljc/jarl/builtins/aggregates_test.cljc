(ns jarl.builtins.aggregates-test
  (:require  [test.utils   :refer [testing-builtin]]
    #?(:clj  [clojure.test :refer [deftest]]
       :cljs [cljs.test    :refer [deftest]])))

(deftest builtin-count-test
  (testing-builtin "count"
    [[]]               0
    [[1]]              1
    [[1 "a"]]          2
    ["abc"]            3
    [(vec (range 11))] 11
    ["🍧🍨🧁🍰🍮"]      5
    ["🇩🇪"]             2))

(deftest builtin-sum-test
  (testing-builtin "sum"
    [[]]             0
    [[0]]            0
    [[1 1]]          2
    [[1 1.9]]        2.9
    [[1.0 1.0]]      2
    [#{3 2 1}]       6
    [[-1 -1 -1]]    -3
    [[-1.5 1.5 -1]] -1
    [[1 "3"]]       [:jarl.exceptions/type-exception
                     "operand must be array or set of number but got array or set containing string"]))

(deftest builtin-product-test
  (testing-builtin "product"
    [[]]                1
    [[2]]               2
    [[0]]               0
    [[1 1]]             1
    [[2.5 3]]           7.5
    [[10.0 10.000 -2]]  -200
    [#{3 2 1}]          6
    [[1 "3"]]           [:jarl.exceptions/type-exception
                         "operand must be array or set of number but got array or set containing string"]))

(deftest builtin-max-test
  (testing-builtin "max"
    [#{3 2 1}]                                  3
    [[2 1 nil]]                                 2
    [[#{1} "str" 10 {"x" 3} [2.2] 1 nil false]] #{1}))

(deftest builtin-min-test
  (testing-builtin "min"
    [#{3 2 1}]                                  1
    [[2 1 nil]]                                 nil
    [[#{1} "str" 10 {"x" 3} [2.2] 1 nil false]] nil))

(deftest builtin-sort-test
  (testing-builtin "sort"
    [#{3 2 1}]                                  [1 2 3]
    [[2 1 nil]]                                 [nil 1 2]
    [[#{1} "str" 10 {"x" 3} [2.2] 1 nil false]] [nil false 1 10 "str" [2.2] {"x" 3} #{1}]))

(deftest builtin-internal-member-2-test
  (testing-builtin "internal.member_2"
    [1 [1]] true
    [{"foo" {"baz" 2000}} [{"foo" {"baz" 2000}}]] true))

(deftest builtin-internal-member-3-test
  (testing-builtin "internal.member_3"
    [0 1 [1]] true
    ["foo" {"baz" 2000} {"foo" {"baz" 2000}}] true))

(ns jarl.builtins.conversions-test
  (:require  [test.utils   :refer [testing-builtin]]
    #?(:clj  [clojure.test :refer [deftest]]
       :cljs [cljs.test    :refer [deftest]])))

(deftest to-number-test
  (testing-builtin "to_number"
    ["1"]       1
    ["1.12345"] 1.12345
    ["-22"]     -22
    ["-3.14"]   -3.14
    [true]      1
    [false]     0
    [nil]       0))

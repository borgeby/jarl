(ns jarl.builtins.conversions-test
  (:require [clojure.test :refer [deftest]]
            [test.utils :refer [testing-builtin]]))

(deftest to-number-test
  (testing-builtin "to_number"
    ["1"]       1
    ["1.12345"] 1.12345
    ["-22"]     -22
    ["-3.14"]   -3.14
    [true]      1
    [false]     0
    [nil]       0))

(ns jarl.builtins.bits-test
  (:require [clojure.test :refer [deftest]]
            [test.utils :refer [testing-builtin]]))

(deftest builtin-bits-or-test
  (testing-builtin "bits.or"
    [13 10]   15
    [10 -10]  -2
    [-10 -10] -10
    [10.1 1]  [:jarl.exceptions/type-exception "operand 1 must be integer number but got floating-point number"]
    [2 2.22]  [:jarl.exceptions/type-exception "operand 2 must be integer number but got floating-point number"]))

(deftest builtin-bits-and-test
  (testing-builtin "bits.and"
    [13 10]  8
    [10 -10] 2
    [100 5]  4
    [10.1 1] [:jarl.exceptions/type-exception "operand 1 must be integer number but got floating-point number"]
    [2 2.22] [:jarl.exceptions/type-exception "operand 2 must be integer number but got floating-point number"]))

(deftest builtin-bits-negate-test
  (testing-builtin "bits.negate"
    [100]  -101
    [2]    -3
    [-33]  32
    [10.1] [:jarl.exceptions/type-exception "operand 1 must be integer number but got floating-point number"]))

(deftest builtin-bits-xor-test
  (testing-builtin "bits.xor"
    [10 20]   30
    [10 -20]  -26
    [-10 -20] 26
    [10.1 1]  [:jarl.exceptions/type-exception "operand 1 must be integer number but got floating-point number"]
    [2 2.22]  [:jarl.exceptions/type-exception "operand 2 must be integer number but got floating-point number"]))

; Note: Clojure shift left/right functions take negative numbers, Rego does not

(deftest builtin-bits-lsh-test
  (testing-builtin "bits.lsh"
    [10 20]                 10485760
    [1 4]                   16
    [1 8]                   256
    [9223372036854775807 1] 18446744073709551614N
    ; non-integers
    [10.1 1] [:jarl.exceptions/type-exception "operand 1 must be integer number but got floating-point number"]
    [2 2.22] [:jarl.exceptions/type-exception "operand 2 must be integer number but got floating-point number"]
    ; negative integers
    [-10 1]  [:jarl.exceptions/type-exception "operand 1 must be an unsigned integer number but got a negative integer"]
    [2 -2]   [:jarl.exceptions/type-exception "operand 2 must be an unsigned integer number but got a negative integer"]))

(deftest builtin-bits-rsh-test
  (testing-builtin "bits.rsh"
    [10 20] 0
    [10 3]  1
    [100 3] 12
    ; non-integers
    [10.1 1] [:jarl.exceptions/type-exception "operand 1 must be integer number but got floating-point number"]
    [2 2.22] [:jarl.exceptions/type-exception "operand 2 must be integer number but got floating-point number"]
    ; negative integers
    [-10 1]  [:jarl.exceptions/type-exception "operand 1 must be an unsigned integer number but got a negative integer"]
    [2 -2]   [:jarl.exceptions/type-exception "operand 2 must be an unsigned integer number but got a negative integer"]))

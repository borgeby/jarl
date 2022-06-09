(ns jarl.builtins.bits_test
  (:require [clojure.test :refer [deftest is testing]]
            [jarl.builtins.bits :refer [builtin-bits-or builtin-bits-and builtin-bits-negate
                                        builtin-bits-xor builtin-bits-lsh builtin-bits-rsh]])
  (:import (se.fylling.jarl UndefinedException)))

(deftest builtin-bits-or-test
  (testing "bits.or"
    (is (= (builtin-bits-or 13 10) 15))
    (is (= (builtin-bits-or 10 -10) -2))
    (is (= (builtin-bits-or -10 -10) -10)))
  (testing "non-integers"
    (is (thrown-with-msg? UndefinedException
                          #"eval_type_error: bits.or: operand 1 must be integer number but got floating-point number"
                          (builtin-bits-or 10.1 1)))
    (is (thrown-with-msg? UndefinedException
                          #"eval_type_error: bits.or: operand 2 must be integer number but got floating-point number"
                          (builtin-bits-or 2 2.22)))))

(deftest builtin-bits-and-test
  (testing "bits.and"
    (is (= (builtin-bits-and 13 10) 8))
    (is (= (builtin-bits-and 10 -10) 2))
    (is (= (builtin-bits-and 100 5) 4)))
  (testing "non-integers"
    (is (thrown-with-msg? UndefinedException
                          #"eval_type_error: bits.and: operand 1 must be integer number but got floating-point number"
                          (builtin-bits-and 10.1 1)))
    (is (thrown-with-msg? UndefinedException
                          #"eval_type_error: bits.and: operand 2 must be integer number but got floating-point number"
                          (builtin-bits-and 2 2.22)))))

(deftest builtin-bits-negate-test
  (testing "bits.negate"
    (is (= (builtin-bits-negate 100) -101))
    (is (= (builtin-bits-negate 2) -3))
    (is (= (builtin-bits-negate -33) 32)))
  (testing "non-integers"
    (is (thrown-with-msg? UndefinedException
                          #"eval_type_error: bits.negate: operand 1 must be integer number but got floating-point number"
                          (builtin-bits-negate 10.1)))))

(deftest builtin-bits-xor-test
  (testing "bits.xor"
    (is (= (builtin-bits-xor 10 20) 30))
    (is (= (builtin-bits-xor 10 -20) -26))
    (is (= (builtin-bits-xor -10 -20) 26)))
  (testing "non-integers"
    (is (thrown-with-msg? UndefinedException
                          #"eval_type_error: bits.xor: operand 1 must be integer number but got floating-point number"
                          (builtin-bits-xor 10.1 1)))
    (is (thrown-with-msg? UndefinedException
                          #"eval_type_error: bits.xor: operand 2 must be integer number but got floating-point number"
                          (builtin-bits-xor 2 2.22)))))

; Note: Clojure shift left/right functions take negative numbers, Rego does not

(deftest builtin-bits-lsh-test
  (testing "bits.lsh"
    (is (= (builtin-bits-lsh 10 20) 10485760))
    (is (= (builtin-bits-lsh 1 4) 16))
    (is (= (builtin-bits-lsh 1 8) 256))
    (is (= (builtin-bits-lsh 9223372036854775807 1) 18446744073709551614N)))
  (testing "non-integers"
    (is (thrown-with-msg? UndefinedException
                          #"eval_type_error: bits.lsh: operand 1 must be integer number but got floating-point number"
                          (builtin-bits-lsh 10.1 1)))
    (is (thrown-with-msg? UndefinedException
                          #"eval_type_error: bits.lsh: operand 2 must be integer number but got floating-point number"
                          (builtin-bits-lsh 2 2.22))))
  (testing "negative integers"
    (is (thrown-with-msg? UndefinedException
                          #"eval_type_error: bits.lsh: operand 1 must be an unsigned integer number but got a negative number"
                          (builtin-bits-lsh -10 1)))
    (is (thrown-with-msg? UndefinedException
                          #"eval_type_error: bits.lsh: operand 2 must be an unsigned integer number but got a negative number"
                          (builtin-bits-lsh 2 -2)))))

(deftest builtin-bits-rsh-test
  (testing "bits.rsh"
    (is (= (builtin-bits-rsh 10 20) 0))
    (is (= (builtin-bits-rsh 10 3) 1))
    (is (= (builtin-bits-rsh 100 3) 12)))
  (testing "non-integers"
    (is (thrown-with-msg? UndefinedException
                          #"eval_type_error: bits.rsh: operand 1 must be integer number but got floating-point number"
                          (builtin-bits-rsh 10.1 1)))
    (is (thrown-with-msg? UndefinedException
                          #"eval_type_error: bits.rsh: operand 2 must be integer number but got floating-point number"
                          (builtin-bits-rsh 2 2.22))))
  (testing "negative integers"
    (is (thrown-with-msg? UndefinedException
                          #"eval_type_error: bits.rsh: operand 1 must be an unsigned integer number but got a negative number"
                          (builtin-bits-rsh -10 1)))
    (is (thrown-with-msg? UndefinedException
                          #"eval_type_error: bits.rsh: operand 2 must be an unsigned integer number but got a negative number"
                          (builtin-bits-rsh 2 -2)))))

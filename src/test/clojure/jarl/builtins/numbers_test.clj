(ns jarl.builtins.numbers_test
  (:require [clojure.test :refer [deftest is testing]]
            [jarl.builtins.numbers :refer [builtin-plus builtin-minus builtin-mul builtin-div builtin-rem builtin-round
                                           builtin-ceil builtin-floor builtin-abs builtin-numbers-range]])
  (:import (se.fylling.jarl UndefinedException JarlException)))

(deftest builtin-plus-test
  (testing "plus"
    (is (= (builtin-plus 1 1) 2))
    (is (= (builtin-plus 1.5 1.5) 3))
    (is (= (builtin-plus 1.6 1.6) 3.2))
    (is (= (builtin-plus -1.0 1) 0))))

(deftest builtin-minus-test
  (testing "minus"
    (is (= (builtin-minus 1 1) 0))
    (is (= (builtin-minus 1.5 2) -0.5))
    (is (= (builtin-minus -1.5 -4.5) 3))
    (is (= (builtin-minus -1.0 1) -2)))
  (testing "set difference"
    (is (= (builtin-minus #{1 2 3} #{2 3}) #{1}))
    (is (= (builtin-minus #{1 2 3} #{4}) #{1 2 3}))
    (is (= (builtin-minus #{1 2 3} #{}) #{1 2 3}))
    (is (= (builtin-minus #{1 2 3} #{2 3 4}) #{1}))))

(deftest builtin-mul-test
  (testing "mul"
    (is (= (builtin-mul 1 1) 1))
    (is (= (builtin-mul 1.5 2) 3))
    (is (= (builtin-mul -1.5 -4.5) 6.75))
    (is (= (builtin-mul -1.0 1) -1))))

(deftest builtin-div-test
  (testing "div"
    (is (= (builtin-div 1 1) 1))
    (is (= (builtin-div 1.0 1.0) 1))
    (is (= (builtin-div 4 2) 2))
    (is (= (builtin-div 1 3) 0.3333333333333333))
    (is (= (builtin-div 1 2) 0.5)))
  (testing "divide by zero"
    (is (thrown-with-msg? UndefinedException #"div: divide by zero" (builtin-div 2 0)))))

(deftest builtin-rem-test
  (testing "rem"
    (is (= (builtin-rem 5 2) 1))
    (is (= (builtin-rem 2 2) 0))
    (is (= (builtin-rem 2.0 2.0) 0)))
  (testing "non-int result is undefined"
    (is (thrown-with-msg? UndefinedException #"remainder is not an integer" (builtin-rem 2.0 1.5))))
  (testing "rem by zero"
    (try
      (builtin-rem 2 0)
      (catch JarlException e
        (is (= (.getMessage e) "modulo by zero"))
        (is (= (.getType e) "eval_builtin_error"))))))

(deftest builtin-round-test
  (testing "round"
    (is (= (builtin-round 1) 1))
    (is (= (builtin-round 1.4) 1))
    (is (= (builtin-round 1.5) 2))))

(deftest builtin-ceil-test
  (testing "ceil"
    (is (= (builtin-ceil 1) 1))
    (is (= (builtin-ceil 1.4) 2))
    (is (= (builtin-ceil 1.01) 2))))

(deftest builtin-floor-test
  (testing "floor"
    (is (= (builtin-floor 1) 1))
    (is (= (builtin-floor 1.4) 1))
    (is (= (builtin-floor 1.999) 1))))

(deftest builtin-abs-test
  (testing "abs"
    (is (= (builtin-abs 1) 1))
    (is (= (builtin-abs -1) 1))
    (is (= (builtin-abs -1.4) 1.4))))

(deftest builtin-numbers-range-test
  (testing "numbers.range"
    (is (= (builtin-numbers-range 1 5) [1 2 3 4 5]))
    (is (= (builtin-numbers-range 1 1) [1]))
    (is (= (builtin-numbers-range 5 1) [5 4 3 2 1]))))

(ns jarl.builtins.comparison_test
  (:require [clojure.test :refer [deftest is testing]]
            [jarl.builtins.comparison :refer [builtin-equal builtin-neq builtin-lt builtin-lte builtin-gt
                                              builtin-gte]]))

(deftest builtin-equal-test
  (testing "numbers"
    (is (= (builtin-equal 1 1) true))
    (is (= (builtin-equal 1 1.0) true))
    (is (= (builtin-equal -3.334 -3.3340000) true))
    (is (= (builtin-equal 3.00000000000001 3) false))
    (is (= (builtin-equal 3 "3") false)))
  (testing "strings"
    (is (= (builtin-equal "a" "a") true))
    (is (= (builtin-equal "a" "A") false))
    (is (= (builtin-equal "a" "b") false))
    (is (= (builtin-equal "å" "å") true))
    (is (= (builtin-equal "🇸🇪" "🇸🇪") true))
    (is (= (builtin-equal "foo bar baz\t" "foo bar baz\t") true)))
  (testing "objects"
    (is (= (builtin-equal {"foo" "bar"} {"foo" "bar"}) true))
    (is (= (builtin-equal {"foo" "bar"} {"foo" "bar" "bar" "baz"}) false))
    (is (= (builtin-equal {"foo" "bar" "bar" "baz"} {"bar" "baz" "foo" "bar"}) true))
    (is (= (builtin-equal {"foo" ["bar" 1]} {"foo" ["bar" 1]}) true))
    (is (= (builtin-equal {"foo" 1} {"foo" 1.0}) true)))
  (testing "arrays"
    (is (= (builtin-equal ["foo" "bar"] ["foo" "bar"]) true))
    (is (= (builtin-equal ["bar" "foo"] ["foo" "bar"]) false))
    (is (= (builtin-equal ["fåäö" "båäör"] ["fåäö" "båäör"]) true))
    (is (= (builtin-equal ["a" 1 [2] "b"] ["a" 1 [2] "b"]) true)))
  (testing "booleans"
    (is (= (builtin-equal true true) true))
    (is (= (builtin-equal false false) true))
    (is (= (builtin-equal true false) false)))
  (testing "null"
    (is (= (builtin-equal nil nil) true))
    (is (= (builtin-equal nil " ") false))))

(deftest builtin-neq-test
  (testing "numbers"
    (is (= (builtin-neq 1 2) true))
    (is (= (builtin-neq 1 1) false))
    (is (= (builtin-neq 1 1.0) false))
    (is (= (builtin-neq -3.334 -3.3340000) false)))
  (testing "strings"
    (is (= (builtin-neq "a" "a") false))
    (is (= (builtin-neq "a" "A") true))
    (is (= (builtin-neq "å" "å") false))
    (is (= (builtin-neq "🇸🇪" "🇸🇪") false))
    (is (= (builtin-neq "foo bar baz\t" "foo bar baz\t") false)))
  (testing "objects"
    (is (= (builtin-neq {"foo" "bar"} {"foo" "bar"}) false))
    (is (= (builtin-neq {"foo" ["bar" 1]} {"foo" ["bar" 1]}) false)))
  (testing "arrays"
    (is (= (builtin-neq ["foo" "bar"] ["foo" "bar"]) false))
    (is (= (builtin-neq ["bar" "foo"] ["foo" "bar"]) true))
    (is (= (builtin-neq ["fåäö" "båäör"] ["fåäö" "båäör"]) false))
    (is (= (builtin-neq ["a" 1 [2] "b"] ["a" 1 [2] "b"]) false)))
  (testing "boolean"
    (is (= (builtin-neq true true) false))
    (is (= (builtin-neq false false) false))
    (is (= (builtin-neq true false) true)))
  (testing "null"
    (is (= (builtin-neq nil nil) false))
    (is (= (builtin-neq nil " ") true))))

(deftest builtin-lt-test
  (testing "numbers"
    (is (= (builtin-lt 1 1) false))
    (is (= (builtin-lt 1.001 1.001) false))
    (is (= (builtin-lt 1.001 1.002) true))
    (is (= (builtin-lt 2 1) false))
    (is (= (builtin-lt 1 2) true))
    (is (= (builtin-lt 1.9 2) true))
    (is (= (builtin-lt -2 2) true)))
  (testing "strings"
    (is (= (builtin-lt "a" "a") false))
    (is (= (builtin-lt "b" "a") false))
    (is (= (builtin-lt "a" "b") true)))
  (testing "objects"
    (is (= (builtin-lt {"foo" "bar"} {"foo" "bar"}) false))
    (is (= (builtin-lt {"foo" 1} {"foo" 2}) true))
    (is (= (builtin-lt {"foo" 1} {"foo" 1 "bar" 2}) false))
    (is (= (builtin-lt {"foo" {"x" 1}} {"foo" {"x" 2}}) true))
    (is (= (builtin-lt {"foo" [1 2]} {"foo" [1 2 3]}) true)))
  (testing "objects-different-keys"
    (is (= (builtin-lt {"foo" 1} {"bar" 2}) false)))
  (testing "arrays"
    (is (= (builtin-lt ["foo" "bar"] ["foo" "bar"]) false))
    (is (= (builtin-lt ["a" "b"] ["a" "c"]) true))
    (is (= (builtin-lt [1 2 3] [1 2 4]) true))
    (is (= (builtin-lt [1 2 3] [1 2 "a"]) true))
    (is (= (builtin-lt [1 2 3] [1 2 3 -100]) true))
    (is (= (builtin-lt [[1 2]] [[1 3]]) true)))
  (testing "sets"
    (is (= (builtin-lt #{1} #{1}) false))
    (is (= (builtin-lt #{1} #{2}) true))
    (is (= (builtin-lt #{1 2} #{3}) true))
    (is (= (builtin-lt #{100} #{1 10001}) false))
    (is (= (builtin-lt #{{"x" 1}} #{{"x" 2}}) true)))
  (testing "booleans"
    (is (= (builtin-lt true true) false))
    (is (= (builtin-lt true false) false))
    (is (= (builtin-lt false true) true))
    (is (= (builtin-lt false 0) true))
    (is (= (builtin-lt true 0) true)))
  (testing "null"
    (is (= (builtin-lt nil nil) false))
    (is (= (builtin-lt nil 1) true))
    (is (= (builtin-lt nil "") true))
    (is (= (builtin-lt nil false) true))))

(deftest builtin-gt-test
  (testing "numbers"
    (is (= (builtin-gt 1 1) false))
    (is (= (builtin-gt 1.001 1.001) false))
    (is (= (builtin-gt 1.001 1.002) false))
    (is (= (builtin-gt 2 1) true))
    (is (= (builtin-gt 1 2) false))
    (is (= (builtin-gt 1.9 2) false))
    (is (= (builtin-gt -2 2) false)))
  (testing "strings"
    (is (= (builtin-gt "a" "a") false))
    (is (= (builtin-gt "b" "a") true))
    (is (= (builtin-gt "a" "b") false)))
  (testing "objects"
    (is (= (builtin-gt {"foo" "bar"} {"foo" "bar"}) false))
    (is (= (builtin-gt {"foo" 1} {"foo" 2}) false))
    (is (= (builtin-gt {"foo" 1} {"foo" 1 "bar" 2}) true))
    (is (= (builtin-gt {"foo" {"x" 1}} {"foo" {"x" 2}}) false))
    (is (= (builtin-gt {"foo" [1 2]} {"foo" [1 2 3]}) false)))
  (testing "objects-different-keys"
    (is (= (builtin-gt {"foo" 1} {"bar" 2}) true)))
  (testing "arrays"
    (is (= (builtin-gt ["foo" "bar"] ["foo" "bar"]) false))
    (is (= (builtin-gt ["a" "b"] ["a" "c"]) false))
    (is (= (builtin-gt [1 2 3] [1 2 4]) false))
    (is (= (builtin-gt [1 2 3] [1 2 "a"]) false))
    (is (= (builtin-gt [1 2 3] [1 2 3 -100]) false))
    (is (= (builtin-gt [[1 2]] [[1 3]]) false)))
  (testing "sets"
    (is (= (builtin-gt #{1} #{1}) false))
    (is (= (builtin-gt #{1} #{2}) false))
    (is (= (builtin-gt #{1 2} #{3}) false))
    (is (= (builtin-gt #{100} #{1 10001}) true))
    (is (= (builtin-gt #{{"x" 1}} #{{"x" 2}}) false)))
  (testing "booleans"
    (is (= (builtin-gt true true) false))
    (is (= (builtin-gt true false) true))
    (is (= (builtin-gt false true) false))
    (is (= (builtin-gt false 0) false))
    (is (= (builtin-gt true 0) false)))
  (testing "null"
    (is (= (builtin-gt nil nil) false))
    (is (= (builtin-gt nil 1) false))
    (is (= (builtin-gt nil "") false))
    (is (= (builtin-gt nil false) false))))

; These are already tested by virtue of rego-compare and the tests above
; Just sanity checking here..

(deftest builtin-lte-test
  (testing "numbers"
    (is (= (builtin-lte 1 1) true))
    (is (= (builtin-lte 1 2) true))
    (is (= (builtin-lte 2 1) false))))

(deftest builtin-gte-test
  (testing "numbers"
    (is (= (builtin-gte 1 1) true))
    (is (= (builtin-gte 1 2) false))
    (is (= (builtin-gte 2 1) true))))

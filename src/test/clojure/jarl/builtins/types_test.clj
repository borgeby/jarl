(ns jarl.builtins.types_test
  (:require [clojure.test :refer [deftest is testing]]
            [jarl.builtins.types :refer [builtin-is-number builtin-is-string builtin-is-boolean builtin-is-array
                                         builtin-is-set builtin-is-object builtin-is-null builtin-type-name]])
  (:import (se.fylling.jarl UndefinedException)
           (java.util.regex Pattern)))

(deftest builtin-is-number-test
  (testing "is_number"
    (is (= (builtin-is-number 1) true))
    (is (= (builtin-is-number 1.3) true))
  (testing "not a number"
    (is (thrown-with-msg? UndefinedException #"foo is not a number" (builtin-is-number "foo"))))))

(deftest builtin-is-string-test
  (testing "is_string"
    (is (= (builtin-is-string "") true))
    (is (= (builtin-is-string "foo") true))
    (testing "not a string"
      (is (thrown-with-msg? UndefinedException #"22 is not a string" (builtin-is-string 22))))))

(deftest builtin-is-boolean-test
  (testing "is_boolean"
    (is (= (builtin-is-boolean true) true))
    (is (= (builtin-is-boolean false) true))
    (testing "not a boolean"
      (is (thrown-with-msg? UndefinedException #"foo is not a boolean" (builtin-is-boolean "foo"))))))

(deftest builtin-is-array-test
  (testing "is_array"
    (is (= (builtin-is-array []) true))
    (is (= (builtin-is-array [1 "foo"]) true))
    (testing "not an array"
      (is (thrown-with-msg? UndefinedException #"foo is not an array" (builtin-is-array "foo"))))))

(deftest builtin-is-set-test
  (testing "is_set"
    (is (= (builtin-is-set #{}) true))
    (is (= (builtin-is-set #{1 2 3}) true))
    (testing "not a set"
      (is (thrown-with-msg? UndefinedException
                            (re-pattern (Pattern/quote "[1 2 3] is not a set")) (builtin-is-set [1 2 3]))))))

(deftest builtin-is-object-test
  (testing "is_object"
    (is (= (builtin-is-object {}) true))
    (is (= (builtin-is-object {"a" 1 "b" 2}) true))
    (testing "not an object"
      (is (thrown-with-msg? UndefinedException
                            (re-pattern (Pattern/quote "[1 2 3] is not an object")) (builtin-is-object [1 2 3]))))))

(deftest builtin-is-null-test
  (testing "is_null"
    (is (= (builtin-is-null nil) true))
    (testing "not nil"
      (is (thrown-with-msg? UndefinedException
                            (re-pattern (Pattern/quote "[1 2 3] is not null")) (builtin-is-null [1 2 3]))))))

(deftest builtin-type-name-test
  (testing "type_name"
    (is (= (builtin-type-name "foo") "string"))
    (is (= (builtin-type-name 1) "number"))
    (is (= (builtin-type-name 3.14) "number"))
    (is (= (builtin-type-name true) "boolean"))
    (is (= (builtin-type-name nil) "null"))
    (is (= (builtin-type-name {"foo" "bar"}) "object"))
    (is (= (builtin-type-name #{1 2}) "set"))))

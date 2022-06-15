(ns jarl.builtins.types_test
  (:require [clojure.test :refer [deftest]]
            [test.utils :refer [testing-builtin]])
  (:import (se.fylling.jarl UndefinedException)))

(deftest builtin-is-number-test
  (testing-builtin "is_number"
    [1] true
    [1.3] true
    ; not a number
    ["foo"] [UndefinedException "foo is not a number"]))

(deftest builtin-is-string-test
  (testing-builtin "is_string"
    [""] true
    ["foo"] true
    ; not a string
    [22] [UndefinedException "22 is not a string"]))

(deftest builtin-is-boolean-test
  (testing-builtin "is_boolean"
    [true] true
    [false] true
    ; not a boolean
    ["foo"] [UndefinedException "foo is not a boolean"]))

(deftest builtin-is-array-test
  (testing-builtin "is_array"
    [[]] true
    [[1 "foo"]] true
    ; not an array
    ["foo"] [UndefinedException "foo is not an array"]))

(deftest builtin-is-set-test
  (testing-builtin "is_set"
    [#{}] true
    [#{1 2 3}] true
    ; not a set
    [[1 2 3]] [UndefinedException "\\[1 2 3\\] is not a set"]))

(deftest builtin-is-object-test
  (testing-builtin "is_object"
    [{}] true
    [{"a" 1 "b" 2}] true
    ; not an object
    [[1 2 3]] [UndefinedException "\\[1 2 3\\] is not an object"]))

(deftest builtin-is-null-test
  (testing-builtin "is_null"
    [nil] true
    ; not nil
    [[1 2 3]] [UndefinedException "\\[1 2 3\\] is not null"]))

(deftest builtin-type-name-test
  (testing-builtin "type_name"
    ["foo"] "string"
    [1] "number"
    [3.14] "number"
    [true] "boolean"
    [nil] "null"
    [{"foo" "bar"}] "object"
    [#{1 2}] "set"))

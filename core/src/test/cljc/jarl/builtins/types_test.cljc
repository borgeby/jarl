(ns jarl.builtins.types-test
  (:require  [test.utils   :refer [testing-builtin]]
    #?(:clj  [clojure.test :refer [deftest]]
       :cljs [cljs.test    :refer [deftest]])))

(deftest builtin-is-number-test
  (testing-builtin "is_number"
    [1] true
    [1.3] true
    ; not a number
    ["foo"] false))

(deftest builtin-is-string-test
  (testing-builtin "is_string"
    [""] true
    ["foo"] true
    ; not a string
    [22] false))

(deftest builtin-is-boolean-test
  (testing-builtin "is_boolean"
    [true] true
    [false] true
    ; not a boolean
    ["foo"] false))

(deftest builtin-is-array-test
  (testing-builtin "is_array"
    [[]] true
    [[1 "foo"]] true
    ; not an array
    ["foo"] false))

(deftest builtin-is-set-test
  (testing-builtin "is_set"
    [#{}] true
    [#{1 2 3}] true
    ; not a set
    [[1 2 3]] false))

(deftest builtin-is-object-test
  (testing-builtin "is_object"
    [{}] true
    [{"a" 1 "b" 2}] true
    ; not an object
    [[1 2 3]] false))

(deftest builtin-is-null-test
  (testing-builtin "is_null"
    [nil] true
    ; not nil
    [[1 2 3]] false))

(deftest builtin-type-name-test
  (testing-builtin "type_name"
    ["foo"] "string"
    [1] "number"
    [3.14] "number"
    [true] "boolean"
    [nil] "null"
    [{"foo" "bar"}] "object"
    [#{1 2}] "set"))

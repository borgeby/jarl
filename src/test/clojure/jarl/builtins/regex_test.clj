(ns jarl.builtins.regex-test
  (:require [clojure.test :refer [deftest]]
            [test.utils :refer [testing-builtin]]))

(deftest builtin-regex-match-test
  (testing-builtin "regex.match"
    ["[\\d]+" "123"] true))

(deftest builtin-regex-is-valid-test
  (testing-builtin "regex.is_valid"
    ["[\\d]+"] true
    ["+++"] false
    [5] false))

(deftest builtin-regex-split-test
  (testing-builtin "regex.split"
    ["[\\d]" "a1b2c"] ["a" "b" "c"]))

(deftest builtin-regex-find-n-test
  (testing-builtin "regex.find_n"
    ["[\\d]" "a1b2c3" 0] []
    ["[\\d]" "a1b2c3" 3] ["1" "2" "3"]
    ["[\\d]" "a1b2c3" -1] ["1" "2" "3"]
    ["[\\d]" "a1b2c3" 2] ["1" "2"]
    ["[biw]" "bird is the word" 10] ["b" "i" "i" "w"]))

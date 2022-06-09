(ns jarl.builtins.regex_test
  (:require [clojure.test :refer [deftest is testing]]
            [jarl.builtins.regex :refer [builtin-regex-match builtin-regex-is-valid builtin-regex-split
                                         builtin-regex-find-n]]))

(deftest builtin-regex-match-test
  (testing "regex.match"
    (is (= (builtin-regex-match "[\\d]+" "123") true))))

(deftest builtin-regex-is-valid-test
  (testing "regex.is_valid"
    (is (= (builtin-regex-is-valid "[\\d]+") true))
    (is (= (builtin-regex-is-valid "+++") false))
    (is (= (builtin-regex-is-valid 5) false))))

(deftest builtin-regex-split-test
  (testing "regex.split"
    (is (= (builtin-regex-split "[\\d]" "a1b2c") ["a" "b" "c"]))))

(deftest builtin-regex-find-n-test
  (testing "regex.find_n"
    (is (= (builtin-regex-find-n "[\\d]" "a1b2c3" 0) []))
    (is (= (builtin-regex-find-n "[\\d]" "a1b2c3" 3) ["1" "2" "3"]))
    (is (= (builtin-regex-find-n "[\\d]" "a1b2c3" -1) ["1" "2" "3"]))
    (is (= (builtin-regex-find-n "[\\d]" "a1b2c3" 2) ["1" "2"]))
    (is (= (builtin-regex-find-n "[biw]" "bird is the word" 10) ["b" "i" "i" "w"]))))

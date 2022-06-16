(ns jarl.state-test
  (:require [clojure.test :refer [deftest is testing]]
            [jarl.state :refer [get-local push-with-stack]]))

(deftest get-local-test
  (testing "empty state"
    (let [state {}
          index 0
          result (get-local state index)]
      (is (= result nil))))
  (testing "get primitive value"
    (let [value 42
          index 2
          state {:local {index value}}
          result (get-local state index)]
      (is (= result value))))
  (testing "get object value"
    (let [value {"foo" "bar"}
          index 2
          state {:local {index value}}
          result (get-local state index)]
      (is (= result value))))
  (testing "get primitive value miss"
    (let [value 42
          index 2
          state {:local {3 value}}
          result (get-local state index)]
      (is (= result nil))))
  (testing "get primitive value miss with default value"
    (let [value 42
          index 2
          state {:local {3 value}}
          default-value 1337
          result (get-local state index default-value)]
      (is (= result default-value))))
  (testing "get with-stack miss"
    (let [value {"a" {"b" "foo"
                      "c" "bar"}}
          index 2
          state {:local {index value}}
          state (push-with-stack state 3 ["a" "b"] 42)
          result (get-local state index)]
      (is (= result value))))
  (testing "get with-stack miss (no path)"
    (let [value {"a" {"b" "foo"
                      "c" "bar"}}
          index 2
          state {:local {index value}}
          state (push-with-stack state 3 [] 42)
          result (get-local state index)]
      (is (= result value))))
  (testing "get with-stack hit"
    (let [value {"a" {"b" "foo"
                      "c" "bar"}}
          index 1
          state {:local {index value}}
          state (push-with-stack state index ["a" "b"] 42)
          result (get-local state index)]
      (is (= result {"a" {"b" 42
                          "c" "bar"}}))))
  (testing "get with-stack hit (key-type mismatch)"
    (let [value {"a" [1 2 3 4]}
          index 1
          state {:local {index value}}
          state (push-with-stack state index ["a" "b"] 42)
          result (get-local state index)]
      (is (= result {"a" {"b" 42}})))))

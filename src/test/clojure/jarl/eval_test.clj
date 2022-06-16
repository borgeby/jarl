(ns jarl.eval-test
  (:require [clojure.test :refer [deftest is testing]]
            [jarl.eval :refer [eval-ArrayAppendStmt
                               eval-AssignVarStmt
                               eval-AssignVarOnceStmt
                               eval-DotStmt
                               eval-IsObjectStmt eval-LenStmt
                               eval-MakeNumberIntStmt
                               eval-MakeObjectStmt
                               eval-SetAddStmt]]
            [jarl.state :refer [get-local set-local]])
  (:import (se.fylling.jarl UndefinedException)))

(defn make-value-key [type value]
  {"type"  type
   "value" value})

(defn make-local-value-key [index]
  (make-value-key "local" index))

(defn make-string-value-key [index]
  (make-value-key "string_index" index))

(defn make-bool-value-key [value]
  (make-value-key "bool" value))

(defn set-static [state strings]
  (let [strings-map (into {} (map-indexed (fn [i s] {i {"value" s}}) strings))
        static {"strings" strings-map}]
    (assoc state :static static)))

(deftest eval-ArrayAppendStmt-test
  (testing "append to empty array"
    (let [array []
          value 1
          array-index 2
          value-index (make-local-value-key 3)
          state {:local {}}
          state (set-local state 2 array)
          state (set-local state 3 value)
          result-state (eval-ArrayAppendStmt array-index value-index state)
          result (get-local result-state array-index)]
      (is (= result [1]))))
  (testing "append to non-empty array"
    (let [array ["foo" "bar"]
          value "baz"
          array-index 2
          value-index (make-string-value-key 0)
          state {:local {}}
          state (set-static state [value])
          state (set-local state 2 array)
          state (set-local state 3 value)
          result-state (eval-ArrayAppendStmt array-index value-index state)
          result (get-local result-state array-index)]
      (is (= result ["foo" "bar" "baz"]))))
  (testing "append to non-existent array"
    (let [value 1
          array-index 2
          value-index (make-local-value-key 3)
          state {:local {}}
          state (set-local state 3 value)
          result-state (eval-ArrayAppendStmt array-index value-index state)]
      (is (contains? result-state :break-index))))
  (testing "append non-existent value to array"
    (let [array [1 2]
          array-index 2
          value-index (make-local-value-key 3)
          state {:local {}}
          state (set-local state 2 array)
          result-state (eval-ArrayAppendStmt array-index value-index state)]
      (is (contains? result-state :break-index)))))

(deftest eval-AssignVarStmt-test
  (testing "assign local value"
    (let [value 42
          source-index (make-local-value-key 3)
          target 2
          state {}
          state (set-local state 3 value)
          result-state (eval-AssignVarStmt source-index target state)
          result (get-local result-state target)]
      (is (= result value))))
  (testing "assign string value"
    (let [value "foo"
          source-index (make-string-value-key 0)
          target 2
          state {}
          state (set-static state [value])
          result-state (eval-AssignVarStmt source-index target state)
          result (get-local result-state target)]
      (is (= result value))))
  (testing "assign bool value"
    (let [value true
          source-index (make-bool-value-key value)
          target 2
          state {}
          result-state (eval-AssignVarStmt source-index target state)
          result (get-local result-state target)]
      (is (= result value))))
  (testing "assign non-existent value"
    (let [source-index (make-local-value-key 3)
          target 2
          state {}
          result-state (eval-AssignVarStmt source-index target state)]
      (is (= result-state state)))))

(deftest eval-AssignVarOnceStmt-test
  (testing "assign local value"
    (let [value 42
          source-index (make-local-value-key 3)
          target 2
          state {}
          state (set-local state 3 value)
          result-state (eval-AssignVarOnceStmt source-index target state)
          result (get-local result-state target)]
      (is (= result value))))
  (testing "assign already existing local value (==)"
    (let [value 42
          source-index (make-local-value-key 3)
          target 2
          state {:local {}}
          state (set-local state target value)
          state (set-local state 3 value)
          result-state (eval-AssignVarOnceStmt source-index target state)
          result (get-local result-state target)]
      (is (= result value))))
  (testing "assign already existing local value (!=)"
    (let [value 42
          existing-value 43
          source-index (make-local-value-key 3)
          target 2
          state {:local {}}
          state (set-local state target existing-value)
          state (set-local state 3 value)]
      (is (thrown-with-msg? Exception (re-pattern "<\\{\"type\" \"local\", \"value\" 3}> already assigned")
                            (eval-AssignVarOnceStmt source-index target state)))))
  (testing "assign non-existent value"
    (let [source-index (make-local-value-key 3)
          target 2
          state {:local {}}
          result-state (eval-AssignVarOnceStmt source-index target state)]
      (is (= result-state state)))))

(deftest eval-DotStmt-test
  (testing "get in object"
    (let [target-index 42
          key 13
          key-index 3
          key-pos (make-local-value-key key-index)
          value 37
          source-value {key value}
          source-index 2
          source-pos (make-local-value-key source-index)
          state {}
          state (set-local state key-index key)
          state (set-local state source-index source-value)
          result-state (eval-DotStmt source-pos key-pos target-index state)
          result (get-local result-state target-index)]
      (is (not (contains? result-state :break-index)))
      (is (= result value))))
  (testing "get in empty object"
    (let [target-index 42
          key 13
          key-index 3
          key-pos (make-local-value-key key-index)
          source-value {}
          source-index 2
          source-pos (make-local-value-key source-index)
          state {}
          state (set-local state key-index key)
          state (set-local state source-index source-value)
          result-state (eval-DotStmt source-pos key-pos target-index state)]
      (is (contains? result-state :break-index))))
  (testing "get in array"
    (let [target-index 42
          key 13
          key-index 3
          key-pos (make-local-value-key key-index)
          source-value [1 2 3]
          source-index 2
          source-pos (make-local-value-key source-index)
          state {}
          state (set-local state key-index key)
          state (set-local state source-index source-value)
          result-state (eval-DotStmt source-pos key-pos target-index state)]
      (is (contains? result-state :break-index))))
  (testing "key not present"
    (let [target-index 42
          key-index 3
          key-pos (make-local-value-key key-index)
          source-value [1 2 3]
          source-index 2
          source-pos (make-local-value-key source-index)
          state {}
          state (set-local state source-index source-value)
          result-state (eval-DotStmt source-pos key-pos target-index state)]
      (is (contains? result-state :break-index))))
  (testing "source not present"
    (let [target-index 42
          key 13
          key-index 3
          key-pos (make-local-value-key key-index)
          source-index 2
          source-pos (make-local-value-key source-index)
          state {}
          state (set-local state key-index key)
          result-state (eval-DotStmt source-pos key-pos target-index state)]
      (is (contains? result-state :break-index)))))

(deftest eval-MakeObjectStmt-test
  (testing "make an empty object"
    (let [target 42
          state {}
          result-state (eval-MakeObjectStmt target state)
          result (get-local result-state target)]
      (is (= result {})))))

(deftest eval-IsObjectStmt-test
  (testing "empty object"
    (let [index 42
          value {}
          source (make-local-value-key index)
          state {}
          state (set-local state index value)
          result-state (eval-IsObjectStmt source state)]
      (is (not (contains? result-state :break-index)))))
  (testing "non-empty object"
    (let [index 42
          value {:foo "bar"}
          source (make-local-value-key index)
          state {}
          state (set-local state index value)
          result-state (eval-IsObjectStmt source state)]
      (is (not (contains? result-state :break-index)))))
  (testing "string"
    (let [index 42
          value "not an object"
          source (make-local-value-key index)
          state {}
          state (set-local state index value)
          result-state (eval-IsObjectStmt source state)]
      (is (contains? result-state :break-index))))
  (testing "number"
    (let [index 42
          value 1337
          source (make-local-value-key index)
          state {}
          state (set-local state index value)
          result-state (eval-IsObjectStmt source state)]
      (is (contains? result-state :break-index))))
  (testing "bool"
    (let [index 42
          value false
          source (make-local-value-key index)
          state {}
          state (set-local state index value)
          result-state (eval-IsObjectStmt source state)]
      (is (contains? result-state :break-index))))
  (testing "bool constant"
    (let [value false
          source (make-bool-value-key value)
          state {}
          result-state (eval-IsObjectStmt source state)]
      (is (contains? result-state :break-index))))
  (testing "string constant"
    (let [value "not an object"
          source (make-string-value-key value)
          state {}
          result-state (eval-IsObjectStmt source state)]
      (is (contains? result-state :break-index))))
  (testing "undefined local"
    (let [index 42
          source (make-local-value-key index)
          state {}
          result-state (eval-IsObjectStmt source state)]
      (is (contains? result-state :break-index)))))

(deftest eval-LenStmt-test
  (testing "empty object"
    (let [index 2
          value {}
          target 3
          source (make-local-value-key index)
          state {}
          state (set-local state index value)
          result-state (eval-LenStmt source target state)
          result (get-local result-state target)]
      (is (= result 0))))
  (testing "object"
    (let [index 2
          value {"foo" "bar"
                 "do"  "re"}
          target 3
          source (make-local-value-key index)
          state {}
          state (set-local state index value)
          result-state (eval-LenStmt source target state)
          result (get-local result-state target)]
      (is (= result 2))))
  (testing "empty list"
    (let [index 2
          value '()
          target 3
          source (make-local-value-key index)
          state {}
          state (set-local state index value)
          result-state (eval-LenStmt source target state)
          result (get-local result-state target)]
      (is (= result 0))))
  (testing "list"
    (let [index 2
          value '("foo" "bar" "baz")
          target 3
          source (make-local-value-key index)
          state {}
          state (set-local state index value)
          result-state (eval-LenStmt source target state)
          result (get-local result-state target)]
      (is (= result 3))))
  (testing "empty vector"
    (let [index 2
          value []
          target 3
          source (make-local-value-key index)
          state {}
          state (set-local state index value)
          result-state (eval-LenStmt source target state)
          result (get-local result-state target)]
      (is (= result 0))))
  (testing "vector"
    (let [index 2
          value ["foo" "bar" "baz"]
          target 3
          source (make-local-value-key index)
          state {}
          state (set-local state index value)
          result-state (eval-LenStmt source target state)
          result (get-local result-state target)]
      (is (= result 3))))
  (testing "empty set"
    (let [index 2
          value #{}
          target 3
          source (make-local-value-key index)
          state {}
          state (set-local state index value)
          result-state (eval-LenStmt source target state)
          result (get-local result-state target)]
      (is (= result 0))))
  (testing "set"
    (let [index 2
          value #{"foo" "bar" "baz"}
          target 3
          source (make-local-value-key index)
          state {}
          state (set-local state index value)
          result-state (eval-LenStmt source target state)
          result (get-local result-state target)]
      (is (= result 3))))
  (testing "empty string"
    (let [index 2
          target 3
          value ""
          source (make-local-value-key index)
          state {}
          state (set-local state index value)
          result-state (eval-LenStmt source target state)
          result (get-local result-state target)]
      (is (= result 0))))
  (testing "string"
    (let [index 2
          value "foobar"
          target 3
          source (make-local-value-key index)
          state {}
          state (set-local state index value)
          result-state (eval-LenStmt source target state)
          result (get-local result-state target)]
      (is (= result 6))))
  (testing "string constant"
    (let [value "hello"
          target 3
          source (make-string-value-key 0)
          state {}
          state (set-static state [value])
          result-state (eval-LenStmt source target state)
          result (get-local result-state target)]
      (is (= result 5))))
  (testing "undefined local"
    (let [index 42
          target 3
          source (make-local-value-key index)
          state {}]
      (is (thrown? UndefinedException (eval-LenStmt source target state))))))

(deftest eval-MakeNumberIntStmt-test
  (testing "integer"
    (let [value 42
          target 3
          state {}
          result-state (eval-MakeNumberIntStmt value target state)
          result (get-local result-state target)]
      (is (= result 42))))
  (testing "valid float"
    (let [value 42.0
          target 3
          state {}
          result-state (eval-MakeNumberIntStmt value target state)
          result (get-local result-state target)]
      (is (= result 42))))
  (testing "invalid float"
    (let [value 42.9
          target 3
          state {}]
      (is (thrown-with-msg? Exception #"'42.9' is not an integer" (eval-MakeNumberIntStmt value target state)))))
  (testing "string"
    (let [value "foobar"
          target 3
          state {}]
      (is (thrown-with-msg? Exception #"'foobar' is not an integer" (eval-MakeNumberIntStmt value target state)))))
  (testing "nil"
    (let [value nil
          target 3
          state {}]
      (is (thrown-with-msg? Exception #"'null' is not an integer" (eval-MakeNumberIntStmt value target state))))))

(deftest eval-SetAddStmt-test
  (testing "add to empty set"
    (let [set #{}
          value 1
          set-index 2
          value-index (make-local-value-key 3)
          state {:local {}}
          state (set-local state 2 set)
          state (set-local state 3 value)
          result-state (eval-SetAddStmt set-index value-index state)
          result (get-local result-state set-index)]
      (is (= result #{value}))
      (is (not (contains? result-state :break-index)))))
  (testing "add to non-empty set"
    (let [set #{"foo" "bar"}
          value "baz"
          set-index 2
          value-index (make-local-value-key 3)
          state {:local {}}
          state (set-local state 2 set)
          state (set-local state 3 value)
          result-state (eval-SetAddStmt set-index value-index state)
          result (get-local result-state set-index)]
      (is (= result #{"foo" "bar" "baz"}))
      (is (not (contains? result-state :break-index)))))
  (testing "add overlapping value to non-empty set"
    (let [set #{"foo" "bar"}
          value "bar"
          set-index 2
          value-index (make-local-value-key 3)
          state {:local {}}
          state (set-local state 2 set)
          state (set-local state 3 value)
          result-state (eval-SetAddStmt set-index value-index state)
          result (get-local result-state set-index)]
      (is (= result #{"foo" "bar"}))
      (is (not (contains? result-state :break-index)))))
  (testing "append to non-existent set"
    (let [value 1
          set-index 2
          value-index (make-local-value-key 3)
          state {:local {}}
          state (set-local state 3 value)
          result-state (eval-SetAddStmt set-index value-index state)]
      (is (contains? result-state :break-index))))
  (testing "append non-existent value to set"
    (let [set [1 2]
          set-index 2
          value-index (make-local-value-key 3)
          state {:local {}}
          state (set-local state 2 set)
          result-state (eval-SetAddStmt set-index value-index state)]
      (is (contains? result-state :break-index)))))

;
; Copyright 2022 Johan Fylling, Anders Eknert
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
;

(ns jarl.eval_test
  (:require [clojure.test :refer :all]
            [jarl.eval :refer [eval-ArrayAppendStmt eval-AssignVarStmt]]
            [jarl.state :refer [get-local set-local get-string]]))

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
  (let [strings-map (loop [index 0
                           array strings
                           map {}]
                      (if (empty? array)
                        map
                        (recur (inc index) (next array) (assoc map index {"value" (first array)}))))
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
          state {:local {}}
          state (set-local state 3 value)
          result-state (eval-AssignVarStmt source-index target state)
          result (get-local result-state target)]
      (is (= result value))))
  (testing "assign string value"
    (let [value "foo"
          source-index (make-string-value-key 0)
          target 2
          state {:local {}}
          state (set-static state [value])
          result-state (eval-AssignVarStmt source-index target state)
          result (get-local result-state target)]
      (is (= result value))))
  (testing "assign bool value"
    (let [value true
          source-index (make-bool-value-key value)
          target 2
          state {:local {}}
          result-state (eval-AssignVarStmt source-index target state)
          result (get-local result-state target)]
      (is (= result value))))
  (testing "assign non-existent value"
    (let [source-index (make-local-value-key 3)
          target 2
          state {:local {}}
          result-state (eval-AssignVarStmt source-index target state)]
      (is (contains? result-state :break-index)))))

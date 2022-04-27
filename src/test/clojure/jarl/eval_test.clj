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
            [jarl.eval :refer [eval-ArrayAppendStmt]]
            [jarl.state :refer [get-local set-local get-string]]))

(defn make-value-key [type value]
  {"type"  type
   "value" value})

(defn make-local-value-key [value]
  (make-value-key "local" value))

(defn make-string-value-key [value]
  (make-value-key "string_index" value))

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
(ns jarl.builtins.utils_test
  (:require [clojure.test :refer [deftest is testing]]
            [jarl.builtins.utils :refer [builtin-ex typed-seq]])
  (:import (se.fylling.jarl BuiltinException)))

(deftest builtin-ex-test
  (testing "formatting"
    (let [ex (builtin-ex "my message #%d caused by %s" 1 "incompetence")]
      (is (instance? BuiltinException ex))
      (is (= (.getMessage ^Throwable ex) "my message #1 caused by incompetence"))))
  (testing "no formatting"
    (let [ex (builtin-ex "my message caused by insomnia")]
      (is (instance? BuiltinException ex))
      (is (= (.getMessage ^Throwable ex) "my message caused by insomnia")))))

(deftest typed-seq-test
  (testing "typed-seq compliant"
    (is (nil? (typed-seq ["1", 2, "foo"] #{"string" "number"})))
    (is (nil? (typed-seq #{[1, 2], {"foo" "bar"}} ["array" "object"]))))
  (testing "typed-seq violations"
    (is (thrown-with-msg? BuiltinException #"operand must be array or set of string but got array or set containing number"
                          (typed-seq [1] #{"string"})))
    (is (thrown-with-msg? BuiltinException #"operand must be array or set of string but got array or set containing number"
                          (typed-seq ["foo" 1] #{"string"})))))

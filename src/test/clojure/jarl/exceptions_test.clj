(ns jarl.exceptions-test
  (:require [clojure.test :refer [deftest testing is]]
            [jarl.exceptions :refer [resolve-jarl-exception throws? try-or]])
  (:import (se.fylling.jarl BuiltinException UndefinedException)))

(deftest resolve-jarl-exception-test
  (testing "resolve-jarl-exception"
    (is (= (resolve-jarl-exception BuiltinException) BuiltinException))
    (is (= (resolve-jarl-exception 'BuiltinException) BuiltinException))
    (is (= (resolve-jarl-exception 'UndefinedException) UndefinedException))
    (is (nil? (resolve-jarl-exception RuntimeException)))))

(deftest throws?-test
  (testing "throws?"
    (is (= (throws? #(Integer/parseInt "100")) false))
    (is (= (throws? #(Integer/parseInt "foo")) true))))

(deftest try-or-test
  (testing "try-or"
    (is (= (try-or (Integer/parseInt "foo") 1) 1))
    (is (= (try-or (Integer/parseInt "2")   1) 2))))

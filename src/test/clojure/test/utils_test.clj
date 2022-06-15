(ns test.utils-test
  (:require [clojure.test :refer [deftest testing is]]
            [test.utils :refer [testing-builtin]]
            [jarl.builtins.aggregates])
  (:import (se.fylling.jarl TypeException)))

(deftest testing-builtin-test
  (testing "simple args expansion"
    (is (testing-builtin "count" [#{1}] 1) true))
  (testing "exceptions"
    (is (testing-builtin "sum" [[5 "5"]] [TypeException "operand"]))))

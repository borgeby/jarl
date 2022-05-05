(ns jarl.builtins.sets_test
  (:require [clojure.test :refer [deftest is testing]]
            [jarl.builtins.sets :refer [builtin-and builtin-or builtin-intersection builtin-union]]))

(deftest builtin-and-test
  (testing "and" ; intersection
    (is (= (builtin-and #{4 9 1} #{1 5 4}) #{1 4}))
    (is (= (builtin-and #{1} #{2}) #{}))))

(deftest builtin-or-test
  (testing "or" ; union
    (is (= (builtin-or #{4 9 1} #{1 5 4}) #{1 4 5 9}))))

(deftest builtin-intersection-test
  (testing "intersection"
    (is (= (builtin-intersection #{#{1 5 4} #{1 2 3} #{7 1 4}}) #{1}))
    (is (= (builtin-intersection #{#{1 5 4} #{1 2 3 4} #{7 1 4}}) #{1 4}))
    (is (= (builtin-intersection #{#{1 5 4} #{2 3} #{7 5}}) #{}))))

(deftest builtin-union-test
  (testing "union"
    (is (= (builtin-union #{#{1 5 4} #{1 2 3} #{7 1 4 6}}) #{1 2 3 4 5 6 7}))
    (is (= (builtin-union #{#{1 5 4} #{1 2 3 4} #{7 1 4}}) #{1 2 3 4 5 7}))
    (is (= (builtin-union #{#{1} #{}}) #{1}))))

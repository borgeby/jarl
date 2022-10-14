(ns jarl.builtins.sets-test
  (:require  [test.utils   :refer [testing-builtin]]
    #?(:clj  [clojure.test :refer [deftest]]
       :cljs [cljs.test    :refer [deftest]])))

(deftest builtin-and-test
  (testing-builtin "and" ; intersection
    [#{4 9 1} #{1 5 4}] #{1 4}
    [#{1} #{2}] #{}))

(deftest builtin-or-test
  (testing-builtin "or" ; union
    [#{4 9 1} #{1 5 4}] #{1 4 5 9}))

(deftest builtin-intersection-test
  (testing-builtin "intersection"
    [#{}] #{}
    [#{#{1 5 4} #{1 2 3} #{7 1 4}}] #{1}
    [#{#{1 5 4} #{1 2 3 4} #{7 1 4}}] #{1 4}
    [#{#{1 5 4} #{2 3} #{7 5}}] #{}))

(deftest builtin-union-test
  (testing-builtin "union"
    [#{#{1 5 4} #{1 2 3} #{7 1 4 6}}] #{1 2 3 4 5 6 7}
    [#{#{1 5 4} #{1 2 3 4} #{7 1 4}}] #{1 2 3 4 5 7}
    [#{#{1} #{}}] #{1}))

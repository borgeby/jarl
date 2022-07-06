(ns jarl.types-test
  (:require [cljs.test :refer [deftest testing is]]
            [jarl.builtins.comparison :refer [builtin-equal]]))

(deftest comparison-test
  (testing "simple comparison"
    (is (= (builtin-equal {:args [1 1]}) true))
    (is (= (builtin-equal {:args [1 2]}) false))))

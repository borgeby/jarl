(ns jarl.builtins.utils-test
  (:require #?(:clj  [clojure.test :refer [deftest testing is]]
               :cljs [cljs.test :refer [deftest testing is]])
    [jarl.builtins.utils :refer [type-match?]]))

(deftest foobar-test
  ;(testing "any"
  ;  (is true (type-match? {"type" "any"} 42)))
  ;(testing "not string"
  ;  (is (= false (type-match? {"type" "string"} 42))))
  (testing "foo"
    (is (= 1 2))))
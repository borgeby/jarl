(ns jarl.builtins.objects_test
  (:require [clojure.test :refer [deftest is testing]]
            [jarl.builtins.objects :refer [builtin-object-get builtin-object-remove builtin-object-union
                                           builtin-object-union-n builtin-object-filter builtin-json-filter]]))

(deftest builtin-object-get-test
  (testing "object.get"
    (is (= (builtin-object-get {"foo" "bar"} "foo" nil) "bar"))
    (is (= (builtin-object-get {"foo" "bar"} "baz" nil) nil))
    (is (= (builtin-object-get {"foo" "bar"} ["foo"] nil) "bar"))
    (is (= (builtin-object-get {"foo" {"bar" [{"x" 1} {"y" 2}] }} ["foo" "bar" 1 "y"] nil) 2))))

(deftest builtin-object-remove-test
  (testing "object.remove"
    (is (= (builtin-object-remove {"foo" "bar"} ["foo"]) {}))
    (is (= (builtin-object-remove {"foo" "bar"} #{"foo"}) {}))
    (is (= (builtin-object-remove {"a" 1 "b" 2 "c" {"d" 1 "e" 2}} {"c" {"d" 1}}) {"a" 1 "b" 2}))
    (is (= (builtin-object-remove {"a" 1 "b" 2 "c" {"d" 1 "e" 2}} {"c" {"d" "ignored"}}) {"a" 1 "b" 2}))))

(deftest builtin-object-union-test
  (testing "object.union"
    (is (= (builtin-object-union {"foo" "bar"} {"foo" "baz"}) {"foo" "baz"}))
    (is (= (builtin-object-union {"foo" "bar"} {"foo" "bar" "x" 1}) {"foo" "bar" "x" 1}))))

(deftest builtin-object-union-n-test
  (testing "object.union_n"
    (is (= (builtin-object-union-n [{"foo" "bar"} {"foo" "baz"}]) {"foo" "baz"}))
    (is (= (builtin-object-union-n [{"foo" "bar"} {"foo" "bar" "x" 1}]) {"foo" "bar" "x" 1}))
    (is (= (builtin-object-union-n [{"foo" "bar"} {"foo" "baz"} {"x" 1} {"y" 2 "x" 10}]) {"foo" "baz" "x" 10 "y" 2}))))

(deftest builtin-object-filter-test
  (testing "object.filter"
    (is (= (builtin-object-filter {"a" 1 "b" 2 "c" 3} ["b" "c"]) {"b" 2 "c" 3}))
    (is (= (builtin-object-filter {"a" 1 "b" 2 "c" 3} {"b" 2}) {"b" 2}))
    (is (= (builtin-object-filter {"a" 1 "b" 2 "c" 3} {}) {}))))

(deftest builtin-json-filter-test
  (testing "json.filter"
    (is (= (builtin-json-filter {"a" 1 "b" 2 "c" {"foo" "bar" "x" 10}} ["b" "c/foo"]) {"b" 2 "c" {"foo" "bar"}})))
  ;(testing "nested arrays"
  ;  (is (= (builtin-json-filter {"a" 1 "b" 2 "c" {"d" 1 "e" [1 {"foo" "bar"}]}} ["a", "c/d", "c/e/1/foo"])
  ;         {"a" 1 "c" {"d" 1
  ;                     "e" [{"foo" "bar"}]}})))
  )

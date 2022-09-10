(ns jarl.builtins.objects-test
  (:require  [test.utils   :refer [testing-builtin]]
             #?(:clj  [clojure.test :refer [deftest]]
                :cljs [cljs.test    :refer [deftest]])))

(deftest builtin-object-get-test
  (testing-builtin "object.get"
    [{"foo" "bar"} "foo" nil] "bar"
    [{"foo" "bar"} "baz" nil] nil
    [{"foo" "bar"} ["foo"] nil] "bar"
    [{"foo" {"bar" [{"x" 1} {"y" 2}] }} ["foo" "bar" 1 "y"] nil] 2))

(deftest builtin-object-remove-test
  (testing-builtin "object.remove"
    [{"foo" "bar"} ["foo"]] {}
    [{"foo" "bar"} #{"foo"}] {}
    [{"a" 1 "b" 2 "c" {"d" 1 "e" 2}} {"c" {"d" 1}}] {"a" 1 "b" 2}
    [{"a" 1 "b" 2 "c" {"d" 1 "e" 2}} {"c" {"d" "ignored"}}] {"a" 1 "b" 2}))

(deftest builtin-object-subset-test
  (testing-builtin "object.subset"
    [#{"foo" "bar" "baz"} #{"bar" "baz"}]     true
    [#{"bar" "baz"} #{"foo" "bar" "baz"}]     false
    [["foo" "bar" "baz"] #{"bar" "baz"}]      true
    [["foo" "bar" "baz"] #{"bar" "wat"}]      false
    [[1 2 3] [2 3]]                           true
    [[2 3] [1 2 3]]                           false
    [{"a" 1 "b" 2 "c" {"d" 1}} {"c" {"d" 1}}] true
    [{"c" {"d" 1}} {"a" 1 "b" 2 "c" {"d" 1}}] false))

(deftest builtin-object-union-test
  (testing-builtin "object.union"
    [{"foo" "bar"} {"foo" "baz"}] {"foo" "baz"}
    [{"foo" "bar"} {"foo" "bar" "x" 1}] {"foo" "bar" "x" 1}))

(deftest builtin-object-union-n-test
  (testing-builtin "object.union_n"
    [[{"foo" "bar"} {"foo" "baz"}]] {"foo" "baz"}
    [[{"foo" "bar"} {"foo" "bar" "x" 1}]] {"foo" "bar" "x" 1}
    [[{"foo" "bar"} {"foo" "baz"} {"x" 1} {"y" 2 "x" 10}]] {"foo" "baz" "x" 10 "y" 2}))

(deftest builtin-object-filter-test
  (testing-builtin "object.filter"
    [{"a" 1 "b" 2 "c" 3} ["b" "c"]] {"b" 2 "c" 3}
    [{"a" 1 "b" 2 "c" 3} {"b" 2}] {"b" 2}
    [{"a" 1 "b" 2 "c" 3} {}] {}))

;(deftest builtin-json-filter-test
;  (testing-builtin "json.filter"
;    [{"a" 1 "b" 2 "c" {"foo" "bar" "x" 10}} ["b" "c/foo"]] {"b" 2 "c" {"foo" "bar"}}
  ;(testing "nested arrays"
  ;  (is (= (builtin-json-filter {"a" 1 "b" 2 "c" {"d" 1 "e" [1 {"foo" "bar"}]}} ["a", "c/d", "c/e/1/foo"])
  ;         {"a" 1 "c" {"d" 1
  ;                     "e" [{"foo" "bar"}]}})))
  ;))

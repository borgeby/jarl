(ns test.utils-test)
  ;(:require [jarl.builtins.aggregates]
  ;          #?(:clj  [clojure.test :refer [deftest is testing]]
  ;             :cljs [cljs.test :refer-macros [deftest is testing]])
  ;          #?(:clj  [test.utils :refer [testing-builtin]]
  ;             :cljs [test.utils :refer-macros [testing-builtin]])))

; TODO: The testing-builtin macro throws when compiled by the ClojureScript compiler:
;  Caused by: clojure.lang.ExceptionInfo: failed compiling constant:
;   jarl.builtins.aggregates$builtin_count@5ca67657; jarl.builtins.aggregates$builtin_count is not a valid ClojureScript constant.
;   {:constant #object[jarl.builtins.aggregates$builtin_count 0x5ca67657 "jarl.builtins.aggregates$builtin_count@5ca67657"], :type jarl.builtins.aggregates$builtin_count, :clojure.error/phase :compilation}
;  I have yet to figure out why

;(deftest testing-builtin-test
;  (testing "simple args expansion"
;    (is (testing-builtin "count" [#{1}] 1))))
  ;(testing "exceptions"
  ;  (is (testing-builtin "sum" [[5 "5"]] [:jarl.exceptions/type-exception "operand"]))))

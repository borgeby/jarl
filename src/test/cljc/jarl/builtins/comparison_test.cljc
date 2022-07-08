(ns jarl.builtins.comparison-test
  (:require  [test.utils   :refer [testing-builtin]]
    #?(:clj  [clojure.test :refer [deftest]]
       :cljs [cljs.test    :refer [deftest]])))

(deftest builtin-equal-test
  (testing-builtin "equal"
    ; numbers
    [1 1]                true
    [1 1.0]              true
    [-3.334 -3.3340000]  true
    [3.00000000000001 3] false
    [3 "3"]              false
    ; strings
    ["a" "a"]                         true
    ["a" "b"]                         false
    ["å" "å"]                         true
    ["🇸🇪" "🇸🇪"]                       true
    ["foo bar baz\t" "foo bar baz\t"] true
    ; objects
    [{"foo" "bar"} {"foo" "bar"}]                         true
    [{"foo" "bar"} {"foo" "bar" "bar" "baz"}]             false
    [{"foo" "bar" "bar" "baz"} {"bar" "baz" "foo" "bar"}] true
    [{"foo" ["bar" 1]} {"foo" ["bar" 1]}]                 true
    [{"foo" 1} {"foo" 1.0}]                               true
    ; arrays
    [["foo" "bar"] ["foo" "bar"]]       true
    [["bar" "foo"] ["foo" "bar"]]       false
    [["fåäö" "båäör"] ["fåäö" "båäör"]] true
    [["a" 1 [2] "b"] ["a" 1 [2] "b"]]   true
    ; booleans
    [true true]   true
    [false false] true
    [true false]  false
    ; null
    [nil nil] true
    [nil " "] false))

(deftest builtin-neq-test
  (testing-builtin "neq"
    [1 2] true
    [1 1] false
    [1 1.0] false
    [-3.334 -3.3340000] false
    ; strings
    ["a" "a"] false
    ["a" "A"] true
    ["å" "å"] false
    ["🇸🇪" "🇸🇪"] false
    ["foo bar baz\t" "foo bar baz\t"] false
    ; objects
    [{"foo" "bar"} {"foo" "bar"}] false
    [{"foo" ["bar" 1]} {"foo" ["bar" 1]}] false
    ; arrays
    [["foo" "bar"] ["foo" "bar"]] false
    [["bar" "foo"] ["foo" "bar"]] true
    [["fåäö" "båäör"] ["fåäö" "båäör"]] false
    [["a" 1 [2] "b"] ["a" 1 [2] "b"]] false
    ; booleans
    [true true] false
    [false false] false
    [true false] true
    ; null
    [nil nil] false
    [nil " "] true))

; bigint
#?(:clj
   (deftest builtin-neq-bigint-test
     (testing-builtin "neq"
       [28857836529306024611913 28857836529306024611912] true)))

(deftest builtin-lt-test
  (testing-builtin "lt"
    [1 1] false
    [1.001 1.001] false
    [1.001 1.002] true
    [2 1] false
    [1 2] true
    [1.9 2] true
    [-2 2] true
    ; strings
    ["a" "a"] false
    ["b" "a"] false
    ["a" "b"] true
    ; objects
    [{"foo" "bar"} {"foo" "bar"}] false
    [{"foo" 1} {"foo" 2}] true
    [{"foo" 1} {"foo" 1 "bar" 2}] false
    [{"foo" {"x" 1}} {"foo" {"x" 2}}] true
    [{"foo" [1 2]} {"foo" [1 2 3]}] true
    ; objects-different-keys
    [{"foo" 1} {"bar" 2}] false
    ; arrays
    [["foo" "bar"] ["foo" "bar"]] false
    [["a" "b"] ["a" "c"]] true
    [[1 2 3] [1 2 4]] true
    [[1 2 3] [1 2 "a"]] true
    [[1 2 3] [1 2 3 -100]] true
    [[[1 2]] [[1 3]]] true
    ; sets
    [#{1} #{1}] false
    [#{1} #{2}] true
    [#{1 2} #{3}] true
    [#{100} #{1 10001}] false
    [#{{"x" 1}} #{{"x" 2}}] true
    ; booleans
    [true true] false
    [true false] false
    [false true] true
    [false 0] true
    [true 0] true
    ; null
    [nil nil] false
    [nil 1] true
    [nil ""] true
    [nil false] true))

(deftest builtin-gt-test
  (testing-builtin "gt"
    [1 1] false
    [1.001 1.001] false
    [1.001 1.002] false
    [2 1] true
    [1 2] false
    [1.9 2] false
    [-2 2] false
    ; strings
    ["a" "a"] false
    ["b" "a"] true
    ["a" "b"] false
    ; objects
    [{"foo" "bar"} {"foo" "bar"}] false
    [{"foo" 1} {"foo" 2}] false
    [{"foo" 1} {"foo" 1 "bar" 2}] true
    [{"foo" {"x" 1}} {"foo" {"x" 2}}] false
    [{"foo" [1 2]} {"foo" [1 2 3]}] false
    ; objects-different-keys
    [{"foo" 1} {"bar" 2}] true
    ; arrays
    [["foo" "bar"] ["foo" "bar"]] false
    [["a" "b"] ["a" "c"]] false
    [[1 2 3] [1 2 4]] false
    [[1 2 3] [1 2 "a"]] false
    [[1 2 3] [1 2 3 -100]] false
    [[[1 2]] [[1 3]]] false
    ; sets
    [#{1} #{1}] false
    [#{1} #{2}] false
    [#{1 2} #{3}] false
    [#{100} #{1 10001}] true
    [#{{"x" 1}} #{{"x" 2}}] false
    ; booleans
    [true true] false
    [true false] true
    [false true] false
    [false 0] false
    [true 0] false
    ; null
    [nil nil] false
    [nil 1] false
    [nil ""] false
    [nil false] false))

; These are already tested by virtue of rego-compare and the tests above
; Just sanity checking here..

(deftest builtin-lte-test
  (testing-builtin "lte"
    [1 1] true
    [1 2] true
    [2 1] false))

(deftest builtin-gte-test
  (testing-builtin "gte"
    [1 1] true
    [1 2] false
    [2 1] true))

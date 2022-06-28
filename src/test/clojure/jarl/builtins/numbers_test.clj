(ns jarl.builtins.numbers-test
  (:require [clojure.test :refer [deftest is]]
            [test.utils :refer [testing-builtin]]
            [jarl.builtins.numbers :refer [builtin-rem]]
            [jarl.exceptions :as errors])
  (:import (clojure.lang ExceptionInfo)))

(deftest builtin-plus-test
  (testing-builtin "plus"
    [1 1] 2
    [1.5 1.5] 3
    [1.6 1.6] 3.2
    [-1.0 1] 0))

(deftest builtin-minus-test
  (testing-builtin "minus"
    [1 1] 0
    [1.5 2] -0.5
    [-1.5 -4.5] 3
    [-1.0 1] -2
    ; set difference
    [#{1 2 3} #{2 3}] #{1}
    [#{1 2 3} #{4}] #{1 2 3}
    [#{1 2 3} #{}] #{1 2 3}
    [#{1 2 3} #{2 3 4}] #{1}))

(deftest builtin-mul-test
  (testing-builtin "mul"
    [1 1] 1
    [1.5 2] 3
    [-1.5 -4.5] 6.75
    [-1.0 1] -1))

(deftest builtin-div-test
  (testing-builtin "div"
    [1 1] 1
    [1.0 1.0] 1
    [4 2] 2
    [1 3] 0.3333333333333333
    [1 2] 0.5
    ; divide by zero
    [2 0] [:jarl.exceptions/builtin-exception "divide by zero"]))

(deftest builtin-rem-test
  (testing-builtin "rem"
    [5 2] 1
    [2 2] 0
    [2.0 2.0] 0
    ; non-int result is undefined
    [2.0 1.5] [:jarl.exceptions/builtin-exception "modulo on floating-point number"]
    ; rem by zero
    (try
      (builtin-rem {:args [2 0]})
      (catch ExceptionInfo e
        (is (= (ex-message e) "modulo by zero"))
        (is (= (errors/ex-type e) "eval_builtin_error"))))))

(deftest builtin-round-test
  (testing-builtin "round"
    [1] 1
    [1.4] 1
    [1.5] 2))

(deftest builtin-ceil-test
  (testing-builtin "ceil"
    [1] 1
    [1.4] 2
    [1.01] 2))

(deftest builtin-floor-test
  (testing-builtin "floor"
    [1] 1
    [1.4] 1
    [1.999] 1))

(deftest builtin-abs-test
  (testing-builtin "abs"
    [1] 1
    [-1] 1
    [-1.4] 1.4))

(deftest builtin-numbers-range-test
  (testing-builtin "numbers.range"
    [1 5] [1 2 3 4 5]
    [1 1] [1]
    [5 1] [5 4 3 2 1]
    [49649733057 49649733060] [49649733057, 49649733058, 49649733059, 49649733060]))

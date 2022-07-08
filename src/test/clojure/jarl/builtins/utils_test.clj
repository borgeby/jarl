(ns jarl.builtins.utils-test
  (:require [clojure.test :refer [deftest is testing]]
            [jarl.exceptions :as errors]
            [jarl.builtins.utils :refer [typed-seq]]
            [clojure.string :as str]))

(deftest builtin-ex-test
  (testing "formatting"
    (let [ex (errors/builtin-ex "my message #%d caused by %s" 1 "incompetence")]
      (is (errors/builtin-ex? ex))
      (is (= (ex-message ex) "eval_builtin_error: my message #1 caused by incompetence"))))
  (testing "no formatting"
    (let [ex (errors/builtin-ex "my message caused by insomnia")]
      (is (errors/builtin-ex? ex))
      (is (= (ex-message ex) "eval_builtin_error: my message caused by insomnia")))))

(deftest typed-seq-test
  (testing "typed-seq compliant"
    (is (nil? (typed-seq "foo" ["1", 2, "foo"] #{"string" "number"})))
    (is (nil? (typed-seq "bar" #{[1, 2], {"foo" "bar"}} ["array" "object"]))))
  (testing "typed-seq violations"
    (let [e (errors/try-return #(typed-seq "foo" [1] #{"string"}))]
      (is (= (errors/ex-type e) ::errors/type-exception))
      (is (str/includes? (ex-message e) "operand must be array or set of string but got array or set containing number")))
    (let [e (errors/try-return #(typed-seq "foo "["foo" 1] #{"string"}))]
      (is (= (errors/ex-type e) ::errors/type-exception))
      (is (str/includes? (ex-message e) "operand must be array or set of string but got array or set containing number")))))

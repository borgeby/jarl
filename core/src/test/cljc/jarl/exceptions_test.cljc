(ns jarl.exceptions-test
  (:require #?(:clj  [clojure.test :refer [deftest testing is]]
               :cljs [cljs.test :refer [deftest testing is]])
            [jarl.exceptions :refer [rego-type builtin-ex conflict-ex assignment-conflict-ex
                                     multiple-outputs-conflict-ex type-ex undefined-ex throws? try-or]]))

(deftest rego-type-test
  (testing "exception type to rego"
    (is (= (rego-type (builtin-ex "foo")) "eval_builtin_error"))
    (is (= (rego-type (conflict-ex "foo")) "eval_conflict_error"))
    (is (= (rego-type (assignment-conflict-ex "foo")) "eval_conflict_error"))
    (is (= (rego-type (multiple-outputs-conflict-ex (builtin-ex "foo") "bar")) "eval_conflict_error"))
    (is (= (rego-type (type-ex "foo")) "eval_type_error"))
    (is (= (rego-type (undefined-ex "foo")) ""))))

(deftest throws?-test
  (testing "throws?"
    (is (= (throws? #(+ 1 1)) false))
    (is (= (throws? #(throw (ex-info "fail" {}))) true))))

(deftest try-or-test
  (testing "try-or"
    (is (= (try-or #(throw (ex-info "fail" {:something :something})) 1) 1))
    (is (= (try-or #(+ 0.5 0.5) 1000) 1.0))))

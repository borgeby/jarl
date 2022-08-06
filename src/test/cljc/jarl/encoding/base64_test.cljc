(ns jarl.encoding.base64-test
  (:require #?(:clj  [clojure.test :refer [deftest testing is]]
               :cljs [cljs.test :refer [deftest testing is]])
            [jarl.encoding.base64 :as base64]))

(deftest base64?-test
  (testing "base64?"
    (is (= (base64/base64? "abc")      false))
    (is (= (base64/base64? "abc=")     true))
    (is (= (base64/base64? "fB/a14B=") true))
    (is (= (base64/base64? "fB=/a1")   false))
    (is (= (base64/base64? "abcÖ")     false))))

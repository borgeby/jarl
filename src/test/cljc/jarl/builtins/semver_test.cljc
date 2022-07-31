(ns jarl.builtins.semver-test
    (:require  [test.utils   :refer [testing-builtin]]
             #?(:clj  [clojure.test :refer [deftest]]
                :cljs [cljs.test    :refer [deftest]])))

(deftest builtin-semver-compare-test
  (testing-builtin "semver.compare"
    ["1.0.0" "1.1.1"] -1
    ["1.0.0" "1.0.0"] 0
    ["1.1.1" "1.0.0"] 1))

(deftest builtin-semver-is-valid-test
  (testing-builtin "semver.is_valid"
    ["1.0.0"] true
    ["foo"]   false))

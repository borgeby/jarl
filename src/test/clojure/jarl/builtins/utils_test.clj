(ns jarl.builtins.utils_test
  (:require [clojure.test :refer [deftest is testing]]
            [jarl.builtins.utils :refer [builtin-ex]])
  (:import (se.fylling.jarl BuiltinException)))

(deftest builtin-ex-test
  (testing "formatting"
    (let [ex (builtin-ex "my message #%d caused by %s" 1 "incompetence")]
      (is (instance? BuiltinException ex))
      (is (= (.getMessage ^Throwable ex) "my message #1 caused by incompetence"))))
  (testing "no formatting"
    (let [ex (builtin-ex "my message caused by insomnia")]
      (is (instance? BuiltinException ex))
      (is (= (.getMessage ^Throwable ex) "my message caused by insomnia")))))

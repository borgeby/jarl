(ns jarl.builtins.crypto-test
  (:require  [test.utils   :refer [testing-builtin]]
    #?(:clj  [clojure.test :refer [deftest]]
       :cljs [cljs.test    :refer [deftest]])))

(deftest builtin-crypto-hmac-md5-test
  (testing-builtin "crypto.hmac.md5"
    ["foo" "bar"] "31b6db9e5eb4addb42f1a6ca07367adc"))

(deftest builtin-crypto-hmac-sha1-test
  (testing-builtin "crypto.hmac.sha1"
    ["foo" "bar"] "85d155c55ed286a300bd1cf124de08d87e914f3a"))

(deftest builtin-crypto-hmac-sha256-test
  (testing-builtin "crypto.hmac.sha256"
    ["foo" "bar"] "147933218aaabc0b8b10a2b3a5c34684c8d94341bcf10a4736dc7270f7741851"))

(deftest builtin-crypto-hmac-sha512-test
  (testing-builtin "crypto.hmac.sha512"
    ["foo" "bar"] "24257d7210582a65c731ec55159c8184cc24c02489453e58587f71f44c23a2d61b4b72154a89d17b2d49448a8452ea066f4fc56a2bcead45c088572ffccdb3d8"))

(deftest builtin-crypto-md5
  (testing-builtin "crypto.md5"
    ["foo"] "acbd18db4cc2f85cedef654fccc4a4d8"))

(deftest builtin-crypto-sha1
  (testing-builtin "crypto.sha1"
    ["foo"] "0beec7b5ea3f0fdbc95d0dd47f3c5bc275da8a33"))

(deftest builtin-crypto-sha256
  (testing-builtin "crypto.sha256"
    ["foo"] "2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae"))

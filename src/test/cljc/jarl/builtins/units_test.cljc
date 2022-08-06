(ns jarl.builtins.units-test
  (:require  [test.utils   :refer [testing-builtin]]
    #?(:clj  [clojure.test :refer [deftest]]
       :cljs [cljs.test    :refer [deftest]])))

(deftest builtin-units-parse-test
  (testing-builtin "units.parse"
    ["100p"] 100000000000000000
    ["1ki"]  1024))

(deftest builtin-units-parse-bytes-test
  (testing-builtin "units.parse_bytes"
    ["1MB"]     1000000
    ["2.52Tb"]  2520000000000
    ["33.3gib"] 35755602739
    ["1kb"]     1000
    ["1kib"]    1024
    ["1"]       1))

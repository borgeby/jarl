(ns jarl.encoding.json-test
  (:require #?(:clj  [clojure.test :refer [deftest testing is]]
               :cljs [cljs.test    :refer [deftest testing is]])
            [clojure.string        :as str]
            [jarl.encoding.json    :as json]))

(deftest read-str-test
  (testing "basic decoding"
    (is (= (json/read-str "{\"foo_bar\": 1}")                          {"foo_bar" 1}))
    (is (= (json/read-str "{\"foo_bar\": \"baz\"}")                    {"foo_bar" "baz"})))
  (testing "with key-fn"
    (is (= (json/read-str "{\"foo_bar\": 1}" {:key-fn str/upper-case}) {"FOO_BAR" 1}))))

(deftest write-str-test
  (testing "basic encoding"
    (is (= (json/write-str {"foo_bar" 1})                              "{\"foo_bar\":1}")))
  (testing "with key-fn"
    (is (= (json/write-str {"foo_bar" 1} {:key-fn str/upper-case})     "{\"FOO_BAR\":1}"))))

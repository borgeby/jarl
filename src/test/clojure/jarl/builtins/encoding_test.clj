(ns jarl.builtins.encoding_test
  (:require [clojure.test :refer [deftest is testing]]
            [jarl.builtins.encoding :refer [builtin-base64-encode builtin-base64-decode builtin-base64-url-encode
                                            builtin-base64-url-encode-no-pad builtin-base64-url-decode
                                            builtin-url-query-encode builtin-url-query-decode builtin-json-unmarshal
                                            builtin-json-is-valid builtin-hex-encode builtin-hex-decode]])
  (:import (se.fylling.jarl BuiltinException)))

(deftest builtin-base64-encode-test
  (testing "base64.encode"
    (is (= (builtin-base64-encode "abc123") "YWJjMTIz"))
    (is (= (builtin-base64-encode "målarfärg") "bcOlbGFyZsOkcmc="))
    (is (= (builtin-base64-encode "The Rego Playground") "VGhlIFJlZ28gUGxheWdyb3VuZA=="))
    (is (= (builtin-base64-encode "😆") "8J+Yhg=="))))

(deftest builtin-base64-decode-test
  (testing "base64.decode"
    (is (= (builtin-base64-decode "YWJjMTIz") "abc123"))
    (is (= (builtin-base64-decode "bcOlbGFyZsOkcmc=") "målarfärg"))
    (is (= (builtin-base64-decode "VGhlIFJlZ28gUGxheWdyb3VuZA==") "The Rego Playground"))
    (is (= (builtin-base64-decode "8J+Yhg==") "😆"))))

(deftest builtin-base64-url-encode-test
  (testing "base64url.encode"
    (is (= (builtin-base64-url-encode "abc123") "YWJjMTIz"))
    (is (= (builtin-base64-url-encode "målarfärg") "bcOlbGFyZsOkcmc="))
    (is (= (builtin-base64-url-encode "The Rego Playground") "VGhlIFJlZ28gUGxheWdyb3VuZA=="))
    (is (= (builtin-base64-url-encode "😆") "8J-Yhg==")))) ; + replaced with -

(deftest builtin-base64-url-encode-no-pad-test
  (testing "base64url.encode_no_pad"
    (is (= (builtin-base64-url-encode-no-pad "abc123") "YWJjMTIz"))
    (is (= (builtin-base64-url-encode-no-pad "målarfärg") "bcOlbGFyZsOkcmc"))
    (is (= (builtin-base64-url-encode-no-pad "The Rego Playground") "VGhlIFJlZ28gUGxheWdyb3VuZA"))
    (is (= (builtin-base64-url-encode-no-pad "😆") "8J-Yhg")))) ; + replaced with -

(deftest builtin-base64-url-decode-test
  (testing "base64url.decode"
    (is (= (builtin-base64-url-decode "YWJjMTIz") "abc123"))
    (is (= (builtin-base64-url-decode "bcOlbGFyZsOkcmc=") "målarfärg"))
    (is (= (builtin-base64-url-decode "VGhlIFJlZ28gUGxheWdyb3VuZA==") "The Rego Playground"))
    (is (= (builtin-base64-url-decode "8J-Yhg==") "😆"))))

(deftest builtin-url-query-encode-test
  (testing "urlquery.encode"
    (is (= (builtin-url-query-encode "foo bar") "foo+bar"))
    (is (= (builtin-url-query-encode "blå") "bl%C3%A5"))
    (is (= (builtin-url-query-encode "/&=") "%2F%26%3D"))))

(deftest builtin-url-query-decode-test
  (testing "urlquery.decode"
    (is (= (builtin-url-query-decode "foo+bar") "foo bar"))
    (is (= (builtin-url-query-decode "bl%C3%A5") "blå"))
    (is (= (builtin-url-query-decode "%2F%26%3D") "/&="))))

(deftest builtin-json-unmarshal-test
  (testing "json.unmarshal"
    (is (= (builtin-json-unmarshal "{}") {}))
    (is (= (builtin-json-unmarshal "null") nil))
    (is (= (builtin-json-unmarshal "[1, 2, 3]") [1 2 3]))))

(deftest builtin-json-is-valid-test
  (testing "json.is_valid"
    (is (= (builtin-json-is-valid "{}") true))
    (is (= (builtin-json-is-valid "null") true))
    (is (= (builtin-json-is-valid "[1, 2, 3]") true))
    (is (= (builtin-json-is-valid "") false))
    (is (= (builtin-json-is-valid "foo") false))
    (is (= (builtin-json-is-valid "[[1, 2, 3]") false))))

(deftest builtin-hex-encode-test
  (testing "hex.encode"
    (is (= (builtin-hex-encode "foobar") "666f6f626172"))
    (is (= (builtin-hex-encode "🍺") "f09f8dba"))
    (is (= (builtin-hex-encode "hex! hex!") "686578212068657821"))))

(deftest builtin-hex-decode-test
  (testing "hex.decode"
    (is (= (builtin-hex-decode "666f6f626172") "foobar"))
    (is (= (builtin-hex-decode "f09f8dba") "🍺"))
    (is (= (builtin-hex-decode "686578212068657821") "hex! hex!")))
  (testing "invalid hex"
    (is (thrown-with-msg? BuiltinException #"invalid byte: U\+0067 'g'" (builtin-hex-decode "fghijkl")))))

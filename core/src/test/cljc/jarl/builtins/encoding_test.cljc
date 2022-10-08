(ns jarl.builtins.encoding-test
  (:require  [test.utils   :refer [testing-builtin]]
    #?(:clj  [clojure.test :refer [deftest]]
       :cljs [cljs.test    :refer [deftest]])))

(deftest builtin-base64-encode-test
  (testing-builtin "base64.encode"
    ["abc123"] "YWJjMTIz"
    ["målarfärg"] "bcOlbGFyZsOkcmc="
    ["The Rego Playground"] "VGhlIFJlZ28gUGxheWdyb3VuZA=="
    ["😆"] "8J+Yhg=="))

(deftest builtin-base64-decode-test
  (testing-builtin "base64.decode"
    ["YWJjMTIz"] "abc123"
    ["bcOlbGFyZsOkcmc="] "målarfärg"
    ["VGhlIFJlZ28gUGxheWdyb3VuZA=="] "The Rego Playground"
    ["8J+Yhg=="] "😆"))

(deftest builtin-base64-url-encode-test
  (testing-builtin "base64url.encode"
    ["abc123"] "YWJjMTIz"
    ["målarfärg"] "bcOlbGFyZsOkcmc="
    ["The Rego Playground"] "VGhlIFJlZ28gUGxheWdyb3VuZA=="
    ["😆"] "8J-Yhg==")) ; + replaced with -

(deftest builtin-base64-url-encode-no-pad-test
  (testing-builtin "base64url.encode_no_pad"
    ["abc123"] "YWJjMTIz"
    ["målarfärg"] "bcOlbGFyZsOkcmc"
    ["The Rego Playground"] "VGhlIFJlZ28gUGxheWdyb3VuZA"
    ["😆"] "8J-Yhg")) ; + replaced with -

(deftest builtin-base64-url-decode-test
  (testing-builtin "base64url.decode"
    ["YWJjMTIz"] "abc123"
    ["bcOlbGFyZsOkcmc="] "målarfärg"
    ["VGhlIFJlZ28gUGxheWdyb3VuZA=="] "The Rego Playground"
    ["8J-Yhg=="] "😆"))

(deftest builtin-url-query-encode-test
  (testing-builtin "urlquery.encode"
    ["foo bar"] "foo+bar"
    ["blå"] "bl%C3%A5"
    ["/&="] "%2F%26%3D"))

(deftest builtin-url-query-encode-object-test
  (testing-builtin "urlquery.encode_object"
    [{"foo" ["bar"]}]                             "foo=bar"
    [{"foo" ["bar" "baz"]}]                       "foo=bar&foo=baz"
    [{"foo" ["bar" "baz"] "a" ["b"] "c" ["d"]}]   "foo=bar&foo=baz&a=b&c=d"
    ; string values
    [{"a" "b" "c" "d"}                            "a=b&c=d"]))

(deftest builtin-url-query-decode-test
  (testing-builtin "urlquery.decode"
    ["foo+bar"]   "foo bar"
    ["bl%C3%A5"]  "blå"
    ["%2F%26%3D"] "/&="))

(deftest builtin-url-query-decode-object-test
  (testing-builtin "urlquery.decode_object"
    ["foo=bar"]                                 {"foo" ["bar"]}
    ["foo=bar&bar=baz"]                         {"foo" ["bar"] "bar" ["baz"]}
    ["foo=bar&bar=baz&foo=qux"]                 {"foo" ["bar" "qux"] "bar" ["baz"]}
    ["f%20o%20o=b%20a%20r&f%20o%20o=b%20a%20z"] {"f o o" ["b a r" "b a z"]}
    ["f+o+o=b+a+r&f+o+o=b+a+z"]                 {"f o o" ["b a r" "b a z"]}
    ; empty parameter
    ["a=1&b"]                                   {"a" ["1"] "b" [""]}))

(deftest builtin-json-unmarshal-test
  (testing-builtin "json.unmarshal"
    ["{}"] {}
    ["null"] nil
    ["[1, 2, 3]"] [1 2 3]))

(deftest builtin-json-is-valid-test
  (testing-builtin "json.is_valid"
    ["{}"] true
    ["null"] true
    ["[1, 2, 3]"] true
    [""] false
    ["foo"] false
    ["[[1, 2, 3]"] false))

(deftest builtin-hex-encode-test
  (testing-builtin "hex.encode"
    ["foobar"] "666f6f626172"
    ["🍺"] "f09f8dba"
    ["hex! hex!"] "686578212068657821"))

(deftest builtin-hex-decode-test
  (testing-builtin "hex.decode"
    ["666f6f626172"] "foobar"
    ["f09f8dba"] "🍺"
    ["686578212068657821"] "hex! hex!"
    ; invalid hex
    ["fghijkl"] [:jarl.exceptions/builtin-exception "invalid byte: U\\+0067 'g'"]))

#?(:clj
   (deftest builtin-yaml-marshal-test
     (testing-builtin "yaml.marshal"
       [{"foo" "bar"}] "foo: bar\n"
       ; "sorted keys"
       [{"foo" "bar" "baz" {"x" 5}}] "baz:\n  x: 5\nfoo: bar\n")))

#?(:clj
   (deftest builtin-yaml-unmarshal-test
     (testing-builtin "yaml.unmarshal"
       ["foo: bar"] {"foo" "bar"}
       ; mimic error message from OPA
       ["[1, 2"] [:jarl.exceptions/builtin-exception "yaml: line 1: did not find expected ',' or ']'"])))

#?(:clj
   (deftest builtin-yaml-is-valid-test
     (testing-builtin "yaml.is_valid"
       ["foo: bar"] true
       ["[["] false)))

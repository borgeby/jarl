(ns jarl.builtins.strings_test
  (:require [clojure.test :refer [deftest]]
            [test.utils :refer [testing-builtin]])
  (:import (se.fylling.jarl BuiltinException)))

(deftest builtin-concat-test
  (testing-builtin "concat"
    [", " ["a" "b" "c"]] "a, b, c"
    ["🙂" ["🙃" "🙃" "🙃"]] "🙃🙂🙃🙂🙃"
    ; concat sets
    [", " #{"a", "b", "c"}] "a, b, c"
    [", " #{"c", "b", "a"}] "a, b, c"))

(deftest builtin-contains-test
  (testing-builtin "contains"
    ["some text included" "text"] true
    ["some ünicÖde works" "ünicÖde"] true
    ["negative test" "positive"] false
    ["🍧🍨🧁🍰🍮" "🍨🧁🍰"] true))

(deftest builtin-endswith-test
  (testing-builtin "endswith"
    ["some text included" "included"] true
    ["some ünicÖde" "ünicÖde"] true
    ["negative test" "positive"] false))

(deftest builtin-format-int-test
  (testing-builtin "format_int"
    [10 10] "10"
    [10 2] "1010"
    [10 16] "a"))

(deftest builtin-indexof-test
  (testing-builtin "indexof"
    ["some text included" "text"] 5
    ["some ünicÖde" "ünicÖde"] 5
    ["negative test" "positive"] -1
    ["🍧🍨🧁🍰🍮" "🍮"] 4))

(deftest builtin-indexof-n-test
  (testing-builtin "indexof_n"
    ["some text included" "e"] [3 6 16]
    ["some ünicÖde" "Ö"] [9]
    ["negative test" "positive"] []
    ["🍧🍮🍨🧁🍰🍮" "🍮"] [1 5]))

(deftest builtin-lower-test
  (testing-builtin "lower"
    ["ABBA"] "abba"
    ["ÛnicÖde"] "ûnicöde"))

(deftest builtin-replace-test
  (testing-builtin "replace"
    ["abba" "a" "e"] "ebbe"
    ["abba" "e" "a"] "abba"
    ["ÛnicÖde" "Ö" "o"] "Ûnicode"))

(deftest builtin-strings-reverse-test
  (testing-builtin "strings.reverse"
    ["abba"] "abba"
    ["ÛnicÖde"] "edÖcinÛ"))

(deftest builtin-split-test
  (testing-builtin "split"
    ["a,b,c" ","] ["a" "b" "c"]
    ["abc", ","] ["abc"]
    ["abc" ""] ["a" "b" "c"]
    ["abc" "a"] ["" "bc"]
    ["åäö" ""] ["å" "ä" "ö"]
    [",a,b,,c", ","] ["", "a", "b", "", "c"]
    ; regex escape
    ["a[b[c" "["] ["a" "b" "c"]
    ["a*b*c" "*"] ["a" "b" "c"]
    ; string and delim are the same (quirk)
    ["abc", "abc"] ["" ""]))

(deftest builtin-startswith-test
  (testing-builtin "startswith"
    ["some text included" "some"] true
    ["ünicÖde everywhere" "ünicÖde"] true
    ["negative test" "positive"] false))

(deftest builtin-substring-test
  (testing-builtin "substring"
    ["abcde" 1 3] "bcd"
    ["aaa" 4 -1] ""
    ["aaa" 3 3] ""
    ["abcde" 0 5] "abcde"
    ["ünicÖde" 4 1] "Ö"
    ["a" 0 100] "a"
    ["everything" 0 -1] "everything"
    ; code points
    ["⭐🚀🙂" 0 1] "⭐"
    ["⭐🚀🙂" 1 1] "🚀"
    ["⭐🚀🙂" 0 -1] "⭐🚀🙂"
    ["⭐🚀🙂" 1 -1] "🚀🙂"
    ["𨦇𨦈𥻘" 1 2] "𨦈𥻘"
    ; negative offset
    ["a" -1 1] [BuiltinException "negative offset"]))

(deftest builtin-trim-test
  (testing-builtin "trim"
    ["abcde" "ae"] "bcd"
    ["abcde" "cbae"] "d"
    ["aalalaah" "ah"] "lal"
    ["   test " " t"] "es"
    ["foo" "foo"] ""))

(deftest builtin-trim-left-test
  (testing-builtin "trim_left"
    ["abcde" "x"] "abcde"
    ["abcde" "cba"] "de"
    ["åäö" "åä"] "ö"
    ["   test" " t"] "est"
    ["foo" "foo"] ""))

(deftest builtin-trim-prefix-test
  (testing-builtin "trim_prefix"
    ["some text included" "some "] "text included"
    ["ünicÖde everywhere" "ünicÖde "] "everywhere"
    ["negative test" "positive"] "negative test"))

(deftest builtin-trim-right-test
  (testing-builtin "trim_right"
    ["abcde" "x"] "abcde"
    ["abcde" "cde"] "ab"
    ["åäö" "öä"] "å"
    ["test   " " t"] "tes"))

(deftest builtin-trim-suffix-test
  (testing-builtin "trim_suffix"
    ["some text included" " included"] "some text"
    ["ünicÖde everywhere" " everywhere"] "ünicÖde"
    ["negative test" "positive"] "negative test"))

(deftest builtin-trim-space-test
  (testing-builtin "trim_space"
    ["  foo  "] "foo"
    ["foo"] "foo"
    ["\n\t foo\n \n"] "foo"))

(deftest builtin-upper-test
  (testing-builtin "upper"
    ["abba"] "ABBA"
    ["ûnicöde"] "ÛNICÖDE"))

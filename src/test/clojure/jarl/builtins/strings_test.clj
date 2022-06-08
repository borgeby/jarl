(ns jarl.builtins.strings_test
  (:require [clojure.test :refer [deftest is testing]]
            [jarl.builtins.strings :refer [builtin-concat builtin-contains builtin-endswith builtin-format-int
                                           builtin-indexof builtin-indexof-n builtin-lower builtin-replace
                                           builtin-strings-reverse builtin-split builtin-startswith builtin-substring
                                           builtin-trim builtin-trim-left builtin-trim-prefix builtin-trim-right
                                           builtin-trim-suffix builtin-trim-space builtin-upper]])
  (:import (se.fylling.jarl BuiltinException)))

(deftest builtin-concat-test
  (testing "concat"
    (is (= (builtin-concat ", " ["a" "b" "c"]) "a, b, c"))
    (is (= (builtin-concat "🙂" ["🙃" "🙃" "🙃"]) "🙃🙂🙃🙂🙃")))
  (testing "concat sets"
    (is (= (builtin-concat ", " #{"a", "b", "c"}) "a, b, c"))
    (is (= (builtin-concat ", " #{"c", "b", "a"}) "a, b, c"))))

(deftest builtin-contains-test
  (testing "contains"
    (is (= (builtin-contains "some text included" "text") true))
    (is (= (builtin-contains "some ünicÖde works" "ünicÖde") true))
    (is (= (builtin-contains "negative test" "positive") false))
    (is (= (builtin-contains "🍧🍨🧁🍰🍮" "🍨🧁🍰") true))))

(deftest builtin-endswith-test
  (testing "endswith"
    (is (= (builtin-endswith "some text included" "included") true))
    (is (= (builtin-endswith "some ünicÖde" "ünicÖde") true))
    (is (= (builtin-endswith "negative test" "positive") false))))

(deftest builtin-format-int-test
  (testing "format_int"
    (is (= (builtin-format-int 10 10) "10"))
    (is (= (builtin-format-int 10 2) "1010"))
    (is (= (builtin-format-int 10 16) "a"))))

(deftest builtin-indexof-test
  (testing "indexof"
    (is (= (builtin-indexof "some text included" "text") 5))
    (is (= (builtin-indexof "some ünicÖde" "ünicÖde") 5))
    (is (= (builtin-indexof "negative test" "positive") -1))
    (is (= (builtin-indexof "🍧🍨🧁🍰🍮" "🍮") 4))))

(deftest builtin-indexof-n-test
  (testing "indexof_n"
    (is (= (builtin-indexof-n "some text included" "e") [3 6 16]))
    (is (= (builtin-indexof-n "some ünicÖde" "Ö") [9]))
    (is (= (builtin-indexof-n "negative test" "positive") []))
    (is (= (builtin-indexof-n "🍧🍮🍨🧁🍰🍮" "🍮") [1 5]))))

(deftest builtin-lower-test
  (testing "lower"
    (is (= (builtin-lower "ABBA") "abba"))
    (is (= (builtin-lower "ÛnicÖde") "ûnicöde"))))

(deftest builtin-replace-test
  (testing "replace"
    (is (= (builtin-replace "abba" "a" "e") "ebbe"))
    (is (= (builtin-replace "abba" "e" "a") "abba"))
    (is (= (builtin-replace "ÛnicÖde" "Ö" "o") "Ûnicode"))))

(deftest builtin-strings-reverse-test
  (testing "strings.reverse"
    (is (= (builtin-strings-reverse "abba") "abba"))
    (is (= (builtin-strings-reverse "ÛnicÖde") "edÖcinÛ"))))

(deftest builtin-split-test
  (testing "split"
    (is (= (builtin-split "a,b,c" ",") ["a" "b" "c"]))
    (is (= (builtin-split "abc", ",") ["abc"]))
    (is (= (builtin-split "abc" "") ["a" "b" "c"]))
    (is (= (builtin-split "abc" "a") ["" "bc"]))
    (is (= (builtin-split "åäö" "") ["å" "ä" "ö"]))
    (is (= (builtin-split ",a,b,,c", ",") ["", "a", "b", "", "c"])))
  (testing "regex escape"
    (is (= (builtin-split "a[b[c" "[") ["a" "b" "c"]))
    (is (= (builtin-split "a*b*c" "*") ["a" "b" "c"])))
  (testing "string and delim are the same (quirk)"
    (is (= (builtin-split "abc", "abc") ["" ""]))))

(deftest builtin-startswith-test
  (testing "startswith"
    (is (= (builtin-startswith "some text included" "some") true))
    (is (= (builtin-startswith "ünicÖde everywhere" "ünicÖde") true))
    (is (= (builtin-startswith "negative test" "positive") false))))

(deftest builtin-substring-test
  (testing "substring"
    (is (= (builtin-substring "abcde" 1 3) "bcd"))
    (is (= (builtin-substring "aaa" 4 -1) ""))
    (is (= (builtin-substring "aaa" 3 3) ""))
    (is (= (builtin-substring "abcde" 0 5) "abcde"))
    (is (= (builtin-substring "ünicÖde" 4 1) "Ö"))
    (is (= (builtin-substring "a" 0 100) "a"))
    (is (= (builtin-substring "everything" 0 -1) "everything")))
  (testing "code points"
    (is (= (builtin-substring "⭐🚀🙂" 0 1) "⭐"))
    (is (= (builtin-substring "⭐🚀🙂" 1 1) "🚀"))
    (is (= (builtin-substring "⭐🚀🙂" 0 -1) "⭐🚀🙂"))
    (is (= (builtin-substring "⭐🚀🙂" 1 -1) "🚀🙂"))
    (is (= (builtin-substring "𨦇𨦈𥻘" 1 2) "𨦈𥻘")))
  (testing "negative offset"
    (is (thrown-with-msg? BuiltinException #"negative offset" (builtin-substring "a" -1 1)))))

(deftest builtin-trim-test
  (testing "trim"
    (is (= (builtin-trim "abcde" "ae") "bcd"))
    (is (= (builtin-trim "abcde" "cbae") "d"))
    (is (= (builtin-trim "aalalaah" "ah") "lal"))
    (is (= (builtin-trim "   test " " t") "es"))
    (is (= (builtin-trim "foo" "foo") ""))))

(deftest builtin-trim-left-test
  (testing "trim_left"
    (is (= (builtin-trim-left "abcde" "x") "abcde"))
    (is (= (builtin-trim-left "abcde" "cba") "de"))
    (is (= (builtin-trim-left "åäö" "åä") "ö"))
    (is (= (builtin-trim-left "   test" " t") "est"))
    (is (= (builtin-trim-left "foo" "foo") ""))))

(deftest builtin-trim-prefix-test
  (testing "trim_prefix"
    (is (= (builtin-trim-prefix "some text included" "some ") "text included"))
    (is (= (builtin-trim-prefix "ünicÖde everywhere" "ünicÖde ") "everywhere"))
    (is (= (builtin-trim-prefix "negative test" "positive") "negative test"))))

(deftest builtin-trim-right-test
  (testing "trim_right"
    (is (= (builtin-trim-right "abcde" "x") "abcde"))
    (is (= (builtin-trim-right "abcde" "cde") "ab"))
    (is (= (builtin-trim-right "åäö" "öä") "å"))
    (is (= (builtin-trim-right "test   " " t") "tes"))))

(deftest builtin-trim-suffix-test
  (testing "trim_suffix"
    (is (= (builtin-trim-suffix "some text included" " included") "some text"))
    (is (= (builtin-trim-suffix "ünicÖde everywhere" " everywhere") "ünicÖde"))
    (is (= (builtin-trim-suffix "negative test" "positive") "negative test"))))

(deftest builtin-trim-space-test
  (testing "upper"
    (is (= (builtin-trim-space "  foo  ") "foo"))
    (is (= (builtin-trim-space "foo") "foo"))
    (is (= (builtin-trim-space "\n\t foo\n \n") "foo"))))

(deftest builtin-upper-test
  (testing "upper"
    (is (= (builtin-upper "abba") "ABBA"))
    (is (= (builtin-upper "ûnicöde") "ÛNICÖDE"))))

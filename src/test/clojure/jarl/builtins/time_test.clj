(ns jarl.builtins.time-test
  (:require [clojure.test :refer [deftest]]
            [jarl.utils :refer [instant-to-ns ns-to-instant]]
            [test.utils :refer [testing-builtin]]
            [jarl.builtins.time :refer [parse-iso-datetime parse-iso-zoned-datetime]])
  (:import (se.fylling.jarl BuiltinException)
           (java.time.temporal ChronoUnit)))

(deftest builtin-time-now-ns-test
  (testing-builtin "time.now_ns"
    {:args [] :builtin-context {:time-now-ns 1655283296943749000}} 1655283296943749000))

(deftest builtin-time-weekday-test
  (let [ns (instant-to-ns (parse-iso-datetime "2022-01-01T00:00:00"))]
    (testing-builtin "time.weekday"
      [1655283296943749000]      "Wednesday"
      [1656284296943749000]      "Sunday"
      [[ns "UTC"]]               "Saturday"
      [[ns "Europe/Stockholm"]]  "Saturday"
      [[ns "America/Anchorage"]] "Friday"
      ; exceptions
      [9655283296943749000]      [BuiltinException "timestamp too big"])))

(deftest builtin-time-parse-duration-ns-test
  (testing-builtin "time.parse_duration_ns"
    ["1ns"]                     1
    ["1ns2ms"]                  2000001
    ["1h"]                      3600000000000
    ["33h11m59s7ms99us12345ns"] 119519007111345
    ; exceptions
    ["h"]                       [BuiltinException "time: invalid duration \"h\""]
    ["33"]                      [BuiltinException "time: missing unit in duration \"33\""]
    ["33ks"]                    [BuiltinException "time: unknown unit \"ks\" in duration \"33ks\""]))

(deftest builtin-time-parse-ns-test
  (let [ref-time (instant-to-ns (parse-iso-zoned-datetime "2022-01-01T12:12:12.00-00:00")) ; 1641039132000000000
        ref-time-tz-offset (instant-to-ns (-> (ns-to-instant ref-time) (.plus 8 ChronoUnit/HOURS)))]
    (testing-builtin "time.parse_ns"
      ["Mon Jan 02 15:04:05 2006" "Sat Jan 01 12:12:12 2022"]             ref-time
      ; ANSIC
      ["Mon Jan _2 15:04:05 2006" "Sat Jan _1 12:12:12 2022"]             ref-time
      ; UnixDate
      ["Mon Jan _2 15:04:05 MST 2006" "Sat Jan _1 12:12:12 -0000 2022"]   ref-time
      ["Mon Jan _2 15:04:05 MST 2006" "Sat Jan _1 12:12:12 -0800 2022"]   ref-time-tz-offset
      ; RubyDate
      ["Mon Jan 02 15:04:05 -0700 2006" "Sat Jan 01 12:12:12 -0800 2022"] ref-time-tz-offset
      ; Reference time format
      ["01/02 03:04:05PM '06 -0700" "01/01 12:12:12PM '22 -0000"]         ref-time
      ["01/02 03:04:05PM '06 -0700" "06/02 07:00:00PM '17 -0700"]         1496455200000000000
      ; exceptions
      ["2006-01-02T15:04:05Z07:00" "2262-04-11T23:47:16.854775808-00:00"] [BuiltinException "time outside of valid range"])))

(deftest builtin-time-parse-rfc3339-ns-test
  (testing-builtin "time.parse_rfc3339_ns"
    ["2022-01-01T12:12:12.00-00:00"]        1641039132000000000
    ["2022-01-01T00:00:00.145224191-00:00"] 1640995200145224191
    ["2262-04-11T23:47:16.854775808-00:00"] [BuiltinException "time outside of valid range"]))

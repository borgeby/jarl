(ns jarl.builtins.time-test
  (:require [clojure.test :refer [deftest]]
            [jarl.utils :refer [instant-to-ns]]
            [test.utils :refer [testing-builtin]]
            [jarl.builtins.time :refer [parse-iso-datetime]])
  (:import (se.fylling.jarl BuiltinException)))

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

(deftest builtin-time-parse-rfc3339-ns-test
  (testing-builtin "time.parse_rfc3339_ns"
    ["2022-01-01T00:00:00.145224191-00:00"] 1640995200145224191))

(ns jarl.performance-test
  (:require [clojure.test :refer [deftest]]
            [clojure.java.io :as io]
            [jarl.parser :as parser]
            [test.benchmark :refer [benchmark-jarl]]))

;TODO: Compare against (2x?) last entry in jarl-perf.csv
(deftest ^:performance perf-test
  (let [ir (parser/parse-file (io/resource "rego/perf/plan.json"))
        bench-result (benchmark-jarl ir)]
    (println bench-result)))

(ns jarl.performance-test
  (:require [clojure.test :refer :all]
            [criterium.core :as criterium]
            [clojure.java.io :as io]
            [jarl.parser :as parser]
            [test.benchmark :refer [benchmark]]))

(deftest ^:performance perf-test
  (let [info (parser/parse-file (io/resource "rego/simple/plan.json"))
        benchmark-result (benchmark info "simple/p" {} {"x" 42})]
    (criterium/report-result benchmark-result)
    (println "MEAN:" (:sample-mean benchmark-result))))

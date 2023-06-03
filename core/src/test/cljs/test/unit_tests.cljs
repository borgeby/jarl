(ns test.unit-tests
  (:require [cljs.test :refer [run-tests report successful?]]
            [cljs.nodejs :as nodejs]
            [test.config]
            [jarl.builtins.aggregates-test]
            [jarl.builtins.array-test]
            [jarl.builtins.bits-test]
            [jarl.builtins.comparison-test]
            [jarl.builtins.conversions-test]
            [jarl.builtins.crypto-test]
            [jarl.builtins.graphs-test]
            [jarl.builtins.numbers-test]
            [jarl.builtins.objects-test]
            [jarl.builtins.opa-test]
            [jarl.builtins.semver-test]
            [jarl.builtins.sets-test]
            [jarl.builtins.strings-test]
            [jarl.builtins.time-test]
            [jarl.builtins.types-test]
            [jarl.builtins.encoding-test]
            [jarl.builtins.units-test]
            [jarl.config.reader-test]
            [jarl.config.schema-test]
            [jarl.encoding.base64-test]
            [jarl.encoding.json-test]
            [jarl.exceptions-test]
            [jarl.builtins.utils-test]))

; For now, include test namespaces manually. Later we might want to switch to run-all-tests:

; run-all-tests - macro
;(run-all-tests)
;(run-all-tests re)
;(run-all-tests re env)

;Runs all tests in all namespaces; prints results.
;Optional argument is a regular expression; only namespaces with
;names matching the regular expression (with re-matches) will be
;tested.

(def process (nodejs/require "process"))

; See https://clojurescript.org/tools/testing#detecting-test-completion-success
(defmethod report [:cljs.test/default :end-run-tests]
  [m]
  (when-not (successful? m)
    (aset process "exitCode" 1)))

(defn run []
  ; see docs on run-tests for how to handle return value!
  (run-tests 'jarl.builtins.aggregates-test
             'jarl.builtins.array-test
             'jarl.builtins.bits-test
             'jarl.builtins.comparison-test
             'jarl.builtins.conversions-test
             'jarl.builtins.crypto-test
             'jarl.builtins.encoding-test
             'jarl.builtins.graphs-test
             'jarl.builtins.numbers-test
             'jarl.builtins.objects-test
             'jarl.builtins.opa-test
             'jarl.builtins.semver-test
             'jarl.builtins.sets-test
             'jarl.builtins.strings-test
             'jarl.builtins.time-test
             'jarl.builtins.types-test
             'jarl.builtins.units-test
             'jarl.config.reader-test
             'jarl.config.schema-test
             'jarl.encoding.base64-test
             'jarl.encoding.json-test
             'jarl.exceptions-test
             'jarl.builtins.utils-test))

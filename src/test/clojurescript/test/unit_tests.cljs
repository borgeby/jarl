(ns test.unit-tests
  (:require [cljs.test :refer [run-tests]]
            [test.config]
            [jarl.builtins.aggregates-test]
            [jarl.builtins.array-test]
            [jarl.builtins.comparison-test]
            [jarl.builtins.conversions-test]
            [jarl.builtins.numbers-test]
            [jarl.builtins.sets-test]
            [jarl.builtins.types-test]
            [jarl.builtins.encoding-test]
            [jarl.exceptions-test]))

; For now, include test namespaces manually. Later we might want to switch to run-all-tests:

; run-all-tests - macro
;(run-all-tests)
;(run-all-tests re)
;(run-all-tests re env)

;Runs all tests in all namespaces; prints results.
;Optional argument is a regular expression; only namespaces with
;names matching the regular expression (with re-matches) will be
;tested.

(run-tests 'jarl.builtins.aggregates-test
           'jarl.builtins.array-test
           'jarl.builtins.comparison-test
           'jarl.builtins.conversions-test
           'jarl.builtins.numbers-test
           'jarl.builtins.sets-test
           'jarl.builtins.types-test
           'jarl.builtins.encoding-test
           'jarl.exceptions-test)

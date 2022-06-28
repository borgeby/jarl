(ns test.unit-tests
  (:require [cljs.test :refer-macros [run-tests]]
            [jarl.exceptions-test]
            [jarl.types-test]))

; For now, include test namespaces manually. Later we might want to switch to run-all-tests:

; run-all-tests - macro
;(run-all-tests)
;(run-all-tests re)
;(run-all-tests re env)

;Runs all tests in all namespaces; prints results.
;Optional argument is a regular expression; only namespaces with
;names matching the regular expression (with re-matches) will be
;tested.

(run-tests 'jarl.exceptions-test
           'jarl.types-test)

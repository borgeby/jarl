(ns test.compliance-tests
  (:require [cljs.test :refer [run-tests]]
            [test.compliance.generated.tests]))

(run-tests 'test.compliance.generated.tests)

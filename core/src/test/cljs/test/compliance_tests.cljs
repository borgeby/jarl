(ns test.compliance-tests
  (:require [cljs.test :refer [run-tests report successful?]]
            [cljs.nodejs :as nodejs]
            [test.config]
            [test.compliance.generated.tests]))

(def process (nodejs/require "process"))

; See https://clojurescript.org/tools/testing#detecting-test-completion-success
(defmethod report [:cljs.test/default :end-run-tests]
  [m]
  (when-not (successful? m)
    (aset process "exitCode" 1)))

(defn run []
  (run-tests 'test.compliance.generated.tests))

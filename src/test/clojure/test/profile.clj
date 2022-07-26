(ns test.profile
  "Profiler generating flame graphs. See https://github.com/clojure-goes-fast/clj-async-profiler for details"
  (:require [clojure.test :refer [run-test-var run-tests]]
            [taoensso.timbre :as log]
            [clj-async-profiler.core :as prof]
            [test.compliance.generated.tests]))

; NOTE: needs to be run with -Djdk.attach.allowAttachSelf
(defn -main []
  (log/set-level! :info)

  (prof/profile
    (dotimes [_ 100]
      (run-tests 'test.compliance.generated.tests)
      (comment "or call any function. to run a single test, yoy may use:"
        (run-test-var (resolve 'test.compliance.generated.tests/aggregates-member+some+key+ref)))))

  ; Serve flame graphs from local server
  (prof/serve-files 8080))

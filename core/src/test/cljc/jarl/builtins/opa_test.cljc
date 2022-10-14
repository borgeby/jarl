(ns jarl.builtins.opa-test
  (:require [clojure.test :refer [deftest]]
            [test.utils :refer [testing-builtin]]))

(deftest builtin-opa-runtime-test
  (testing-builtin "opa.runtime"
    {:args [] :builtin-context {:env {"ENV_KEY" "ENV_VAL"}}} {"commit"  ""
                                                              "env"     {"ENV_KEY" "ENV_VAL"}
                                                              "version" "jarl-dev"}))

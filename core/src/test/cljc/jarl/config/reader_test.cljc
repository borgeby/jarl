(ns jarl.config.reader-test
  (:require #?(:clj  [clojure.test    :refer [deftest testing is]]
               :cljs [cljs.test       :refer [deftest testing is]])
            [jarl.config.reader       :as config]
            [jarl.runtime.environment :as environment]))

(deftest read-json-config
  (testing "simple json config"
    (is (= (config/json->config "{\"labels\": {\"cluster\": \"dev\", \"team\": \"foobars\"}}")
           {:labels {:cluster "dev" :team "foobars"}})))

  (testing "variable substitution"
    (with-redefs [environment/*env* {"ENV" "prod" "CLUSTER" "starfleet" "TEAM" "foobars"}]
      (is (= (config/json->config "{\"labels\": {\"cluster\": \"${CLUSTER}-${ENV}\", \"team\": \"${TEAM}\"}}")
             {:labels {:cluster "starfleet-prod" :team "foobars"}})))))

#?(:clj (deftest read-yaml-config
  (testing "simple yaml config"
    (is (= (config/yaml->config "labels:\n  cluster: dev\n  team: foobars")
           {:labels {:cluster "dev" :team "foobars"}})))

  (testing "variable substitution"
    (with-redefs [environment/*env* {"ENV" "prod" "CLUSTER" "starfleet" "TEAM" "foobars"}]
      (is (= (config/yaml->config "labels:\n  cluster: ${CLUSTER}-${ENV}\n  team: ${TEAM}")
             {:labels {:cluster "starfleet-prod" :team "foobars"}}))))))

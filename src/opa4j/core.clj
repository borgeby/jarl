(ns opa4j.core
  (:gen-class)
  (:require [clojure.data.json :as json])
  (:require [opa4j.parser :as parser]))

(defn run-first-plan [data input]
  (let [[name plan] (first (get data :plans))]
    (println "running plan" name)
    (let [result (plan data input)]
      (println "Result-set:" result)
      result)))

(defn -main
  "Parses and the runs a plan"
  ([] (run-first-plan (parser/parse-file "rego/simple/plan.json") {}))
  ([ir-file input-json]
   (let [input (json/read-str input-json)]
     (run-first-plan (parser/parse-file ir-file) input))))
(ns opa4j.core
  (:gen-class)
  (:require [opa4j.parser :as parser]))

(defn run-first-plan [plans]
  (let [[name plan] (first plans)]
    (println "running plan" name)
    (plan)))

(defn -main
  "Parses and the runs a plan"
  ([] (run-first-plan (parser/parse-file "rego/simple/plan.json")))
  ([& args]
   (println "Welcome to my project! These are your args:" args)
   (run-first-plan (parser/parse-file (first args)))))
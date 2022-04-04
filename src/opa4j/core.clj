(ns opa4j.core
  (:gen-class)
  (:require [opa4j.parser :as parser]))

(defn -main
  "Parses and the runs a plan"
  ([] ((parser/parse-file "rego/simple/plan.json") "x"))
  ([& args]
   (println "Welcome to my project! These are your args:" args)
   ((parser/parse-file (first args)) "x")))
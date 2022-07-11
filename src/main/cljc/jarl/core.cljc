(ns jarl.core
  (:require [jarl.encoding.json :as json]
            [jarl.io :as jio]
            [jarl.parser :as parser]
            #?(:cljs [cljs.nodejs :as nodejs]))
  #?(:clj (:gen-class)))

#?(:cljs (enable-console-print!))
#?(:cljs (nodejs/enable-util-print!))

(defn run-first-plan [data input]
  (let [[name plan] (first (get data :plans))]
    (println "running plan" name)
    (let [result (plan data input)]
      (println "Result-set:" result)
      result)))

(defn -main
  "Parses and runs a plan"
  ([]
   (let [loc #?(:clj "src/test/resources/rego/simple/plan.json"
                :cljs (jio/path-resolve "src/test/resources/rego/simple/plan.json"))]
     (run-first-plan (parser/parse-json (jio/read-file loc)) {})))
  ([ir-file input-json]
   (let [input (json/read-str input-json)]
     (run-first-plan (parser/parse-json (jio/read-file ir-file)) input))))

#?(:cljs (set! *main-cli-fn* -main))

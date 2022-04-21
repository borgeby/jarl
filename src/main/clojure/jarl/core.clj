;
; Copyright 2022 Johan Fylling, Anders Eknert
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
;

(ns jarl.core
  (:gen-class)
  (:require [clojure.data.json :as json])
  (:require [jarl.parser :as parser]))

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
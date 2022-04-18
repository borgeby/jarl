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

(ns opa4j.api
  (:require [opa4j.parser :as parser]))

;
; PLAN
;

(gen-class
  :name "se.fylling.opa4j.PlanImpl"
  :implements [se.fylling.opa4j.Plan]
  :main false
  :prefix "-plan-"
  :constructors {[Object Object String] []}
  :state "state"
  :init "init"
  )

(defn -plan-init [info plan name]
  [[info] [info plan name]])

(defn -plan-eval [^se.fylling.opa4j.PlanImpl this input]
  (let [[info plan name] (.state this)]
    (if (nil? plan)
      (throw (Exception. (format "plan '%s' doesn't exist" name)))
      (plan info input))))

(defn -plans-toString [^se.fylling.opa4j.PlanImpl this]
  (let [[_ _ name] (.state this)]
    name))

;
; OPA4J
;

(gen-class
  :name "se.fylling.opa4j.Opa4jImpl"
  :implements [se.fylling.opa4j.Opa4j]
  :prefix "-opa4j-"
  :main false
  :state "state"
  :init "init"
  :constructors {[Object] []}
  )

(defn -opa4j-init [info]
  [[info] info])

(defn plan-by-name [plans name]
  (loop [plans plans]
    (if (empty? plans)
      nil
      (let [[current-name plan] (first plans)]
        (if (= current-name name)
          plan
          (recur (next plans)))))))

(defn -opa4j-getPlan [^se.fylling.opa4j.Opa4jImpl this name]
  (let [info (.state this)
        plans (get info :plans)
        plan (plan-by-name plans name)]
    (if (nil? plan)
      (throw (Exception. (format "plan '%s' doesn't exist" name)))
      (new se.fylling.opa4j.PlanImpl info plan name))))

;
; OPA4J FACTORY
;

(gen-class
  :name "se.fylling.opa4j.Opa4jFactory"
  :prefix "-opa4j-factory-"
  :main false
  :methods [^{:static true} [fromIr [String] se.fylling.opa4j.Opa4j]]
  )

(defn -opa4j-factory-fromIr [ir]
  (new se.fylling.opa4j.Opa4jImpl (parser/parse ir)))
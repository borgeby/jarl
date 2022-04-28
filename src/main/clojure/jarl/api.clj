(ns jarl.api
  (:require [jarl.parser :as parser]))

;
; PLAN
;

(gen-class
  :name "se.fylling.jarl.PlanImpl"
  :implements [se.fylling.jarl.Plan]
  :main false
  :prefix "-plan-"
  :constructors {[Object Object String] []}
  :state "state"
  :init "init"
  )

(defn -plan-init [info plan name]
  [[info] [info plan name]])

(defn -plan-eval [^se.fylling.jarl.PlanImpl this input]
  (let [[info plan name] (.state this)]
    (if (nil? plan)
      (throw (Exception. (format "plan '%s' doesn't exist" name)))
      (plan info input))))

(defn -plans-toString [^se.fylling.jarl.PlanImpl this]
  (let [[_ _ name] (.state this)]
    name))

;
; JARL
;

(gen-class
  :name "se.fylling.jarl.JarlImpl"
  :implements [se.fylling.jarl.Jarl]
  :prefix "-jarl-"
  :main false
  :state "state"
  :init "init"
  :constructors {[Object] []}
  )

(defn -jarl-init [info]
  [[info] info])

(defn plan-by-name [plans name]
  (loop [plans plans]
    (when-not (seq plans)
      (let [[current-name plan] (first plans)]
        (if (= current-name name)
          plan
          (recur (next plans)))))))

(defn -jarl-getPlan [^se.fylling.jarl.JarlImpl this name]
  (let [info (.state this)
        plans (get info :plans)
        plan (plan-by-name plans name)]
    (if (nil? plan)
      (throw (Exception. (format "plan '%s' doesn't exist" name)))
      (new se.fylling.jarl.PlanImpl info plan name))))

;
; JARL FACTORY
;

(gen-class
  :name "se.fylling.jarl.JarlFactory"
  :prefix "-jarl-factory-"
  :main false
  :methods [^{:static true} [fromIr [String] se.fylling.jarl.Jarl]]
  )

(defn -jarl-factory-fromIr [ir]
  (new se.fylling.jarl.JarlImpl (parser/parse ir)))

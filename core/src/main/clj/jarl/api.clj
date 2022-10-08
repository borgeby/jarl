(ns jarl.api
  (:require [jarl.parser :as parser]
            [jarl.eval :as eval]))

(gen-class
  :name "by.borge.jarl.internal.InternalPlanImpl"
  :implements [by.borge.jarl.internal.InternalPlan]
  :main false
  :prefix "-plan-"
  :constructors {[Object Object String] []}
  :state "state"
  :init "init"
  )

(defn -plan-init [info plan entry-point]
  [[info] [info plan entry-point]])

(defn -plan-eval [^by.borge.jarl.internal.InternalPlanImpl this input data]
  (let [[info plan entry-point] (.state this)]
    (if (nil? plan)
      (throw (Exception. (format "plan with entry-point '%s' doesn't exist" entry-point)))
      (plan info input data))))

(defn -plans-toString [^by.borge.jarl.internal.InternalPlanImpl this]
  (let [[_ _ entry-point] (.state this)]
    entry-point))

(gen-class
  :name "by.borge.jarl.internal.IrImpl"
  :implements [by.borge.jarl.internal.IntermediateRepresentation]
  :prefix "-jarl-ir-"
  :main false
  :state "state"
  :init "init"
  :constructors {[Object] []}
  )

(defn -jarl-ir-init [info]
  [[info] info])

(defn -jarl-ir-withStrictBuiltinErrors [^by.borge.jarl.internal.IrImpl this strict]
  (let [info (.state this)]
    (new by.borge.jarl.internal.IrImpl (assoc info :strict-builtin-errors strict))))

(defn -jarl-ir-getPlans [^by.borge.jarl.internal.IrImpl this]
  (let [info (.state this)
        plans (:plans info)
        plans (into (sorted-map) plans)
        plans (reduce-kv (fn [m name plan] (assoc m name (new by.borge.jarl.internal.InternalPlanImpl info plan name))) {} plans)]
    plans))

(defn -jarl-ir-getPlan [^by.borge.jarl.internal.IrImpl this entry-point]
  (let [info (.state this)
        plan (eval/find-plan info entry-point)]
    (new by.borge.jarl.internal.InternalPlanImpl info plan entry-point)))

(gen-class
  :name "by.borge.jarl.internal.Parser"
  :prefix "-jarl-parser-"
  :main false
  :methods [^{:static true} [parse [String] by.borge.jarl.internal.IntermediateRepresentation]]
  )

(defn -jarl-parser-parse [ir]
  (new by.borge.jarl.internal.IrImpl (parser/parse-json ir)))
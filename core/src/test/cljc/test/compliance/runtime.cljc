(ns test.compliance.runtime
  "Vars and functions made available to the compliance tests at runtime. Anything not required there should rather be
  included in the generator"
  (:require [clojure.string :as str]
            [jarl.builtins.registry :as registry]
            [jarl.exceptions :as errors])
  (:import #?(:clj  (clojure.lang ExceptionInfo))))

(def test-builtins
  {"opa.runtime" (fn [_] {})})

(defn compliance-builtin-resolver [builtin-name]
  (get test-builtins builtin-name (registry/get-builtin builtin-name)))

(defn- get-plan [plans name]
  (get (first (filter #(= name (get % 0)) plans)) 1))

(defn eval-entry-points-for-results [info entry-points data input]
  (let [eval-entry-point (fn [acc entry-point]
                           (let [plan (-> info :plans (get-plan entry-point))
                                 entry-point-result (get (first (plan info data input)) "result")]
                             (assoc acc entry-point entry-point-result)))]
    (reduce eval-entry-point {} entry-points)))

(defn eval-entry-points-for-errors [info entry-points data input]
  (let [try-error (fn [errors entry-point]
                     (let [plan (get-plan (get info :plans) entry-point)]
                       (try
                         (plan info data input)                 ; We don't care about the result set
                         errors
                         (catch ExceptionInfo e                 ; Blow up on non-jarl exceptions
                           (conj errors e)))))]
    (reduce try-error [] entry-points)))

(defn- match-one-of [provided-msg expected-msg]
  (when (and (str/includes? provided-msg "one of") (str/includes? expected-msg "one of"))
       (let [pr-set (into #{} (map str/trim (str/split (second (re-find #"one of \{(.*)\}" provided-msg)) #",")))
             ex-set (into #{} (map str/trim (str/split (second (re-find #"one of \{(.*)\}" expected-msg)) #",")))]
         (and (= pr-set ex-set)
              (= (second (re-find #"but got (.*)" provided-msg))
                 (second (re-find #"but got (.*)" expected-msg)))))))

(defn error-filter [want-error-code want-error]
  (fn [error]
    (and (= (errors/rego-type error) want-error-code)
         (or (nil? want-error) ; arguably something to fix in the OPA tests, but not all of them assert an error message
             (str/includes? (ex-message error) want-error)
             (match-one-of (ex-message error) want-error)))))

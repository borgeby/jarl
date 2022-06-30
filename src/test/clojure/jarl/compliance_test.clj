(ns jarl.compliance-test
  (:require [clojure.test :refer [is test-vars deftest]]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [test.utils :refer [add-test]]
            [jarl.builtins.registry :as registry]
            [jarl.parser :refer [parse]])
  (:import (java.nio.file FileSystems)
           (java.io File)
           (se.fylling.jarl JarlException)))

(defn json->test-cases [^File file]
  (get (json/read-str (slurp file)) "cases"))

(defn read-test-cases []
  (let [root (io/resource "compliance")
        is-json (.getPathMatcher
                  (FileSystems/getDefault)
                  "glob:*.{json}")]
    (->>
      root
      io/file
      file-seq
      (filter #(.isFile ^File %))
      (filter #(.matches is-json (.getFileName (.toPath ^File %))))
      (mapv #(json->test-cases %))
      flatten)))

(defn- get-plan [plans name]
  (get (first (filter #(= name (get % 0)) plans)) 1))

; Temporary workaround since we can't parse non-JSON terms
(defn parse-input-term [term]
  (condp = term
    "{\"foo\": {{1}}}" {"foo" #{#{1}}}
    (json/read-str term)))

(def test-builtins
  {"opa.runtime" (fn [_] {})})

(defn compliance-builtin-resolver [builtin-name]
  (get test-builtins builtin-name (registry/get-builtin builtin-name)))

(defn- eval-entry-points-for-results [info entry-points data input]
  (loop [entry-points entry-points
         result {}]
    (if (empty? entry-points)
      result
      (let [entry-point (first entry-points)
            plan (get-plan (get info :plans) entry-point)
            entry-point-result-set (plan info data input)
            entry-point-result (get (first entry-point-result-set) "result")
            result (assoc result entry-point entry-point-result)]
        (recur (next entry-points) result)))))

(defn- eval-entry-points-for-errors [info entry-points data input]
  (loop [entry-points entry-points
         errors []]
    (if (empty? entry-points)
      errors
      (let [entry-point (first entry-points)
            plan (get-plan (get info :plans) entry-point)
            errors (try
                     (plan info data input)                 ; We don't care about the result set
                     errors
                     (catch JarlException e                 ; Blow up on non-jarl exceptions
                       (conj errors e)))]
        (recur (next entry-points) errors)))))

(defn match-one-of [provided-msg expected-msg]
  (let [pr-set (into #{} (map str/trim (str/split (second (re-find #"one of \{(.*)\}" provided-msg)) #",")))
        ex-set (into #{} (map str/trim (str/split (second (re-find #"one of \{(.*)\}" expected-msg)) #",")))]
    (and (= pr-set ex-set)
         (= (second (re-find #"but got (.*)" provided-msg))
            (second (re-find #"but got (.*)" expected-msg))))))

(defn- do-test [{:strs           [data note]
                 entry-points    "entrypoints"
                 want-result     "want_plan_result"
                 want-error-code "want_error_code"
                 want-error      "want_error"
                 ir              "plan"
                 :as             test-case}]
  (println "Running test case:" note)
  (let [input (if (contains? test-case "input_term")
                (parse-input-term (get test-case "input_term"))
                (get test-case "input"))
        info (cond-> (parse ir compliance-builtin-resolver)
                     (true? (get test-case "strict_error")) (assoc :strict-builtin-errors true))]
    (if (and (nil? want-error-code) (nil? want-error))
      (let [result (eval-entry-points-for-results info entry-points data input)]
        (is (= result want-result)))
      (let [errors (eval-entry-points-for-errors info entry-points data input)
            error-filter (fn [^JarlException error]
                           (and
                             (= (.getType error) want-error-code)
                             (or (.contains (.getMessage error) want-error)
                                 (match-one-of (.getMessage error) want-error))))
            jarl-errors (filter error-filter errors)]
        ; There might be other errors generated than what is expected by the test case definition, but the test case
        ; doesn't know we're executing multiple entry-points, so we can't count unexpected JarlExceptions as violations
        (is (not-empty jarl-errors)
            (str "Expected error code:" want-error-code "; message: " want-error ", got" errors))))))

;
; Test generator
;

(defn- builtin-names [ir]
  (let [builtins (get (get ir "static") "builtin_funcs")]
    (map #(get % "name") builtins)))

(defn- ir-supported? [ir]
  (if (nil? ir)
    false
    (let [used-builtins (builtin-names ir)
          unsupported (filter #(not (contains? registry/builtins %)) used-builtins)]
      (if (empty? unsupported)
        true
        (do
          (println "Unsupported built-ins:" unsupported)
          false)))))

(defn- generate-tests
  "Generate tests and intern them in the namespace"
  []
  (let [test-to-run (get (System/getenv) "TEST")]
    (doseq [test-case (read-test-cases)]
      (let [{:strs [note plan]} test-case
            test-name (str/replace note #"[/\s]" "_")]
        (if (and (or (nil? test-to-run) (= note test-to-run))
                 (ir-supported? plan))
          (add-test test-name
                    'jarl.compliance-test
                    #(do-test test-case)
                    {:compliance true})
          (println "Ignoring" test-name))))))

; Trick `lein test` into consider this namespace before the tests have been generated
(deftest ^:compliance phony
  (is (= true true)))

(defn test-ns-hook
  "Run tests in a sorted order."
  []
  (generate-tests)
  (test-vars (->> (ns-interns 'jarl.compliance-test) vals (sort-by str))))

(comment
  (test-ns-hook))

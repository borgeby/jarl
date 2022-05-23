(ns jarl.compliance-test
  (:require [clojure.test :refer :all]
            [test.utils :refer :all]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [jarl.parser :refer [parse]])
  (:import (java.nio.file FileSystems)
           (java.io File)))

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
      (filter #(.isFile %))
      (filter #(.matches is-json (.getFileName (.toPath %))))
      (mapv #(json->test-cases %))
      flatten)))


(defn- get-plan [plans name]
  (get (first (filter #(= name (get % 0)) plans)) 1))

(defn- do-test [test-case]
  (let [entry-points (get test-case "entrypoints")
        want-results (get test-case "want_plan_result")
        input (get test-case "input")
        data (get test-case "data")
        plan-data (get test-case "plan")
        info (parse plan-data)]
    (doseq [entry-point entry-points]
      (let [want-result (get want-results entry-point)
            plan (get-plan (get info :plans) entry-point)
            result-set (plan info input)
            result (get (first result-set) "result")]
        (println "Expected:" want-result)
        (println)
        (println "Got:" result)
        (is (= result want-result))))))

;
; Test generator
;

(defn- builtin-names [ir]
  (let [builtins (get (get ir "static") "builtin_funcs")]
    (map #(get % "name") builtins)))

(defn- ir-supported? [ir]
  (let [used-builtins (builtin-names ir)
        unsupported (filter #(not (contains? jarl.builtins.registry/builtins %)) used-builtins)]
    (if (empty? unsupported)
      true
      (do
        (println "Unsupported built-ins:" unsupported)
        false))))

(do
  (let [test-cases (read-test-cases)]
    (doseq [test-case test-cases]
      (let [note (get test-case "note")
            sanitized-note (clojure.string/replace note #"[/\s]" "_")
            test-name sanitized-note
            ir (get test-case "plan")]
        (if (ir-supported? ir)
          (add-test test-name
                    'jarl.compliance-test
                    #(do-test test-case))
          (println "Ignoring" test-name))))))


(defn test-ns-hook
  "Run tests in a sorted order."
  []
  (test-vars (->> (ns-interns 'jarl.compliance-test) vals (sort-by str))))

;(clojure.test/run-tests 'jarl.compliance-test)
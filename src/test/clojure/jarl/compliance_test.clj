(ns jarl.compliance-test
  (:require [clojure.test :refer [is test-vars]]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [test.utils :refer [add-test]]
            [jarl.builtins.registry :as registry]
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
        ir (get test-case "plan")
        info (parse ir)]
    (doseq [entry-point entry-points]
      (let [want-result (get want-results entry-point)
            plan (get-plan (get info :plans) entry-point)
            want-error-code (get test-case "want_error_code")
            want-error (get test-case "want_error")]
        (is (not (nil? plan)))
        (if-not (nil? plan)
          (if (and (nil? want-error-code) (nil? want-error))
            (let [result-set (plan info data input)
                  result (get (first result-set) "result")]
              ;(println (str "Want: " want-result "\n\nGot: " result "\n"))
              (is (= result want-result)))
            (do
              (println (str "Want error: " want-error-code ": " want-error "\n"))
              (is (thrown-with-msg? Exception #"foobar" (plan info input)))))
          (println info))))))

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

(let [test-cases (read-test-cases)]
  (doseq [test-case test-cases]
    (let [note (get test-case "note")
          sanitized-note (str/replace note #"[/\s]" "_")
          test-name sanitized-note
          ir (get test-case "plan")]
      (if (ir-supported? ir)
        (add-test test-name
                  'jarl.compliance-test
                  #(do-test test-case))
        (println "Ignoring" test-name)))))


(defn test-ns-hook
  "Run tests in a sorted order."
  []
  (test-vars (->> (ns-interns 'jarl.compliance-test) vals (sort-by str))))

;(clojure.test/run-tests 'jarl.compliance-test)

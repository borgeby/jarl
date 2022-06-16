(ns jarl.compliance-test
  (:require [clojure.test :refer [is test-vars deftest]]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
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

(defn- do-test [{:strs           [data note]
                 entry-points    "entrypoints"
                 want-results    "want_plan_result"
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
    (doseq [entry-point entry-points]
      (let [want-result (get want-results entry-point)
            plan (get-plan (get info :plans) entry-point)]
        (is (not (nil? plan)))
        (when-not (nil? plan)
          (if (and (nil? want-error-code) (nil? want-error))
            (let [result-set (plan info data input)
                  result (get (first result-set) "result")]
              ;(println (str "Want: " want-result "\n\nGot: " result "\n"))
              (is (= result want-result)))
            (do
              (log/infof "Want error: %s: %s" want-error-code want-error)
              (try
                (let [result-set (plan info data input)]
                  (is (nil? result-set) (str "Exception must be thrown, got " result-set)))
                (catch JarlException e
                  (is (.contains (.getMessage e) want-error) (str "Got error message: " (ex-message e)))
                  (is (= (.getType e) want-error-code) (str "Got error code: " (.getType e))))
                (catch Throwable e
                  (do
                    (println "Unexpected error" e)
                    (is false "JarlException must be thrown")))))))))))

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
  (doseq [test-case (read-test-cases)]
    (let [{:strs [note plan]} test-case
          test-name (str/replace note #"[/\s]" "_")]
      (if (ir-supported? plan)
        (add-test test-name
                  'jarl.compliance-test
                  #(do-test test-case)
                  {:compliance true})
        (println "Ignoring" test-name)))))

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

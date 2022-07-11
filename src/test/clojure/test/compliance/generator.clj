(ns test.compliance.generator
  "Generates compliance test files for both Clojure and ClojureScript"
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [jarl.builtins.registry :as registry]
            [zprint.core :as zp])
  (:import (java.nio.file FileSystems)
           (java.io File)))

(def ignored-tests
  {:clj
   #{}
   :cljs
   #{"aggregates/count with invalid utf-8 chars (0xFFFD)" ; does not seem terribly important, ignoring for now
     "arithmetic/big_int"}}) ; bigint not supported in ClojureScript

(defn ignored? [target note]
  (contains? (get ignored-tests target) note))

(defn json->test-cases [^File file]
  (-> (slurp file)
      (json/read-str)
      (get "cases")))

(defn read-test-cases []
  (let [is-json (-> (FileSystems/getDefault) (.getPathMatcher "glob:*.{json}"))]
    (->> (io/resource "compliance")
         io/file
         file-seq
         (filter #(.isFile ^File %))
         (filter #(.matches is-json (.getFileName (.toPath ^File %))))
         (mapv #(json->test-cases %))
         flatten)))

(defn note->test-name [note]
  (-> note
      (str/replace #"[/\s]" "-")
      (str/replace #"[\(\)\[\]\{\}\"\.,:]" "")
      (symbol)
      (vary-meta assoc :tag :compliance)))

; Deftest form templates - note the heavy use of ~' which unquotes a symbol using its name _only_, i.e.
; `deftest` rather than `clojure.test/deftest`, which would be the default behavior of syntax-quoted forms.
; While this make total sense for macros, the purpose here is readable code!

(defn test-case-want-result [note data input entry-points want-result]
  (let [note (str/replace note #"\"" "")
        test-name (note->test-name note)]
    `(~'deftest ~test-name
       (~'let [~'plan (~'get ~'test-plans ~note)
               ~'info (~'parser/parse ~'plan ~'compliance-builtin-resolver)
               ~'data ~data
               ~'input ~input
               ~'result (~'eval-entry-points-for-results ~'info ~entry-points ~'data ~'input)]
         (~'is (~'= ~'result ~want-result))))))

(defn test-case-want-error [note data input entry-points want-error-code want-error strict-error]
  (let [note (str/replace note #"\"" "")
        test-name (note->test-name note)]
    `(~'deftest ~test-name
       (~'let [~'strict-error ~strict-error
               ~'plan (~'get ~'test-plans ~note)
               ~'info (~'cond-> (~'parser/parse ~'plan ~'compliance-builtin-resolver)
                                (~'true? ~'strict-error) (~'assoc :strict-builtin-errors true))
               ~'data ~data
               ~'input ~input
               ~'result (~'eval-entry-points-for-errors ~'info ~entry-points ~'data ~'input)
               ~'jarl-errors (~'filter (~'error-filter ~want-error-code ~want-error) ~'result)]
          ; There might be other errors generated than what is expected by the test case definition, but the test case
          ; doesn't know we're executing multiple entry-points, so we can't count unexpected JarlExceptions as violations
          (~'is (~'not-empty ~'jarl-errors)
            (~'str ~(str "Expected error code:" want-error-code "; message: " want-error ", got: ") ~'result))))))

(defn create-test
  "Creates a deftest form from an OPA compliance test plan"
  [{:strs [data note]
    entry-points    "entrypoints"
    want-result     "want_plan_result"
    want-error-code "want_error_code"
    want-error      "want_error"
    strict-error    "strict_error"
    :as             test-case}]
  (println "Creating test case:" note)
  (let [parse-input-term (fn [term] ; Temporary workaround since we can't parse non-JSON terms
                           (condp = term
                             "{\"foo\": {{1}}}" {"foo" #{#{1}}}
                             (json/read-str term)))
        input (if (contains? test-case "input_term") (parse-input-term (test-case "input_term")) (test-case "input"))]
    (if (and (nil? want-error-code) (nil? want-error))
      (test-case-want-result note data input entry-points want-result)
      (test-case-want-error note data input entry-points want-error-code want-error strict-error))))

(defn- builtin-names [ir]
  (let [builtins (get (get ir "static") "builtin_funcs")]
    (map #(get % "name") builtins)))

(defn read-conditional [form target]
  (second (drop-while #(not= target %) (:form form))))

(defn read-string-as
  "Read form as given target (e.g. :clj or :cljs) parsing any reader conditionals found for target"
  [s target]
  (let [form (read-string {:read-cond :preserve} s)]
    (if (reader-conditional? form)
      (read-conditional form target)
      (map #(if (reader-conditional? %)
              (read-conditional % target)
              (identity %))
              form))))

(defn read-cljs-registry []
  (let [reg-path (str (System/getProperty "user.dir") "/src/main/cljc/jarl/builtins/registry.cljc")
        forms (str/split (slurp reg-path) #"\n\n") ; TODO Fix! we should not rely on whitespace here but find a way to read forms
        builtins-form (get forms 1)
        def-builtins (read-string-as builtins-form :cljs)
        builtins-map (nth def-builtins 2)]
    (into {} (for [[k, _] builtins-map] [k, (constantly nil)]))))

(defn target-registry-builtins
  [target]
  (let [cljs-registry-reader (memoize read-cljs-registry)]
    (condp = target
      :clj  registry/builtins
      :cljs (cljs-registry-reader)
      (println "unknown target" target))))

(defn- ir-supported? [ir target]
  (if (nil? ir)
    false
    (let [used-builtins (builtin-names ir)
          unsupported (remove #(contains? (target-registry-builtins target) %) used-builtins)]
      (if (empty? unsupported)
        true
        (do
          (println "Unsupported built-ins:" unsupported)
          false)))))

(defn inc-note
  "Take note and return a new one with a dash and an incremented number appended if already exists in plans, else note..
   i.e. if 'my-test' exists in plans, return 'my-test-2', if 'my-test-2' exists, return 'my-test-3', and so on"
  [note plans]
  (if (some? (get plans note))
    (let [with-num (re-find #".*-(\d)" note)
          new-note (cond (vector? with-num) (str/replace note #"-(\d)" (str "-" (inc (parse-long (second with-num)))))
                         :else (str note "-2"))]
      (inc-note new-note plans))
    note))

(defn- generate-tests
  "Generate tests for provided target (:clj or :cljs)"
  [target]
  (loop [test-cases (read-test-cases)
         result {:plans {} :tests []}]
    (let [test-case (first test-cases)]
      (if (nil? test-case)
        result
        (let [{:strs [note plan]} test-case
              note (inc-note note (:plans result))
              test-case (assoc test-case "note" note)]
          (if (and (ir-supported? plan target) (not (ignored? target note)))
            (recur (rest test-cases) (-> result
                                         (assoc-in [:plans note] plan)
                                         (assoc :tests (conj (:tests result) (create-test test-case)))))
            (do
              (println "Ignoring" note)
              (recur (rest test-cases) result))))))))

(defn fmt-plans [plans]
  (str/triml (str/join "\n" (for [[name plan] plans] (str "   \"" (str/replace name #"\"" "") "\" " plan)))))

(def compliance-tests-ns-decl
  {:clj '(ns test.compliance.generated.tests
           (:require [clojure.test :refer [deftest is]]
                     [jarl.parser :as parser]
                     [test.compliance.runtime :refer [compliance-builtin-resolver
                                                      eval-entry-points-for-results
                                                      eval-entry-points-for-errors
                                                      error-filter]]
                     [test.compliance.generated.plans :refer [test-plans]]))
   :cljs '(ns test.compliance.generated.tests
            (:require [cljs.test :refer [deftest is]]
                      [jarl.parser :as parser]
                      [test.compliance.runtime :refer [compliance-builtin-resolver
                                                       eval-entry-points-for-results
                                                       eval-entry-points-for-errors
                                                       error-filter]]
                      [test.compliance.generated.plans :refer [test-plans]]))})

(defn compliance-tests-ns-str [tests target]
  (let [ns-decl (target compliance-tests-ns-decl)]
    (str (pprint/write ns-decl :dispatch clojure.pprint/code-dispatch :stream nil :pretty true)
         "\n\n"
         (binding [*print-meta* true]
           (zp/zprint-file-str (str/join "\n" (map prn-str tests)) "" {:width 120})))))

(defn generate-for-target [target]
  (let [{:keys [plans tests]} (generate-tests target)
        target-name (get {:clj "clojure" :cljs "clojurescript"} target)
        dir (str "src/test/" target-name "/test/compliance/generated/")
        file (io/file dir)]
    (if (or (.exists file) (.mkdirs file))
      (do
        (spit (str dir "plans." (name target)) (str "(ns test.compliance.generated.plans)"
                                                    "\n\n(def test-plans \n  {"
                                                    (fmt-plans plans) "})"))
        (spit (str dir "tests." (name target)) (compliance-tests-ns-str tests target)))
      (println "ERROR: failed to create dir:" dir))))

(defn -main
  ([]
   (println "generating compliance tests for all targets")
   (time
     (do (generate-for-target :clj)
         (generate-for-target :cljs))))
  ([target]
   (let [target (keyword (str/replace target #":" ""))]
     (println "generating compliance tests for target" target)
     (if (contains? #{:clj :cljs} target)
       (time
         (generate-for-target target))
       (println "unsupported target" target)))))

(ns test.benchmark
  (:require [clojure.java.shell :as shell]
            [clojure.string :as string]
            [criterium.core :as criterium]
            [test.csv :as csv]
            [jarl.io :as jio]
            [jarl.encoding.json :as json]
            [jarl.parser :as parser]
            [test.git :as git]))

(defn mean [bench-result]
  (* (first (:sample-mean bench-result)) 1e6))              ; microseconds

(defn ir->result-map [f ir]
  (loop [plans (get ir :plans)
         result-map {}]
    (if (empty? plans)
      result-map
      (let [[entry-point plan] (first plans)
            _ (println "Measuring performance of plan:" entry-point)
            res (f entry-point plan)]
        (recur (next plans) (assoc result-map entry-point res))))))

(defn benchmark-ir [ir]
  (let [f (fn [_ plan]
            (let [bench-fn (fn [] (plan ir {} {}))]
              (mean (criterium/with-progress-reporting
                      (criterium/benchmark* bench-fn {})))))]
    (ir->result-map f ir)))

(defn benchmark-jarl [ir]
  (benchmark-ir ir))

(defn get-opa []
  (get (System/getenv) "OPA" "opa"))

(defn run-opa [policy query]
  (let [result (shell/sh (get-opa) "eval" "--metrics" "-d" policy query)
        err (:err result)]
    (when (not (string/blank? err))
      (throw (ex-info "Failed to run OPA eval" {:err err})))
    (let [output (:out result)]
      (json/read-str output))))

(defn benchmark-opa [policy ir]
  (let [f (fn [entry-point _]
            (let [query (str "data." (string/replace entry-point #"/" "."))
                  run (fn [] (let [result (run-opa policy query)
                                   metrics (get result "metrics")
                                   eval-ns (get metrics "timer_rego_query_eval_ns")
                                   eval-micros (* eval-ns 0.001)]
                               eval-micros))
                  iterations 1000]
              (->> (repeatedly iterations run)
                   (reduce +)
                   (* (/ 1 iterations)))))]
    (ir->result-map f ir)))

(defn benchmark-baseline []
  (println "Measuring performance of baseline")
  (let [bench-fn (fn [] (reduce + (range 100)))
        bench-result (criterium/with-progress-reporting
                       (criterium/benchmark* bench-fn {}))]
    {"baseline" (mean bench-result)}))

(defn jarl-version []
  (let [revision (git/get-revision)
        tag (git/get-tag)]
    (if-not (nil? tag)
      (str tag " (" revision ")")
      revision)))

(defn opa-version []
  (let [result (shell/sh (get-opa) "version")
        err (:err result)]
    (when (not (string/blank? err))
      (throw (ex-info "Failed to get OPA version" {:err err})))
    (let [output (:out result)
          matcher (re-matcher #"Version: (.+)\n" output)]
      (nth (re-find matcher) 1))))

(defn benchmark [f ir-file csv-file version]
  (let [csv (csv/read-file csv-file)
        ir (parser/parse-json (jio/read-file ir-file))
        data {"label" version}
        data (conj data (benchmark-baseline))
        data (conj data (f ir))
        csv (csv/add csv data)]
    (csv/write-file csv csv-file ["label" "baseline"])))

(defn -main
  ([category]
   (let [label (if (= category "opa")
                 (opa-version)
                 (jarl-version))]
     (-main category label)))
  ([category label]
   (let [ir-file "src/test/resources/rego/perf/plan.json"
         policy-file "src/test/resources/rego/perf/policy.rego"]
     (if (= category "opa")
       (let [f (fn [ir]
                 (benchmark-opa
                   policy-file
                   ir))]
         (benchmark f ir-file "perf/opa-perf.csv" label))
       (benchmark benchmark-jarl ir-file "perf/jarl-perf.csv" label)))))

(ns jarl.core
  (:require [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [jarl.bundle :as bundle]
            [jarl.logging :as logging]
            [jarl.encoding.json :as json]
            [jarl.io :as jio]
            [jarl.parser :as parser]
            [jarl.eval :as evaluator]
            [jarl.formatting :as fmt]
            #?(:cljs [cljs.nodejs :as nodejs]))
  #?(:clj (:import (clojure.lang ExceptionInfo)))
  #?(:clj (:gen-class)))

#?(:cljs (enable-console-print!))
#?(:cljs (nodejs/enable-util-print!))

(def cli-options
  [["-i" "--input string" "set input file path"
    :parse-fn #(json/read-str (jio/read-file %))]
   ["-d" "--data string" "set data file path"
    :parse-fn #(json/read-str (jio/read-file %))]
   ["-f" "--format string" "set output format (default json)"]
   [nil "--strict-builtin-errors" "treat built-in function errors as fatal"]
   ["-h" "--help"]
   ["-v" "--verbose" "Print debug information to stdout"]])

(defn usage [options-summary]
  (->> ["Jarl is a tool for evaluating Rego policies compiled into the OPA IR format"
        ""
        "Usage: jarl <plan or bundle file> [<entrypoint>] [options]"
        ""
        "Options:"
        options-summary]
       (str/join \newline)))

(defn error-msg [errors]
  (->> (flatten ["Failure:"
                 ""
                 errors
                 ""
                 "Use -h option for usage"])
       (str/join \newline)))

(defn parse-args
  [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      errors
      {:exit-message (error-msg errors)}
      (:help options)
      {:exit-message (usage summary) :ok? true}
      (not-empty arguments)
      (let [[path entrypoint] arguments]
        (if (jio/file? path)
          {:path path :entrypoint entrypoint :options options}
          {:exit-message (error-msg [(fmt/sprintf "\"%s\" is not a file" path)])}))
      :else
      {:exit-message (usage summary)})))

(defn abort [msg]
  (throw (ex-info msg {})))

(defn- eval-plan [info entrypoint input data]
  (if (some? entrypoint)
    (evaluator/eval-plan info entrypoint input data)
    (let [plans (:plans info)]
      (case (count plans)
        0 (abort (error-msg "plan contains no entrypoints/plans"))
        1 (let [entrypoint (first (first plans))]
            (evaluator/eval-plan info entrypoint input data))
        (let [msg (fmt/sprintf "plan contains more than one entrypoint; please specify which one of %s to use"
                               (pr-str (map first plans)))]
          (abort (error-msg msg)))))))

(defn -main [& args]
  (try
    (let [{:keys [path entrypoint options exit-message ok?]} (parse-args args)]
      (if exit-message
        (if ok?
          (println exit-message)
          (abort exit-message))
        (let [ir (if (str/ends-with? path ".tar.gz") (bundle/extract-plan path) (jio/read-file path))
              {:keys [data input verbose]} options]
          (if verbose
            (logging/set-log-level :debug)
            (logging/set-log-level :warn))
          (if (nil? ir)
            (abort (error-msg "no valid plan file found"))
            (let [strict (:strict-builtin-errors options)
                  info (cond-> (parser/parse-json ir) (some? strict) (assoc :strict-builtin-errors strict))
                  result (eval-plan info entrypoint input data)]
              (case (:format options)
                "raw" (println (json/write-str (get-in result [0 "result"])))
                (println (json/write-str result))))))))
    (catch ExceptionInfo e
      (println (ex-message e))
      #?(:clj (System/exit 1)))))

#?(:cljs (set! *main-cli-fn* -main))

(ns jarl.core
  (:require [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [jarl.logging :as logging]
            [jarl.encoding.json :as json]
            [jarl.io :as jio]
            [jarl.parser :as parser]
            [jarl.eval :as evaluator]
            [jarl.formatting :as fmt]
            #?(:cljs [cljs.nodejs :as nodejs]))
  #?(:clj (:gen-class)))

#?(:cljs (enable-console-print!))
#?(:cljs (nodejs/enable-util-print!))

(def cli-options
  [["-i" "--input string" "set input file path"
    :parse-fn #(json/read-str (slurp %))]
   ["-d" "--data string" "set data file path"
    :parse-fn #(json/read-str (slurp %))]
   [nil "--strict-builtin-errors" "treat built-in function errors as fatal"]
   ["-h" "--help"]
   ["-v" "--verbose" "Print debug information to stdout"]])

(defn usage [options-summary]
  (->> ["Jarl is a tool for evaluating Rego policies compiled into the OPA IR format"
        ""
        "Usage: jarl <IR file> [<entrypoint>] [options]"
        ""
        "Options:"
        options-summary]
       (string/join \newline)))

(defn error-msg [errors]
  (->> (flatten ["Failure:"
                 ""
                 errors
                 ""
                 "Use -h option for usage"])
       (string/join \newline)))

(defn parse-args
  [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      errors
      {:exit-message (error-msg errors)}
      (:help options)
      {:exit-message (usage summary) :ok? true}
      (not-empty arguments)
      (let [ir (first arguments)
            plan (second arguments)]
        (if (jio/file? ir)
          {:ir ir :plan plan :options options}
          {:exit-message (error-msg [(fmt/sprintf "IR \"%s\" is not a file" ir)])}))
      :else
      {:exit-message (usage summary)})))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [ir plan options exit-message ok?]} (parse-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (let [ir (slurp ir)
            {:keys [data input verbose]} options]
        (if verbose
          (logging/set-log-level :debug)
          (logging/set-log-level :warn))
        (let [info (parser/parse-json ir)
              info (assoc info :strict-builtin-errors (contains? options :strict-builtin-errors))
              result (if-not (nil? plan)
                       (evaluator/eval-plan info plan data input)
                       (let [plans (:plans info)]
                         (case (count plans)
                           0 (exit 1 (error-msg "IR contains no plans"))
                           1 (let [plan (first plans)]
                               (plan info data input))
                           (exit 1 (error-msg (format "IR contains more than one plan; please specify entrypoint %s"
                                                      (pr-str (map first plans))))))))]
          (exit 0 (json/write-str result)))))))

#?(:cljs (set! *main-cli-fn* -main))

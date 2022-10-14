(ns jarl.logging
  (:require [clojure.string :as str]
            [taoensso.encore :as enc]
            [taoensso.timbre :as timbre]))

(defn- format-ns
  "jarl.builtins.array -> j.b.array"
  [?ns-str]
  (when-let [ns ?ns-str]
    (let [parts (str/split ns #"\.")]
      (str/join "." (conj (mapv first (butlast parts)) (last parts))))))

(defn- output-fn
  "Same as timbre/default-output-fn but without hostname, and with format-ns applied to namespace"
  [{:keys [level ?err msg_ ?ns-str ?file timestamp_ ?line]}]
  (str
    (when-let [ts (force timestamp_)] (str ts " "))
    (str/upper-case (name level)) " "
    "[" (or (format-ns ?ns-str) ?file "?") ":" (or ?line "?") "] - "
    (force msg_)
    (when-let [err ?err]
      (str enc/system-newline (timbre/stacktrace err)))))

(defn set-log-level [level]
  (timbre/merge-config! {:output-fn output-fn :min-level level}))



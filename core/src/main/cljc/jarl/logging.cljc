(ns jarl.logging
  (:require [clojure.string :as str]
            #?(:clj  [taoensso.encore :as enc]
               :cljs [taoensso.encore :as enc])
            #?(:clj  [taoensso.timbre :as timbre]
               :cljs [taoensso.timbre :as timbre])))

(defn trace [& args]
  #?(:cljr    (apply println args)
     :default (timbre/log! :trace :p args)))

(defn tracef [& args]
  #?(:cljr    (apply println args)
     :default (timbre/log! :trace :p args)))

(defn debug [& args]
  #?(:cljr    (apply println args)
     :default (timbre/log! :debug :p args)))

(defn debugf [& args]
  #?(:cljr    (apply println args)
     :default (timbre/log! :debug :p args)))

(defn info [& args]
  #?(:cljr    (apply println args)
     :default (timbre/log! :info :p args)))

(defn infof [& args]
  #?(:cljr    (apply println args)
     :default (timbre/log! :info :p args)))

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
    #?(:cljr    (println "not implemented")
       :default (when-let [err ?err]
                  (str enc/system-newline (timbre/stacktrace err))))))

(defn set-log-level [level]
  #?(:cljr    (println "not implemented")
     :default (timbre/merge-config! {:output-fn output-fn :min-level level})))

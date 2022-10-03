(ns jarl.config.reader
  (:require [jarl.encoding.json :as json]
            [jarl.encoding.yaml :as yaml]
            [jarl.runtime.environment :as environment]
            [camel-snake-kebab.core :as csk]
            [clojure.string :as str]))

(defn- env->resolved [value]
  (let [env-var (-> (first value) (str/replace #"\$\{" "") (str/replace #"\}" ""))]
    (or (environment/env-var env-var) "")))

#?(:clj
   (defn with-resolved-env-vars [_ value]
     (cond-> value (string? value) (str/replace #"(\$\{[^}]+\})" env->resolved))))

#?(:cljs
   (defn with-resolved-env-vars [value]
     (cond-> value (string? value) (str/replace #"(\$\{[^}]+\})" env->resolved))))

(defn json->config [s]
  (json/read-str s {:key-fn csk/->kebab-case-keyword :value-fn with-resolved-env-vars}))

(defn yaml->config [s]
  (-> s yaml/read-str json/write-str json->config))

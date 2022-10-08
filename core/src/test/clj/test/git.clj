(ns test.git
  (:require [clojure.java.shell :as shell]
            [clojure.string :as string]))

(defn get-revision []
  (let [result (shell/sh "git" "rev-parse" "--short=10" "HEAD")
        err (:err result)]
    (when (not (string/blank? err))
      (throw (ex-info "Failed to get OPA version" {:err err})))
    (string/trim-newline (:out result))))

(defn get-tag []
  (let [result (shell/sh "git" "describe" "--tags")
        err (:err result)]
    (if (string/blank? err)
      (string/trim-newline (:out result))
      nil)))
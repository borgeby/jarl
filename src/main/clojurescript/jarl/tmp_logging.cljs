#_:clj-kondo/ignore
(ns jarl.tmp-logging
  (:require [goog.string :as gstring]
            [goog.string.format]))

; TODO: Temporary workaround until we have a logging library
;       capable of serving both Clojure and ClojureScript.

(def ^:dynamic *logging-enabled* false)

(defn debug [msg]
  (when *logging-enabled*
    (println msg)))

(defn trace [msg]
  (when *logging-enabled*
    (println msg)))

(defn info [msg]
  (when *logging-enabled*
    (println msg)))

(defn tracef [msg & args]
  (when *logging-enabled*
    (apply println msg args)))
    ;(apply gstring/format msg args)))

(defn debugf [msg & args]
  (when *logging-enabled*
    (apply println msg args)))
    ;(apply gstring/format msg args)))

(defn infof [msg & args]
  (when *logging-enabled*
    (apply println msg args)))
    ;(apply gstring/format msg args)))

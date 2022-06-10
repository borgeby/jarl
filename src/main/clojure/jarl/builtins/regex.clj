(ns jarl.builtins.regex
  (:require [jarl.exceptions :as errors]
            [jarl.builtins.utils :refer [check-args]]
            [clojure.string :as str])
  (:import [com.google.re2j Pattern]
           (se.fylling.jarl BuiltinException)))

(defn builtin-regex-match
  "Implementation of regex.match built-in"
  {:builtin "regex.match" :args-types ["string", "string"]}
  [pattern ^String value]
  (try
    (-> (Pattern/compile pattern)
        (.matcher value)
        (.find))
    (catch Exception e
      (throw (errors/builtin-ex (str "eval_builtin_error: regex.match: " (.getMessage e)))))))

; Deprecated in OPA - only here for test conformance
(defn builtin-re-match [pattern ^String value]
  (try
    (builtin-regex-match pattern value)
    (catch BuiltinException e (str/replace (.getMessage e) #"regex\.match" "re_match"))))

(defn builtin-regex-is-valid
  "Implementation of regex.is_valid built-in"
  {:builtin "regex.is_valid" :args-types ["string"]}
  [pattern]
  (try (and (Pattern/compile pattern) true)
       (catch Exception _ false)))

(defn builtin-regex-split
  "Implementation of regex.split built-in"
  {:builtin "regex.split" :args-types ["string" "string"]}
  [pattern ^String value]
  (check-args (meta #'builtin-regex-split) pattern value)
  (-> (Pattern/compile pattern)
      (.split value)
      (vec)))

(defn builtin-regex-find-n
  "Implementation of regex.find_n built-in"
  {:builtin "regex.find_n" :args-types ["string" "string" "number"]}
  [pattern ^String value number]
  (let [matcher (.matcher (Pattern/compile pattern) value)]
    (loop [i 0
           matches []
           match (.find matcher)]
      (if (or (false? match) (and (not= number -1) (>= i number)))
        matches
        (recur (inc i) (conj matches (.group matcher 0)) (.find matcher))))))

; Annoyingly, this method is available in the Google package, but not made public :/
; https://github.com/google/re2j/blob/master/java/com/google/re2j/RE2.java#L883

#_:clj-kondo/ignore
(defn builtin-regex-find-all-string-submatch-n
  "Implementation of regex.find_all_string_submatch_n built-in"
  {:builtin "regex.find_all_string_submatch_n" :args-types ["string" "string" "number"]}
  [pattern ^String value number]
  (throw (errors/builtin-ex "not implemented %s %s %s" pattern value number)))

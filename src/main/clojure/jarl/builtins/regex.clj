(ns jarl.builtins.regex
  (:require [jarl.exceptions :as errors]
            [clojure.string :as str])
  (:import [com.google.re2j Pattern]
           (se.fylling.jarl BuiltinException)))

(defn builtin-regex-match
  [{[pattern ^String value] :args}]
  (try
    (-> (Pattern/compile pattern)
        (.matcher value)
        (.find))
    (catch Exception e
      (throw (errors/builtin-ex (str "eval_builtin_error: regex.match: " (.getMessage e)))))))

; Deprecated in OPA - only here for test conformance
(defn builtin-re-match [{[pattern ^String value] :args}]
  (try
    (builtin-regex-match {:args [pattern value]})
    (catch BuiltinException e
      (throw (errors/builtin-ex (str/replace (.getMessage e) #"regex\.match" "re_match"))))))

(defn builtin-regex-is-valid
  [{[pattern] :args}]
  (not (errors/throws? #(Pattern/compile pattern))))

(defn builtin-regex-split
  [{[pattern ^String value] :args}]
  (-> (Pattern/compile pattern)
      (.split value -1)
      (vec)))

(defn builtin-regex-find-n
  [{[pattern ^String value number] :args}]
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
  [{[pattern ^String value number] :args}]
  (throw (errors/builtin-ex "not implemented %s %s %s" pattern value number)))

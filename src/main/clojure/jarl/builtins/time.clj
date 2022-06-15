(ns jarl.builtins.time
  (:require [jarl.builtins.utils :refer [check-args]]
            [jarl.exceptions :as errors]
            [jarl.utils :refer [instant-to-ns]])
  (:import (java.time Instant ZoneId LocalDateTime ZoneOffset ZonedDateTime)
           (java.time.format TextStyle DateTimeFormatter)
           (java.util Locale)))

(defn parse-iso-datetime [s]
  (-> s
      (LocalDateTime/parse DateTimeFormatter/ISO_LOCAL_DATE_TIME)
      (.toInstant ZoneOffset/UTC)))

(defn parse-iso-zoned-datetime [s]
  (-> s
      (ZonedDateTime/parse DateTimeFormatter/ISO_ZONED_DATE_TIME)
      (.toInstant)))

(defn- instant-from-ns ^Instant [ns]
  (Instant/ofEpochSecond 0 ns))

(defn builtin-time-now-ns
  "Implementation of time.now_ns built-in"
  {:builtin "time.now_ns" :args-types []}
  [{bctx :builtin-context}]
  (get bctx :time-now-ns))

(defn builtin-time-weekday
  "Implementation of time.weekday built-in"
  {:builtin "time.weekday" :args-types [#{"number", "array"}]}
  [{[x] :args}]
  (check-args (meta #'builtin-time-weekday) x)
  (let [v (if (vector? x) x [x])]
      (-> (instant-from-ns (errors/try-or-throw #(long (first v)) (errors/builtin-ex "time.weekday: timestamp too big")))
          (.atZone (ZoneId/of (or (second v) "UTC")))
          (.getDayOfWeek)
          (.getDisplayName TextStyle/FULL Locale/ENGLISH))))

(defn builtin-time-parse-rfc3339-ns
  "Implementation of time.parse_rfc3339_ns built-in"
  {:builtin "time.parse_rfc3339_ns" :args-types ["string"]}
  [{[s] :args}]
  (check-args (meta #'builtin-time-parse-rfc3339-ns) s)
  (let [nanos (instant-to-ns (parse-iso-zoned-datetime s))]
    (errors/try-or-throw #(long nanos) (errors/builtin-ex "time.parse_rfc3339_ns: time outside of valid range"))))

(ns jarl.builtins.time
  (:require [jarl.builtins.utils :refer [check-args]]
            [jarl.exceptions :as errors]
            [jarl.utils :refer [instant-to-ns ns-to-instant]]
            [clojure.string :as str])
  (:import (java.time ZoneId LocalDateTime ZoneOffset ZonedDateTime Duration)
           (java.time.format TextStyle DateTimeFormatter)
           (java.util Locale)
           (java.time.temporal ChronoUnit)))

; Courtesy of https://github.com/newm4n/go-dfe
(def go->java-map
  {; Go          Java
   "January"   "MMMM"
   "Jan"       "MMM"
   "1"         "M"
   "01"        "MM"
   "Monday"    "EEEE"
   "Mon"       "EEE"
   "2"         "d"
   "_2"        "_d"
   "02"        "dd"
   "15"        "HH"
   "3"         "K"
   "03"        "KK"
   "4"         "m"
   "04"        "mm"
   "5"         "s"
   "05"        "ss"
   "2006"      "yyyy"
   "06"        "yy"
   "PM"        "aa"
   "pm"        "aa"
   "MST"       "Z"
   "Z0700"     "'Z'XX"
   "Z070000"   "'Z'XX"
   "Z07"       "'Z'X"
   "Z07:00"    "'Z'XXX"
   "Z07:00:00" "'Z'XXX"
   "-0700"     "XX"
   "-070000"   "'Z'XX"
   "-07"       "X"
   "-07:00"    "XXX"
   "-07:00:00" "XXX"
   "999999999" "SSS"})

(defn- key-length-sorter
  "Sort by key length (with the longest first) but treat equal key length as different in order to not discard them"
  [x y]
  (let [cmp (compare (count y) (count x))]
    (if (zero? cmp) -1 cmp)))

(def go->java-sorted-map (into (sorted-map-by key-length-sorter) go->java-map))

(defn- go->java-formatter ^String [format]
  (reduce (fn [acc [go java]] (str/replace acc go java)) format go->java-sorted-map))

(defn parse-iso-datetime
  ([s]
   (parse-iso-datetime s DateTimeFormatter/ISO_LOCAL_DATE_TIME))
  ([s f]
   (-> s (LocalDateTime/parse f) (.toInstant ZoneOffset/UTC))))

(defn parse-iso-zoned-datetime
  ([s]
   (parse-iso-zoned-datetime s DateTimeFormatter/ISO_ZONED_DATE_TIME))
  ([s f]
   (-> s (ZonedDateTime/parse f) (.toInstant))))

(defn parse-formatted-datetime [s f]
  (if (= f "01/02 03:04:05PM '06 -0700")
    ; special case for what Go docs refer to as "reference time": 01/02 03:04:05PM '06 -0700
    ; https://pkg.go.dev/time#pkg-constants
    (parse-iso-zoned-datetime (str/replace s #"'" "") (DateTimeFormatter/ofPattern "MM/dd hh:mm:ssa yy XX"))
    ; everything else
    (let [pattern (go->java-formatter f)]
      (if (re-find #"[ZXx]" pattern) ; time zone in pattern
        (errors/try-or
          (parse-iso-zoned-datetime s (DateTimeFormatter/ofPattern pattern))
          (parse-iso-zoned-datetime s))
        (parse-iso-datetime s (DateTimeFormatter/ofPattern pattern))))))

(def unit->temporal
  {"h"  ChronoUnit/HOURS
   "m"  ChronoUnit/MINUTES
   "s"  ChronoUnit/SECONDS
   "ms" ChronoUnit/MILLIS
   "us" ChronoUnit/MICROS
   "µs" ChronoUnit/MICROS
   "ns" ChronoUnit/NANOS})

(defn d [^long time ^ChronoUnit unit]
  (Duration/of time unit))

(defn d+
  ([^Duration d1 ^Duration d2]
    (.plus d1 d2))
  ([^Duration d1 ^Duration d2 & more]
   (reduce d+ (d+ d1 d2) more)))

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
      (-> (ns-to-instant (errors/try-or-throw #(long (first v)) (errors/builtin-ex "time.weekday: timestamp too big")))
          (.atZone (ZoneId/of (or (second v) "UTC")))
          (.getDayOfWeek)
          (.getDisplayName TextStyle/FULL Locale/ENGLISH))))

(defn unknown-duration-ex [unit time]
  (errors/builtin-ex "time.parse_duration_ns: time: unknown unit \"%s\" in duration \"%s\""
                     unit (str time unit)))

(defn to-duration [[_ time unit]]
  (d (Long/parseLong time) (errors/get-or-throw unit->temporal unit (unknown-duration-ex unit time))))

(defn builtin-time-parse-duration-ns
  "Implementation of time.parse_duration_ns built-in"
  {:builtin "time.parse_duration_ns" :args-types ["string"]}
  [{[s] :args}]
  (check-args (meta #'builtin-time-parse-duration-ns) s)
  (if-let [time-unit-pairs (re-seq #"(\d+)(\p{L}+)" s)] ; returns a seq of [full match, time, unit]
    (.toNanos ^Duration (reduce d+ (map to-duration time-unit-pairs)))
    (if (errors/throws? #(Long/parseLong s))
      (throw (errors/builtin-ex "time.parse_duration_ns: time: invalid duration \"%s\"" s))
      (throw (errors/builtin-ex "time.parse_duration_ns: time: missing unit in duration \"%s\"" s)))))

(defn builtin-time-parse-ns
  "Implementation of time.parse_ns built-in"
  {:builtin "time.parse_ns" :args-types ["string" "string"]}
  [{[layout value] :args}]
  (check-args (meta #'builtin-time-parse-ns) value layout)
  (let [nanos (instant-to-ns (parse-formatted-datetime value layout))]
    (errors/try-or-throw #(long nanos) (errors/builtin-ex "time.parse_ns: time outside of valid range"))))

(defn builtin-time-parse-rfc3339-ns
  "Implementation of time.parse_rfc3339_ns built-in"
  {:builtin "time.parse_rfc3339_ns" :args-types ["string"]}
  [{[s] :args}]
  (check-args (meta #'builtin-time-parse-rfc3339-ns) s)
  (let [nanos (instant-to-ns (parse-iso-zoned-datetime s))]
    (errors/try-or-throw #(long nanos) (errors/builtin-ex "time.parse_rfc3339_ns: time outside of valid range"))))

(ns jarl.builtins.time
  (:require [jarl.exceptions :as errors]
            [jarl.utils :refer [instant-to-ns ns-to-instant]]
            [clojure.string :as str])
  (:import (java.time ZoneId LocalDateTime ZoneOffset ZonedDateTime Duration LocalDate Period)
           (java.time.format TextStyle DateTimeFormatter)
           (java.util Locale)
           (java.time.temporal ChronoUnit)))

; Courtesy of https://github.com/newm4n/go-dfe
(def ^:private go->java-map
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

(def ^:private go->java-sorted-map (into (sorted-map-by key-length-sorter) go->java-map))

(defn- go->java-formatter ^String [format]
  (reduce (fn [acc [go java]] (str/replace acc go java)) format go->java-sorted-map))

(defn parse-iso-datetime
  ([s]
   (parse-iso-datetime s DateTimeFormatter/ISO_LOCAL_DATE_TIME))
  ([s f]
   (let [ldt (errors/try-or #(LocalDateTime/parse s f)
                            (.atStartOfDay (LocalDate/parse s f)))] ; if only date, i.e. 2012-12-12
       (.toInstant ^LocalDateTime ldt ZoneOffset/UTC))))

(defn parse-iso-zoned-datetime
  ([s]
   (parse-iso-zoned-datetime s DateTimeFormatter/ISO_ZONED_DATE_TIME))
  ([s f]
   (-> s (ZonedDateTime/parse f) (.toInstant))))

(def ^:private jvm-major-version
  (let [jvm-version (System/getProperty "java.vm.version")]
    (parse-long (subs jvm-version 0 (str/index-of jvm-version ".")))))

(defn- fixup-am-pm
  "Workaround for weird behavior in Java date time parsing, where AM/PM vs. am/pm is expected depending on JVM version"
  [s]
  (if (> jvm-major-version 11)
    (-> s (str/replace "AM" "am") (str/replace "PM" "pm"))
    (-> s (str/replace "am" "AM") (str/replace "pm" "PM"))))

(defn parse-formatted-datetime [s f]
  (if (= f "01/02 03:04:05PM '06 -0700")
        ; special case for what Go docs refer to as "reference time": 01/02 03:04:05PM '06 -0700
        ; https://pkg.go.dev/time#pkg-constants
        (parse-iso-zoned-datetime (str/replace (fixup-am-pm s) #"'" "")
                                  (DateTimeFormatter/ofPattern "MM/dd hh:mm:ssa yy XX"))
    ; everything else
    (let [pattern (go->java-formatter f)]
      (if (re-find #"[ZXx]" pattern) ; time zone in pattern
        ; TODO: figure out why errors/try-or doesn't work in this context
        (try
          (parse-iso-zoned-datetime s (DateTimeFormatter/ofPattern pattern))
          (catch Throwable _ (parse-iso-zoned-datetime s)))
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

(defn- ns-with-tz
  "Many of the time.* functions take either an ns number, or a vector of [ns, tz] —
   this normalizes the two variants into the latter, using UTC unless provided"
  ^ZonedDateTime
  [x builtin-name]
  (let [arr (if (vector? x) x [x "UTC"])
        tz (second arr)
        time (errors/try-or-throw #(long (first arr))
                                  (errors/builtin-ex "%s: timestamp too big" builtin-name))]
    (.atZone (ns-to-instant time) (ZoneId/of (if (empty? tz) "UTC" tz)))))

(defn builtin-time-add-date
  [{[^long ns ^long years ^long months ^long days] :args}]
  (let [result (instant-to-ns (-> (ZonedDateTime/ofInstant (ns-to-instant ns) (ZoneId/of "UTC"))
                                  (.plus years   ChronoUnit/YEARS)
                                  (.plus months  ChronoUnit/MONTHS)
                                  (.plus days    ChronoUnit/DAYS)
                                  (.toInstant)))]
    (errors/try-or-throw #(long result) (errors/builtin-ex "time.add_date: time outside of valid range"))))

(defn builtin-time-clock
  [{[x] :args}]
  (let [ins (ns-with-tz x "time.clock")]
    [(.getHour ins) (.getMinute ins) (.getSecond ins)]))

(defn builtin-time-date
  [{[x] :args}]
  (let [ins (ns-with-tz x "time.date")]
    [(.getYear ins) (.getMonthValue ins) (.getDayOfMonth ins)]))

; TODO: Commented out in registry - needs to work with both Period and Duration
(defn builtin-time-diff
  [{[x y] :args}]
  (let [ns1  (ns-with-tz x "time.diff")
        ns2  (ns-with-tz y "time.diff")
        per  ^Period (Period/between (.toLocalDate ns1) (.toLocalDate ns2))]
    [(.getYears  per)
     (.getMonths per)
     (.getDays   per)
     (- (.getHour ns1) (.getHour ns2))
     (- (.getMinute ns1) (.getMinute ns2))
     (- (.getSecond ns1) (.getSecond ns2))]))

(defn builtin-time-now-ns
  [{bctx :builtin-context}]
  (get bctx :time-now-ns))

(defn builtin-time-weekday
  [{[x] :args}]
  (-> (ns-with-tz x "time.weekday")
      (.getDayOfWeek)
      (.getDisplayName TextStyle/FULL Locale/ENGLISH)))

(defn unknown-duration-ex [unit time]
  (errors/builtin-ex "time.parse_duration_ns: time: unknown unit \"%s\" in duration \"%s\""
                     unit (str time unit)))

(defn to-duration [[_ time unit]]
  (d (Long/parseLong time) (errors/get-or-throw unit->temporal unit (unknown-duration-ex unit time))))

(defn builtin-time-parse-duration-ns
  [{[s] :args}]
  (if-let [time-unit-pairs (re-seq #"(\d+)(\p{L}+)" s)] ; returns a seq of [full match, time, unit]
    (.toNanos ^Duration (reduce d+ (map to-duration time-unit-pairs)))
    (if (errors/throws? #(Long/parseLong s))
      (throw (errors/builtin-ex "time.parse_duration_ns: time: invalid duration \"%s\"" s))
      (throw (errors/builtin-ex "time.parse_duration_ns: time: missing unit in duration \"%s\"" s)))))

(defn builtin-time-parse-ns
  [{[layout value] :args}]
  (let [nanos (instant-to-ns (parse-formatted-datetime value layout))]
    (errors/try-or-throw #(long nanos) (errors/builtin-ex "time.parse_ns: time outside of valid range"))))

(defn builtin-time-parse-rfc3339-ns
  [{[s] :args}]
  (let [nanos (instant-to-ns (parse-iso-zoned-datetime s))]
    (errors/try-or-throw #(long nanos) (errors/builtin-ex "time.parse_rfc3339_ns: time outside of valid range"))))

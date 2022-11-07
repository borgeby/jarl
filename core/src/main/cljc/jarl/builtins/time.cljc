(ns jarl.builtins.time
  (:require [clojure.string :as str]
            [tick.core :as t]
            [tick.locale-en-us]
            [tick.timezone]
            [jarl.exceptions :as errors]
            [jarl.utils :refer [instant->ns ns->instant]]))

(def iso-zoned-date-time-fmt (t/predefined-formatters :iso-zoned-date-time))
(def iso-local-date-time-fmt (t/predefined-formatters :iso-local-date-time))

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
   (parse-iso-datetime s iso-local-date-time-fmt))
  ([s f]
   (-> (errors/try-or #(t/parse-date-time s f) (t/beginning (t/parse-date s f))) ; if only date, i.e. 2012-12-12
       (t/in "UTC")
       t/instant)))

(defn parse-iso-zoned-datetime
  ([s]
   (parse-iso-zoned-datetime s iso-zoned-date-time-fmt))
  ([s f]
   (-> s (t/parse-zoned-date-time f) t/instant)))

#?(:clj
   (def ^:private jvm-major-version
     (let [jvm-version (System/getProperty "java.vm.version")]
       (parse-long (subs jvm-version 0 (str/index-of jvm-version "."))))))

#?(:clj
   (defn- fixup-am-pm
     "Workaround for weird behavior in Java date time parsing, where AM/PM vs. am/pm is expected depending on JVM version"
     [s]
     (if (> jvm-major-version 11)
       (-> s (str/replace "AM" "am") (str/replace "PM" "pm"))
       (-> s (str/replace "am" "AM") (str/replace "pm" "PM")))))

(defn parse-formatted-datetime [s f]
  (if (= f "01/02 03:04:05PM '06 -0700")
        ; special case for what Go docs refer to as "reference time": 01/02 03:04:05PM '06 -0700
        ; https://pkg.go.dev/time#pkg-constants
        (parse-iso-zoned-datetime #?(:clj  (str/replace (fixup-am-pm s) #"'" "")
                                     :cljs (str/replace s #"'" ""))
                                  (t/formatter "MM/dd hh:mm:ssa yy XX"))
    ; everything else
    (let [pattern (go->java-formatter f)]
      (if (re-find #"[ZXx]" pattern) ; time zone in pattern
        ; TODO: figure out why errors/try-or doesn't work in this context
        (try
          (parse-iso-zoned-datetime s (t/formatter pattern))
          #?(:clj  (catch Throwable _ (parse-iso-zoned-datetime s))
             :cljs (catch js/Object _ (parse-iso-zoned-datetime s))))
        (parse-iso-datetime s (t/formatter pattern))))))

(def unit->temporal
  {"h"  :hours
   "m"  :minutes
   "s"  :seconds
   "ms" :millis
   "us" :micros
   "µs" :micros
   "ns" :nanos})

(defn d [^long time unit]
  (t/new-duration time unit))

(defn- ns-with-tz
  "Many of the time.* functions take either an ns number, or a vector of [ns, tz] —
   this normalizes the two variants into the latter, using UTC unless provided"
  [x]
  (let [arr (if (vector? x) x [x "UTC"])
        tz (second arr)]
    (-> (errors/try-or-throw #(long (first arr))
                             (errors/builtin-ex "timestamp too big"))
        ns->instant
        (t/in (if (empty? tz) "UTC" tz)))))

(defn builtin-time-add-date
  [{[^long ns ^long years ^long months ^long days] :args}]
  (let [result (instant->ns (-> (t/in (ns->instant ns) "UTC")
                                (t/>> (t/of-years years))
                                (t/>> (t/of-months months))
                                (t/>> (t/of-days days))
                                (t/instant)))]
    (errors/try-or-throw #(long result) (errors/builtin-ex "time outside of valid range"))))

(defn builtin-time-clock
  [{[x] :args}]
  (let [ins (ns-with-tz x)]
    [(t/hour ins) (t/minute ins) (t/second ins)]))

(defn builtin-time-date
  [{[x] :args}]
  (let [zdt (ns-with-tz x)]
    [(-> zdt t/year t/int)
     (-> zdt t/month t/int)
     (t/day-of-month zdt)]))

(defn builtin-time-now-ns
  [{bctx :builtin-context}]
  (get bctx :time-now-ns))

(defn builtin-time-weekday
  [{[x] :args}]
  (-> x ns-with-tz t/day-of-week str str/capitalize))

(defn unknown-duration-ex [unit time]
  (errors/builtin-ex "time: unknown unit \"%s\" in duration \"%s\"" unit (str time unit)))

(defn ->duration [[_ time unit]]
  (d (parse-long time) (errors/get-or-throw unit->temporal unit (unknown-duration-ex unit time))))

(defn builtin-time-parse-duration-ns
  [{[s] :args}]
  (let [s (str/replace s "µ" "u")] ; Javascript regex doesn't like unicode, and at least here, it doesn't matter
    (if-let [time-unit-pairs (re-seq #"(\d+)(\D+)" s)] ; returns a seq of [full match, time, unit]
      #_:clj-kondo/ignore
      (t/nanos (reduce t/+ (map ->duration time-unit-pairs)))
      (if (nil? (parse-long s))
        (throw (errors/builtin-ex "time: invalid duration \"%s\"" s))
        (throw (errors/builtin-ex "time: missing unit in duration \"%s\"" s))))))

(defn builtin-time-parse-ns
  [{[layout value] :args}]
  (let [nanos (instant->ns (parse-formatted-datetime value layout))]
    (errors/try-or-throw #(long nanos) (errors/builtin-ex "time outside of valid range"))))

(defn builtin-time-parse-rfc3339-ns
  [{[s] :args}]
  (let [nanos (instant->ns (parse-iso-zoned-datetime s))]
    (errors/try-or-throw #(long nanos) (errors/builtin-ex "time outside of valid range"))))

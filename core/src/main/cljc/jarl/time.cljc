(ns jarl.time
  (:require [clojure.string :as str]
            [cljc.java-time.instant :as i]
            [tick.core :as t]
            [tick.locale-en-us]
            [jarl.exceptions :as errors]))

(def ^:private iso-zoned-date-time-fmt (t/predefined-formatters :iso-zoned-date-time))
(def ^:private iso-local-date-time-fmt (t/predefined-formatters :iso-local-date-time))
(def ^:private rfc-1123-fmt            (t/formatter "EEE, dd MMM yyyy HH:mm:ss zzz"))  ; Mon, 02 Jan 2006 15:04:05 GMT
(def ^:private rfc-850-fmt             (t/formatter "EEEE, dd-MMM-yy HH:mm:ss zzz"))   ; Monday, 02-Jan-06 15:04:05 MST

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

(defn instant->ns [instant]
  #?(:clj  (+' (*' (i/get-epoch-second instant) 1000000000) (t/nanosecond instant))
     :cljs (+  (*  (i/get-epoch-second instant) 1000000000) (t/nanosecond instant))))

(defn ns->instant [ns]
  (i/of-epoch-second 0 ns))

(defn now-ns
  "Current time nanos — not as precise as its OPA/Go equivalent function, but not in any way that matters"
  []
  (instant->ns (t/now)))

(defn parse-iso-zoned-datetime
  ([s]
   (parse-iso-zoned-datetime s iso-zoned-date-time-fmt))
  ([s f]
   (-> s (t/parse-zoned-date-time f) t/instant)))

(defn parse-iso-datetime
  ([s]
   (parse-iso-datetime s iso-local-date-time-fmt))
  ([s f]
   (-> (errors/try-or #(t/parse-date-time s f) (t/beginning (t/parse-date s f))) ; if only date, i.e. 2012-12-12
       (t/in "UTC")
       t/instant)))

#?(:clj
   (def ^:private jvm-major-version
     (let [jvm-version (System/getProperty "java.specification.version")]
       (if-let [dot-idx (str/index-of jvm-version ".")]
         (parse-long (subs jvm-version (+ dot-idx 1)))
         (parse-long jvm-version)))))

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

; Note: ignore the ANSIC date format as nobody should be using that
(defn http-date->instant [date]
  (when (some? date)
    (if-let [zdt (errors/try-or #(t/parse-zoned-date-time date rfc-1123-fmt) nil)]
      (t/instant zdt)
      (when-let [zdt (errors/try-or #(t/parse-zoned-date-time date rfc-850-fmt) nil)]
        (t/instant zdt)))))

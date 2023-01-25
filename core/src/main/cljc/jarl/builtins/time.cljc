(ns jarl.builtins.time
  (:require [clojure.string :as str]
            [tick.core :as t]
            [tick.locale-en-us]
            [tick.timezone]
            [jarl.exceptions :as errors]
            [jarl.time :as time]))

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
        time/ns->instant
        (t/in (if (empty? tz) "UTC" tz)))))

(defn- unknown-duration-ex [unit time]
  (errors/builtin-ex "time: unknown unit \"%s\" in duration \"%s\"" unit (str time unit)))

(defn- ->duration [[_ time unit]]
  (d (parse-long time) (errors/get-or-throw unit->temporal unit (unknown-duration-ex unit time))))

(defn builtin-time-add-date
  [{[^long ns ^long years ^long months ^long days] :args}]
  (let [result (time/instant->ns (-> (t/in (time/ns->instant ns) "UTC")
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
  (let [nanos (time/instant->ns (time/parse-formatted-datetime value layout))]
    (errors/try-or-throw #(long nanos) (errors/builtin-ex "time outside of valid range"))))

(defn builtin-time-parse-rfc3339-ns
  [{[s] :args}]
  (let [nanos (time/instant->ns (time/parse-iso-zoned-datetime s))]
    (errors/try-or-throw #(long nanos) (errors/builtin-ex "time outside of valid range"))))

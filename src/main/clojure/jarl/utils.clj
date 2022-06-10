(ns jarl.utils
  (:import (java.time Instant)))

; FIXME: drop "undefined" values? (new 'Undefined' type required)
(defn map-by-index [array]
  (zipmap (range (count array)) array))

(defn indexed-map-to-array
  "Created array from map values, with nil values in places for missing indices"
  [map]
  (if (empty? map)
    {}
    (let [max-index (key (apply max-key key map))]
      (loop [i 0
             array []]
        (if (> i max-index)
          array
          (recur (inc i) (conj array (get map i))))))))

; Inspired by clojure.core/assoc-in
(defn indiscriminate-assoc-in
  "Works like [clojure.core/assoc-in], but allows any nested component under `m` to be a non-map type,
  which will then be replaced in case `[k & ks]` overlap."
  [m [k & ks] v]
  (let [m (if (map? m)
            m
            {})]
    (if ks
      (update m k indiscriminate-assoc-in ks v)
      (assoc m k v))))

(defn time-now-ns
  "Current time nanos — not as precise as its OPA/Go equivalent function, but not in any way that matters"
  []
  (let [now (Instant/now)]
    (+ (* (.getEpochSecond now) 1000000000) (.getNano now))))

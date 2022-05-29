(ns jarl.utils)

(defn map-by-index [array]
  (zipmap (range (count array)) array))

(defn indexed-map-to-array
  "Created array from map values, with nil values in places for missing indices"
  [map]
  (let [max-index (key (apply max-key key map))]
    (loop [i 0
           array []]
      (if (> i max-index)
        array
        (recur (inc i) (conj array (get map i)))))))

; Inspired by clojure.core/assoc-in
(defn indiscriminate-assoc-in
  "Works like [clojure.core/assoc-in], but allows any nested component under `m` to be a non-map type,
  which will then be replaced in case `[k & ks]` overlap."
  [m [k & ks] v]
  (let [m (if (map? m)
            m
            {})]
    (if ks
      (assoc m k (assoc-in (get m k) ks v))
      (assoc m k v))))

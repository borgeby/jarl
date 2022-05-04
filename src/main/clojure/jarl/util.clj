(ns jarl.util)

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

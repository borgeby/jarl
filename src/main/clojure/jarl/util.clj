(ns jarl.util)

(defn map-by-index [array]
  (loop [array array
         map {}
         i 0]
    (if (empty? array)
      map
      (recur (next array) (assoc map i (first array)) (inc i)))))

(defn indexed-map-to-array [map]
  (let [max-index (key (apply max-key key map))]
    (loop [i 0
           array []]
      (if (> i max-index)
        array
        (recur (inc i) (conj array (get map i)))))))

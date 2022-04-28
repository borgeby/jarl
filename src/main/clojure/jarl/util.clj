;
; Copyright 2022 Johan Fylling, Anders Eknert
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
;

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
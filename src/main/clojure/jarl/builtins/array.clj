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

(ns jarl.builtins.array
  (:require [jarl.builtins.utils :refer [check-args]]))

(defn builtin-concat
  "Implementation of array.concat built-in"
  {:builtin "array.concat" :args-types ["array" "array"]}
  [a b]
  (check-args (meta #'builtin-concat) a b)
  (concat a b))

(defn builtin-reverse
  "Implementation of array.reverse built-in"
  {:builtin "array.reverse" :args-types ["array"]}
  [arr]
  (check-args (meta #'builtin-reverse) arr)
  (reverse arr))

(defn builtin-slice
  "Implementation of array.slice built-in"
  {:builtin "array.slice" :args-types ["array", "number", "number"]}
  [arr start stop]
  (check-args (meta #'builtin-slice) arr start stop)
  (let [start (max start 0)
        stop (min stop (count arr))]
    (if (> start stop)
      []
      (subvec arr start stop))))

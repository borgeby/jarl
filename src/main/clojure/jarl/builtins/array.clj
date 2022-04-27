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
  (:import (se.fylling.jarl BuiltinException)
           (clojure.lang PersistentVector)))

; TODO: Move somewhere else for reuse
(defn java->rego [value]
  (condp instance? value
    String "string"
    Boolean "boolean"
    Double "floating-point number"
    Float "floating-point number"
    Integer "number"
    Long "number"
    Number "number"
    PersistentVector "array"
    (str "unknown type: " (type value) " from value: " value)))

(defn builtin-concat [a b]
  (when-not (vector? a)
    (throw (BuiltinException. (format "array.concat: operand 1 must be array but got %s", (java->rego a)))))
  (when-not (vector? b)
    (throw (BuiltinException. (format "array.concat: operand 2 must be array but got %s", (java->rego b)))))
  (concat a b))

(defn builtin-reverse [arr]
  (when-not (vector? arr)
    (throw (BuiltinException. (format "array.reverse: operand 1 must be array but got %s", (java->rego arr)))))
  (reverse arr))

(defn builtin-slice [arr start stop]
  (when-not (vector? arr)
    (throw (BuiltinException. (format "array.slice: operand 1 must be array but got %s", (java->rego arr)))))
  (when-not (int? start)
    (throw (BuiltinException. (format "array.slice: operand 2 must be number but got %s", (java->rego start)))))
  (when-not (int? stop)
    (throw (BuiltinException. (format "array.slice: operand 3 must be number but got %s", (java->rego stop)))))
  (let [start (max start 0)
        stop (min stop (count arr))]
    (if (> start stop)
      []
      (subvec arr start stop))))

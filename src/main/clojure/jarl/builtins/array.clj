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
  (:require [clojure.tools.logging :as log])
  (:import (se.fylling.jarl BuiltinException)))

(defn builtin-concat [a b]
  (when-not (vector? a)
    (throw (BuiltinException. (format "arg 0 is not an array"))))
  (when-not (vector? b)
    (throw (BuiltinException. (format "arg 1 is not an array"))))
  (clojure.core/concat a b))

(defn builtin-reverse [_]
  (throw (BuiltinException. (format "not implemented"))))

(defn builtin-slice [_]
  (throw (BuiltinException. (format "not implemented"))))
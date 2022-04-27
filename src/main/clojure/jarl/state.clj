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

(ns jarl.state)

(defn get-local [state index]
  (get (get state :local) index))

(defn contains-local? [state index]
  (contains? (get state :local) index))

(defn dissoc-local [state index]
  (let [local (get state :local)]
    (assoc state :local (dissoc local index))))

(defn get-string [state index]
  (get (get (get (get state :static) "strings") index) "value"))

(defn contains-string? [state index]
  (contains? (get (get state :static) "strings") index))

(defn set-local [state index value]
  (let [local (get state :local)]
    (assoc state :local (assoc local index value))))

(defn get-value [state key]
  (let [key-type (get key "type")
        key-value (get key "value")]
    (case key-type
      "local" (get-local state key-value)
      "string_index" (get-string state key-value)
      "bool" key-value
      (throw (Exception. (format "unknown value type '%s'" key-type))))))

(defn contains-value? [state key]
  (let [key-type (get key "type")
        key-value (get key "value")]
    (case key-type
      "local" (contains-local? state key-value)
      "string_index" (contains-string? state key-value)
      "bool" true
      (throw (Exception. (format "unknown value type '%s'" key-type))))))

(defn get-static-string [state index]
  (get-value state {"type" "string_index" "value" index}))

(defn get-func [state name]
  (let [func (get (get state :funcs) name)]
    (if-not (nil? func)
      func
      (get (get state :builtin-funcs) name))))

(defn add-result [state value]
  (let [result-set (get state :result)]
    (assoc state :result-set (conj result-set value))))
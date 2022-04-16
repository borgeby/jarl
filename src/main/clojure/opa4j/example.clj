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

(ns opa4j.example)

(gen-class
  :name com.example.Demo
  :state state
  :init init
  :prefix "-"
  :main false
  ;; declare only new methods, not superclass methods
  :methods [[setLocation [String] void]
            [getLocation [] String]])

;; when we are created we can set defaults if we want.
(defn -init []
  "store our fields as a hash"
  [[] (atom {:location "default"})])

;; little functions to safely set the fields.
(defn setfield
  [this key value]
  (swap! (.state this) into {key value}))

(defn getfield
  [this key]
  (@(.state this) key))

;; "this" is just a parameter, not a keyword
(defn -setLocation [this loc]
  (setfield this :location loc))

(defn  -getLocation
  [this]
  (getfield this :location))

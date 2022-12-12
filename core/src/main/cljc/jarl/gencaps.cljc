(ns jarl.gencaps
  (:require [jarl.builtins.registry :as registry]
            [clojure.core :as core]
            [clojure.set :as set]))

(def supported (set (keys registry/builtins)))

;; Import capabilities.json as a Clojure data structure
(def capabilities (-> "capabilities.json"
                      core/slurp
                      (core/str)
                      core/read-string))

;; Create a new sequence containing only the JSON objects that have matching values in the "supported" set
(def matching-capabilities (filter (fn [capability]
                                     (set/intersection (:name capability) supported))
                                   capabilities))

;; Write the matching JSON objects to a new file called gencaps.json
(core/spit "gencaps.json" (pr-str matching-capabilities))
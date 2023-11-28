(ns jarl.gencaps
  (:require [jarl.builtins.registry :as registry]
            [clojure.set :as set]
            [jarl.encoding.json :as json]))

(def supported (set (keys registry/builtins)))

;; Import capabilities.json as a Clojure data structure
(def capabilities (-> (slurp "capabilities/capabilities.json")
                      (json/read-str)))

;; Create a new sequence containing only the JSON objects that have matching values in the "supported" set
(def matching-capabilities (filter (fn [capability]
                                     (set/intersection (:name capability) supported))
                                   capabilities))

;; Write the matching JSON objects to a new file called gencaps.json
(spit "capabilities/gencaps.json" (json/write-str (pr-str matching-capabilities)))
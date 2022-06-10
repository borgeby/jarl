(ns jarl.builtins.objects
  (:require [clojure.string :as str]
            [jarl.builtins.utils :refer [check-args str->int]]
            [jarl.exceptions :as errors]
            )
  (:import (se.fylling.jarl TypeException)))

(defn builtin-object-get
  "Implementation of object.get built-in"
  {:builtin "object.get" :args-types ["object", "any", "any"]}
  [object key default]
  (if (vector? key)
    (get-in object key default)
    (get object key default)))

(defn builtin-object-remove
  "Implementation of object.remove built-in"
  {:builtin "object.remove" :args-types ["object" #{"object", "set", "array"}]}
  [object ks]
  (try
    (check-args (meta #'builtin-object-remove) object ks)
    (catch TypeException e
      ; Handle wrong error message in OPA until resolved
      ; https://github.com/open-policy-agent/opa/issues/4767
      (throw (errors/type-ex (str/replace (.getMessage e) #"set" "string")))))
  (if (and (map? ks) (not-empty ks))
    (builtin-object-remove object (vec (keys ks)))
    (apply dissoc object ks)))

(defn builtin-object-union
  "Implementation of object.union built-in"
  {:builtin "object.union" :args-types ["object", "object"]}
  [o1 o2]
  (check-args (meta #'builtin-object-union) o1 o2)
  (merge o1 o2))

(defn builtin-object-union-n
  "Implementation of object.union_n built-in"
  {:builtin "object.union" :args-types ["array"]}
  [objects]
  (check-args (meta #'builtin-object-union-n) objects)
  (apply merge objects))

(defn builtin-object-filter
  "Implementation of object.filter built-in"
  {:builtin "object.filter" :args-types ["object", "any"]}
  [object ks]
  (if (map? ks)
    (builtin-object-filter object (keys ks))
    (select-keys object ks)))

; Modified version of:
; https://stackoverflow.com/questions/38893968/how-to-select-keys-in-nested-maps-in-clojure
(defn select-keys* [m paths]
  (let [parts (mapv #(str/split % #"/") paths)
        converted (mapv #(mapv str->int %) parts)]
    (into {} (filter #(-> % val (not= :not-found))
            (into {} (map (fn [p]
                            (let [v (get-in m p :not-found)]
                              ; TODO: assoc-in does not work for arrays/numeric values
                              (assoc-in {} p v))))
                  converted)))))

; TODO: Does not currently build nested objects
(defn builtin-json-filter
  "Implementation of json.filter built-in"
  {:builtin "json.filter" :args-types ["object", "any"]}
  [object paths]
  (select-keys* object paths))

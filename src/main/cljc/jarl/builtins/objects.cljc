(ns jarl.builtins.objects
  (:require [clojure.string :as str]
            [jarl.builtins.utils :refer [str->int]]))

(defn builtin-object-get
  [{[object key default] :args}]
  (if (vector? key)
    (get-in object key default)
    (get object key default)))

(defn builtin-object-remove
  [{[object ks] :args}]
  (if (and (map? ks) (not-empty ks))
    (builtin-object-remove {:args [object (vec (keys ks))]})
    (apply dissoc object ks)))

(defn builtin-object-union
  [{[o1 o2] :args}]
  (merge o1 o2))

(defn builtin-object-union-n
  [{[objects] :args}]
  (apply merge objects))

(defn builtin-object-filter
  [{[object ks] :args}]
  (if (and (map? ks) (not-empty ks))
    (builtin-object-filter {:args [object (vec (keys ks))]})
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
  [{[object paths] :args}]
  (select-keys* object paths))

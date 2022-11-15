(ns jarl.builtins.objects
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [clj-json-pointer.core :as jp]
            [jarl.builtins.utils :refer [str->int]]
            [jarl.exceptions :as errors]))

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

(defn- subvec? [super sub]
  (->> (partition (count sub) 1 super)
       (filter #(= sub %))
       first
       seq
       some?))

(defn- ->key-paths
  ([x] (into {} (->key-paths x [])))
  ([x path]
   (if (map? x)
     (mapcat #(->key-paths (val %) (conj path (key %))) x)
     {path x})))

(defn- sub-match? [sub-val sup-val]
  (cond
    (and (set? sub-val) (set? sup-val))       (set/subset? sub-val sup-val)
    (and (vector? sub-val) (vector? sup-val)) (subvec? sup-val sub-val)
    :else (= sub-val sup-val)))

(defn- submap? [super sub]
  (let [kp-sub (->key-paths sub)
        kp-sup (->key-paths super)]
    (->> kp-sub
         (map #(sub-match? (second %) (get kp-sup (first %))))
         (filter false?)
         first
         nil?)))

(defn builtin-object-subset
  [{[super sub :as args] :args}]
  (cond
    (every? set? args)               (set/subset? sub super)
    (every? vector? args)            (subvec? super sub)
    (every? map? args)               (submap? super sub)
    (and (vector? super) (set? sub)) (set/subset? sub (set super))
    :else (throw (errors/type-ex "both arguments object.subset must be of the same type or array and set"))))

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

(defn- patch-path-prepend-slash [patch]
  (if (string? (get patch "path"))
    (update patch "path" (fn [s] (if-not (str/starts-with? s "/") (str "/" s) s)))
    patch))

(defn builtin-json-patch
  [{[object patches] :args}]
  ; OPA accepts paths without leading /, which is a spec violation, but oh well
  (let [patches (mapv patch-path-prepend-slash patches)]
    (jp/patch object patches)))

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

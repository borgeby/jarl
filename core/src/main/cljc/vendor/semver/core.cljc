; Courtesy of https://github.com/owainlewis/semver
; Changes:
; * Removed all functions not required for our use case
; * Don't use Java interop to ensure this works with cljs too
(ns vendor.semver.core
  (:require [clojure.string :as str]))

(defrecord Semver
  [major minor patch pre-release metadata])

(def ^{:private true} semver
  #"^(\d+)\.(\d+)\.(\d+)(?:-([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?(?:\+([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?$")

(defn valid?
  [^String version]
  (boolean (re-matches semver version)))

(defn parse
  [^String version]
  (when (valid? version)
    (let [[[_ major minor patch pre-release metadata]] (re-seq semver version)
          major-version (parse-long major)
          minor-version (parse-long minor)
          patch-version (parse-long patch)]
      (Semver. major-version minor-version patch-version pre-release metadata))))

(defn- compare-part
  [x y]
  (compare x y))

(defn- compare-split-parts
  [x y]
  (let [[x-parts y-parts] (map #(remove str/blank? (str/split % #"[.]")) [x y])]
    (loop [xs x-parts ys y-parts]
      (cond
        (and (empty? xs) (seq? ys)) 1
        (and (empty? xs) (empty? ys)) 0
        (and (seq xs) (empty? ys)) -1
        :else
        (let [fx (first xs) fy (first ys)]
          (if (= fx fy)
            (recur (rest xs) (rest ys))
            (compare-part fx fy)))))))

(defn- is-snapshot?
  [pre-release]
  (= "SNAPSHOT" pre-release))

(defn- compare-pre-release
  [x y]
  (cond
    (and (is-snapshot? x) (not (is-snapshot? y))) -1
    (and (not (is-snapshot? x)) (is-snapshot? y)) 1
    (and (nil? x) (some? y)) 1
    (and (nil? x) (nil? y)) 0
    (and (some? x) (nil? y)) -1
    ;; Comparing each dot separated identifier from left to right until a difference is found
    :else (compare-split-parts x y)))

(defn- compare-semver
  [v1 v2]
  (if (= (:major v1) (:major v2))
    (if (= (:minor v1) (:minor v2))
      (if (= (:patch v1) (:patch v2))
        (compare-pre-release (:pre-release v1) (:pre-release v2))
        (compare (:patch v1) (:patch v2)))
      (compare (:minor v1) (:minor v2)))
    (compare (:major v1) (:major v2))))

(defn compare-strings
  [^String v1 ^String v2]
  (compare-semver (parse v1) (parse v2)))

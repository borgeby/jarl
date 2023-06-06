(ns jarl.builtins.utils
  (:require [clojure.string :as str]
            [jarl.exceptions :as errors]
            [jarl.types :as types]
            #?(:cljs [goog.crypt :as crypt]))
  #?(:clj (:import (java.nio.charset StandardCharsets))))

(defn numeric? [s]
  (some? (re-matches #"[\d]+" s)))

(defn possibly-int
  "present e.g. 1.0 as 1"
  [num]
  (if (integer? num)
    num
    (if (zero? (mod num 1))
      (long num)
      num)))

(defn str->int
  "Converts a numeric string to int, or returns the string if not a number"
  [s]
  #?(:clj
     (if (numeric? s)
       (.intValue (bigdec s))
       s)
     :cljs
     (let [n (js/parseFloat s)]
       (if (= n js/NaN) s n))))

(defn str->bytes ^bytes [^String s]
  #?(:clj  (.getBytes s StandardCharsets/UTF_8)
     :cljs (crypt/stringToUtf8ByteArray s)))

(defn bytes->str ^String [^bytes ba]
  #?(:clj  (String. ba StandardCharsets/UTF_8)
     :cljs (crypt/utf8ByteArrayToString ba)))

(defn type-match? [expected-type provided-value]
  (let [type (get expected-type "type")
        of (get expected-type "of")
        dynamic (get expected-type "dynamic")
        provided-type (types/->rego provided-value)]
    (cond
      (= "any" type) (or (nil? of) (empty? of) (not (nil? (some #(type-match? % provided-value) of))))
      (= "array" type provided-type) (or (nil? dynamic) (every? #(type-match? dynamic %) provided-value))
      (= "set" type provided-type) (every? #(type-match? of %) provided-value)
      :else (or
              (= type provided-type)
              (and (= type "number") (= provided-type "floating-point number"))))))

(defn- ->human-readable-type [{:strs [type of]}]
  (if (nil? of)
    type
    (if (vector? of)
      (let [types (set (map #(get % "type") of))]
        (if (= types #{"any"}) "any" types)) ; special case for type_name built-in, which is declared as "any of any"
      (get of "type"))))

(defn check-args
  "Check types of provided values, and ensure they match the type names provided in the function metadata"
  [types-def argv]
  (let [operands (vec (range 1 (inc (count types-def))))
        zipped (mapv vector operands types-def argv)]
    (doseq [entry zipped]
      (let [pos (first entry)
            expected-type (second entry)
            value (nth entry 2)
            provided-type (types/->rego value)]
        (when-not (type-match? expected-type value)
          (let [human-readable-type (->human-readable-type expected-type)]
            (if (set? human-readable-type)
              (throw (errors/type-ex "operand %s must be one of {%s} but got %s" pos (str/join ", " human-readable-type) provided-type))
              (throw (errors/type-ex "operand %s must be %s but got %s" pos human-readable-type provided-type)))))))))

(defn typed-seq
  "Ensure that array/set only contains allowed Rego types"
  [arr-or-set allowed-types]
  (let [allowed-set (set allowed-types)
        allow (if (contains? allowed-set "number") (conj allowed-set "floating-point number") allowed-set)
        forbidden (fn [x] (not (contains? allow (types/->rego x))))]
    (when-let [violation (first (filter forbidden arr-or-set))]
      (throw (errors/type-ex "operand must be array or set of %s but got array or set containing %s"
                             (str/join "," allowed-types)
                             (types/->rego violation))))))

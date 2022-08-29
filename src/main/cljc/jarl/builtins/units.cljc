(ns jarl.builtins.units
  (:require [clojure.string :as str]
            [jarl.builtins.utils :refer [numeric?]]
            [jarl.exceptions :as errors]
            #?(:cljs [jarl.utils :as utils])
            #?(:cljs [jarl.types :as types])
            #?(:cljs [cljs.math :as math])))

#?(:clj
   (def unit-map
     {""    (bit-shift-left 1 (* 10 0))
      "ki"  (bit-shift-left 1 (* 10 1))
      "kib" (bit-shift-left 1 (* 10 1))
      "mi"  (bit-shift-left 1 (* 10 2))
      "mib" (bit-shift-left 1 (* 10 2))
      "gi"  (bit-shift-left 1 (* 10 3))
      "gib" (bit-shift-left 1 (* 10 3))
      "ti"  (bit-shift-left 1 (* 10 4))
      "tib" (bit-shift-left 1 (* 10 4))
      "pi"  (bit-shift-left 1 (* 10 5))
      "pib" (bit-shift-left 1 (* 10 5))
      "ei"  (bit-shift-left 1 (* 10 6))
      "eib" (bit-shift-left 1 (* 10 6))

      "k"   1000
      "kb"  1000
      "m"   (* 1000 1000)
      "mb"  (* 1000 1000)
      "g"   (* 1000 1000 1000)
      "gb"  (* 1000 1000 1000)
      "t"   (* 1000 1000 1000 1000)
      "tb"  (* 1000 1000 1000 1000)
      "p"   (* 1000 1000 1000 1000 1000)
      "pb"  (* 1000 1000 1000 1000 1000)
      "e"   (* 1000 1000 1000 1000 1000 1000)
      "eb"  (* 1000 1000 1000 1000 1000 1000)

      "siMilli" 0.001
      "siK"     1000
      "siM"     (* 1000 1000)
      "siG"     (* 1000 1000 1000)
      "siT"     (* 1000 1000 1000 1000)
      "siP"     (* 1000 1000 1000 1000 1000)
      "siE"     (* 1000 1000 1000 1000 1000 1000)}))

#?(:cljs
  (def unit-map
    {""     (bit-shift-left (js/BigInt 1) (* (js/BigInt 10) (js/BigInt 0)))
     "ki"   (bit-shift-left (js/BigInt 1) (* (js/BigInt 10) (js/BigInt 1)))
     "kib"  (bit-shift-left (js/BigInt 1) (* (js/BigInt 10) (js/BigInt 1)))
     "mi"   (bit-shift-left (js/BigInt 1) (* (js/BigInt 10) (js/BigInt 2)))
     "mib"  (bit-shift-left (js/BigInt 1) (* (js/BigInt 10) (js/BigInt 2)))
     "gi"   (bit-shift-left (js/BigInt 1) (* (js/BigInt 10) (js/BigInt 3)))
     "gib"  (bit-shift-left (js/BigInt 1) (* (js/BigInt 10) (js/BigInt 3)))
     "ti"   (bit-shift-left (js/BigInt 1) (* (js/BigInt 10) (js/BigInt 4)))
     "tib"  (bit-shift-left (js/BigInt 1) (* (js/BigInt 10) (js/BigInt 4)))
     "pi"   (bit-shift-left (js/BigInt 1) (* (js/BigInt 10) (js/BigInt 5)))
     "pib"  (bit-shift-left (js/BigInt 1) (* (js/BigInt 10) (js/BigInt 5)))
     "ei"   (bit-shift-left (js/BigInt 1) (* (js/BigInt 10) (js/BigInt 6)))
     "eib"  (bit-shift-left (js/BigInt 1) (* (js/BigInt 10) (js/BigInt 6)))

     "k"    (js/BigInt 1000)
     "kb"   (js/BigInt 1000)
     "m"    (* (js/BigInt 1000) (js/BigInt 1000))
     "mb"   (* (js/BigInt 1000) (js/BigInt 1000))
     "g"    (* (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000))
     "gb"   (* (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000))
     "t"    (* (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000))
     "tb"   (* (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000))
     "p"    (* (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000))
     "pb"   (* (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000))
     "e"    (* (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000))
     "eb"   (* (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000))

     "siMilli" 0.001
     "siK"     (js/BigInt 1000)
     "siM"     (* (js/BigInt 1000) (js/BigInt 1000))
     "siG"     (* (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000))
     "siT"     (* (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000))
     "siP"     (* (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000))
     "siE"     (* (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000) (js/BigInt 1000))}))

(defn- up-unit->val [unit]
  (case unit
    ""          (get unit-map "")
    "m"         (get unit-map "siMilli")
    "M"         (get unit-map "siM")
    ("k" "K")   (get unit-map "siK")
    ("ki" "Ki") (get unit-map "ki")
    ("mi" "Mi") (get unit-map "mi")
    ("g" "G")   (get unit-map "siG")
    ("gi" "Gi") (get unit-map "gi")
    ("t" "T")   (get unit-map "siT")
    ("ti" "Ti") (get unit-map "ti")
    ("p" "P")   (get unit-map "siP")
    ("pi" "Pi") (get unit-map "pi")
    ("e" "E")   (get unit-map "siE")
    ("ei" "Ei") (get unit-map "ei")
    (throw (errors/builtin-ex "unit %s not recognized" unit))))

(defn- upb-unit->val [unit]
  (or (get unit-map unit)
      (throw (errors/builtin-ex "byte unit %s not recognized" unit))))

(defn- validate-formatted [s]
  (if (str/includes? s " ")
    (throw (errors/builtin-ex "spaces not allowed in resource strings"))
    s))

(defn- validate-up [[num _ :as num-unit]]
  (cond
    (empty? num)                     (throw (errors/builtin-ex "no amount provided"))
    (> (count (re-seq #"\." num)) 1) (throw (errors/builtin-ex "could not parse amount to a number"))
    :else num-unit))

(defn- validate-upb [[num _ :as num-unit]]
  (cond
    (empty? num)                     (throw (errors/builtin-ex "no byte amount provided"))
    (> (count (re-seq #"\." num)) 1) (throw (errors/builtin-ex "could not parse byte amount to a number"))
    :else num-unit))

#?(:cljs
  (defn- fake-bigdec-multiply
    "NOTE: only works for this use case, something more extensive would be needed to implement generic bigdec math"
    [x y]
    (let [[bx num-dec] (if (str/includes? x ".")
                         (let [[int decimals] (str/split x #"\.")]
                           [(js/BigInt (str int decimals)) (count decimals)])
                         [(js/BigInt x) 0])]
      (if (types/bigint? y)
        (let [mu (* bx y)
              po  (int (math/pow 10 num-dec))
              mo  (mod mu (js/BigInt po))
              result (/ mu (js/BigInt po))]
          (if (or (= result utils/bigint-zero) (not= mo utils/bigint-zero))
            (/ (js/Number mu) po)
            result))
        (* (parse-double x) y)))))

(defn- lower-case-rest [s]
  (if (> (count s) 1)
    (str (subs s 0 1) (str/lower-case (subs s 1)))
    s))

(defn builtin-units-parse
  [{[x] :args}]
  (let [formatted  (-> x (str/replace "\"" "") validate-formatted)
        [num unit] (->> (remove #(= "" %) (str/split formatted #""))
                        (split-with #(or (numeric? %) (= "." %)))
                        (mapv str/join)
                        validate-up)
        unit (lower-case-rest unit)]
    (try
      #?(:clj  (* (bigdec num) (up-unit->val unit))
         :cljs (utils/bigint->number (fake-bigdec-multiply num (up-unit->val unit))))
      #?(:clj  (catch Exception _ (throw (errors/builtin-ex "could not parse amount to a number")))
         :cljs (catch :default  _ (throw (errors/builtin-ex "could not parse amount to a number")))))))

(defn builtin-units-parse-bytes
  [{[x] :args}]
  (let [formatted  (-> x str/lower-case (str/replace "\"" "") validate-formatted)
        [num unit] (->> (remove #(= "" %) (str/split formatted #""))
                        (split-with #(or (numeric? %) (= "." %)))
                        (mapv str/join)
                        validate-upb)]
    (try
      #?(:clj  (bigint (* (bigdec num) (upb-unit->val unit)))
         :cljs (utils/bigint-or-round (utils/bigint->number (fake-bigdec-multiply num (upb-unit->val unit)))))
      #?(:clj  (catch Exception _ (throw (errors/builtin-ex "could not parse byte amount to a number")))
         :cljs (catch :default  _ (throw (errors/builtin-ex "could not parse byte amount to a number")))))))

(ns jarl.encoding.json
  (:require #?(:clj  [clojure.data.json])
            #?(:cljs [camel-snake-kebab.extras :as cske])
            #?(:cljs [jarl.utils :as utils])))

(defn read-str
  "Read string s with optional options map, where the following keys are currently supported:
  :key-fn   - transform all keys of the returned value using the provided function
  :value-fn - transform all values of the returned value using the provided function"
  [s & [options]]
  #?(:clj  (clojure.data.json/read-str s options)
     :cljs (let [decoded (js->clj (js/JSON.parse s))]
             (cond->> decoded
                      (:key-fn options) (cske/transform-keys (:key-fn options))
                      (:value-fn options) (utils/transform-map-vals (:value-fn options))))))

(defn write-str
  "Write value x to JSON string with optional options map, where the following keys are currently supported:
  :key-fn - transform all keys of the returned value using the provided function"
  [x & [options]]
  (let [options (merge {:escape-slash false} (or options {}))]
    #?(:clj  (clojure.data.json/write-str x options)
       :cljs (if-let [key-fn (:key-fn options)]
               (js/JSON.stringify (clj->js (cske/transform-keys key-fn x)))
               (js/JSON.stringify (clj->js x))))))

(defn write-str-pretty [x]
  #?(:clj  (with-out-str (clojure.data.json/pprint x {:escape-slash false}))
     :cljs (js/JSON.stringify (clj->js x) nil 2)))

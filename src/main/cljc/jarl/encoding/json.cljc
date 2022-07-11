(ns jarl.encoding.json
  (:require #?(:clj [clojure.data.json])))

(defn read-str [s]
  #?(:clj  (clojure.data.json/read-str s)
     :cljs (js->clj (js/JSON.parse s))))

(defn write-str [x]
  #?(:clj  (clojure.data.json/write-str x)
     :cljs (js/JSON.stringify (clj->js x))))

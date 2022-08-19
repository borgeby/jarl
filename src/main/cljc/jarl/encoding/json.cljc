(ns jarl.encoding.json
  (:require #?(:clj [clojure.data.json])))

(defn read-str [s]
  #?(:clj  (clojure.data.json/read-str s)
     :cljs (js->clj (js/JSON.parse s))))

(defn write-str [x]
  #?(:clj  (clojure.data.json/write-str x {:escape-slash false})
     :cljs (js/JSON.stringify (clj->js x))))

(defn write-str-pretty [x]
  #?(:clj  (with-out-str (clojure.data.json/pprint x {:escape-slash false}))
     :cljs (js/JSON.stringify (clj->js x) nil 2)))

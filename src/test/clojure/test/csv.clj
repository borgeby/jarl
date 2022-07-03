(ns test.csv
  (:require [clojure.java.io :as io]
            [clojure.set :refer [union]]
            [clojure.string :as string]))

(defn read-file [file]
  (let [lines (with-open [reader (io/reader file)]
                (reduce conj [] (line-seq reader)))
        lines (map #(string/split % #",") lines)
        categories (vec (first lines))
        entries (next lines)
        data (vec (map #(zipmap categories %) entries))]
    {:categories (set categories)
     :data       data}))

(defn write-file [csv file ordered-categories]
  (let [categories (:categories csv)
        data (:data csv)
        ordered-categories (vec ordered-categories)         ; enforce insert order of into
        unordered-categories (sort (filter #(not (some #{%} ordered-categories)) categories))
        categories (into ordered-categories unordered-categories)
        header (string/join "," categories)
        text (loop [rows header
                    data data]
               (if (empty? data)
                 rows
                 (let [current-data (first data)
                       row (string/join "," (map #(get current-data % "") categories))]
                   (recur (str rows "\n" row) (next data)))))]
    (spit file text)))

(defn add [csv entry]
  (let [categories (union (:categories csv) (set (keys entry)))]
    {:categories categories
     :data       (conj (:data csv) entry)}))
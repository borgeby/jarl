(ns jarl.io
  #?(:cljs (:require [cljs.nodejs :as nodejs]))
  #?(:clj (:import (java.io File))))

#?(:cljs (def fs      (nodejs/require "fs")))
#?(:cljs (def path    (nodejs/require "path")))

(defn dir? [^String path]
  #?(:clj  (.isDirectory (File. path))
     :cljs (-> fs (.statSync path) (.isDirectory))))

(defn file? [^String path]
  #?(:clj  (.isFile (File. path))
     :cljs (-> fs (.statSync path) (.isFile))))

#?(:cljs
   (defn path-resolve
     ([p1]
      (path.resolve p1))
     ([p1 p2]
      (path.resolve p1 p2))))

#?(:cljs
   (defn read-dir [path]
     (map #(path-resolve path %)
          (js->clj (.readdirSync fs path)))))

(defn f-seq [dir]
  #?(:clj  (file-seq dir)
     :cljs (tree-seq
             (fn [f] (dir? f))
             (fn [d] (seq (read-dir d)))
             dir)))

(defn read-file
  "Read file at path as string"
  [path]
  #?(:clj  (slurp path)
     :cljs (.readFileSync fs (path-resolve path))))

(ns jarl.io
  #?(:cljs (:require [cljs.nodejs :as nodejs]))
  #?(:clj (:import (java.io File PipedInputStream PipedOutputStream))))

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
             dir?
             (fn [d] (seq (read-dir d)))
             dir)))

(defn read-file
  "Read file at path as string"
  [path]
  #?(:clj  (slurp path)
     :cljs (.readFileSync fs (path-resolve path))))


; Courtesy of https://github.com/ring-clojure/ring/blob/master/ring-core/src/ring/util/io.clj#L11
#?(:clj
  (defn piped-input-stream
    "Create an input stream from a function that takes an output stream as its
    argument. The function will be executed in a separate thread. The stream
    will be automatically closed after the function finishes.

    For example:

      (piped-input-stream
        (fn [ostream]
          (spit ostream \"Hello\")))"
    [func]
    (let [input  (PipedInputStream.)
          output (PipedOutputStream.)]
      (.connect input output)
      (future
        (try
          (func output)
          (finally (.close output))))
      input)))

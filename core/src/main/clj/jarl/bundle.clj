(ns jarl.bundle
  (:require [clojure.string :as str]
            [jarl.builtins.utils :as utils]
            [jarl.encoding.targz :as tgz])
  (:import (java.io InputStream ByteArrayOutputStream)
           (org.apache.commons.compress.archivers.tar TarArchiveEntry)))

(defn- to-byte-array ^bytes [^InputStream in]
  (with-open [out (ByteArrayOutputStream.)]
    (.transferTo in out)
    (.toByteArray out)))

(defn output-stream-fn
  "Builds a new bundle output stream function, with bundle map as its contents"
  [bundle-map]
  (tgz/map->output-stream-fn bundle-map))

(defn extract-plan
  "Extract plan from bundle input stream. Returns nil if no plan.json file is found in bundle"
  ^String
  [^InputStream in]
  (tgz/extract-first-match in #(str/ends-with? (.getName ^TarArchiveEntry %) "plan.json")))

(defn- ends-with-any? [s substrs]
  (->> substrs
       (map #(str/ends-with? s %))
       (filter true?)
       first))

(defn- to-value [filename in]
  (let [bytes (to-byte-array in)]
    (if (ends-with-any? filename [".json" ".rego" ".manifest"])
      (utils/bytes->str bytes)
      bytes)))

(defn- prepend-slash [s]
  (if (str/starts-with? s "/") s (str "/" s)))

(defn extract
  "Extract bundle input stream into a map keyed by filepath inside the archive.
  All data merged into a single /data.json entry"
  [in]
  (with-open [in (tgz/input-stream in)]
    (loop [e (.getNextEntry in) ret {}]
      (if (some? e)
        (if (.isDirectory e)
          (recur (.getNextEntry in) ret)
          (let [filename (prepend-slash (.getName e))
                contents (to-value filename in)]
            (recur (.getNextEntry in) (merge ret {filename contents}))))
        ret))))

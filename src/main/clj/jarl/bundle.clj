(ns jarl.bundle
  (:require [clojure.string :as str]
            [jarl.builtins.utils :as utils])
  (:import (java.io File)
           (java.io FileInputStream InputStream ByteArrayOutputStream)
           (org.apache.commons.compress.archivers.tar TarArchiveInputStream TarArchiveEntry)
           (org.apache.commons.compress.compressors.gzip GzipCompressorInputStream)))

(defn- tar-gz-input-stream ^TarArchiveInputStream [^File file]
  (TarArchiveInputStream. (GzipCompressorInputStream. (FileInputStream. file))))

(defn- to-byte-array ^bytes [^InputStream in]
  (with-open [out (ByteArrayOutputStream.)]
    (.transferTo in out)
    (.toByteArray out)))

(defn extract-plan
  "Extract plan from bundle file. Returns nil if no plan.json file is found in bundle"
  ^String
  [^String file-path]
  (with-open [in (tar-gz-input-stream (File. ^String file-path))]
    (let [next-entry (fn [^TarArchiveInputStream in] #(.getNextEntry in))]
      (when (->> (repeatedly (next-entry in))
                 (take-while some?)
                 (filter #(str/ends-with? (.getName ^TarArchiveEntry %) "plan.json"))
                 first)
        (utils/bytes->str (to-byte-array in))))))

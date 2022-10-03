(ns jarl.encoding.targz
  (:require [clojure.java.io :as io]
            [taoensso.timbre :as log]
            [jarl.builtins.utils :as utils])
  (:import (org.apache.commons.compress.archivers.tar TarArchiveInputStream TarArchiveOutputStream TarArchiveEntry)
           (org.apache.commons.compress.compressors.gzip GzipCompressorInputStream GzipCompressorOutputStream)
           (java.io InputStream IOException OutputStream ByteArrayOutputStream)
           (java.nio.file Path)))

(defn input-stream ^TarArchiveInputStream [^InputStream in]
  (if (instance? TarArchiveInputStream in)
    in
    (TarArchiveInputStream. (GzipCompressorInputStream. in))))

(defn output-stream ^TarArchiveOutputStream [^OutputStream out]
  (if (instance? TarArchiveOutputStream out)
    out
    (TarArchiveOutputStream. (GzipCompressorOutputStream. out))))

(defn next-entry [^TarArchiveInputStream in]
  #(.getNextEntry in))

(defn write-entry [^TarArchiveOutputStream out ^TarArchiveEntry e ^bytes data]
  (let [size (count data)]
    (.setSize e size)
    (.putArchiveEntry out e)
    (try (io/copy (io/input-stream data) out)
         (catch IOException e (log/error e "failed to write archive entry")))
    (.closeArchiveEntry out)))

(defn- ->byte-array ^bytes [^InputStream in]
  (with-open [out (ByteArrayOutputStream.)]
    (.transferTo in out)
    (.toByteArray out)))

(defn extract-first-match
  "Extract file matching predicate from bundle input stream. Returns nil if no matching file is found in bundle"
  ^String
  [^InputStream in pred-fn]
  (with-open [in (input-stream in)]
    (when (->> (repeatedly (next-entry in))
               (take-while some?)
               (filter pred-fn)
               first)
      (utils/bytes->str (->byte-array in)))))

(defn- ->path ^Path [^String s]
  (Path/of s (make-array String 0)))

(defn map->output-stream-fn
  "Builds a new tgz output stream function, with map m as its contents. Expected map structure similar to:
   {/foo.json {...}
    /bar.json {...}}"
  [m]
  (let [paths (map ->path (keys m))]
    (fn [out-stream]
      (with-open [out (output-stream out-stream)]
        (doseq [^TarArchiveEntry e (map #(TarArchiveEntry. (str ^Path %) true) paths)]
          (write-entry out e (utils/str->bytes (get m (.getName e)))))
        (.finish out)))))

(ns jarl.downloader
  (:require [taoensso.timbre :as log]))

; TODO
;(defn- prefer [config]
;  (if-let [lp (:long-polling-timeout-seconds config)]
;    (let [wait (str "wait=" lp)]
;      wait)
;    "bar"))

(defn- download* [config state]
  (log/debug "Download starting.")
  (log/debug "etag =" (:etag @state))
  (swap! state assoc :etag "foo")
  (let [preferences "modes=snapshot,delta"
        headers     {"If-None-Match" "etag"}]
    "foo"
  ; Set (def)
  ))

(defprotocol
  IDownloader
  (download [_] "Download..."))

(deftype Downloader [config state]
  IDownloader
  (download [_]
    (download* config state)))

(defn -main []
  (let [config {:services []}
        state (atom {:etag ""})
        dl (Downloader. config state)]
    (download dl)
    (download dl)
    ))

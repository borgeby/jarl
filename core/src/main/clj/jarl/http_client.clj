(ns jarl.http-client
  (:require [clj-http.lite.client :as client]
            [jarl.exceptions :as errors])
  (:import (java.net UnknownHostException URL)))

; Allow tests to mock without depending on implementation
(def request-fn client/request)

(defn send-request [request]
  (let [request (assoc request :throw-exceptions false)] ; don't throw on non-200 response codes
    (try
      (request-fn request)
      (catch UnknownHostException _
        (let [url (URL. (:url request)) host (.getHost url)]
          {:status 0
           :error  {:code    "eval_http_send_network_error"
                    :message (str "Get \"" url "\": dial tcp: lookup " host ": no such host")}}))
      (catch Exception e
        (if (:raise-error request)
          (throw (errors/builtin-ex "http.send: %s" (ex-message e)))
          ; TODO: determine whether to use eval_http_send_internal_error or eval_http_send_network_error
          {:status 0
           :error {:code    "eval_http_send_network_error"
                   :message (ex-message e)}})))))

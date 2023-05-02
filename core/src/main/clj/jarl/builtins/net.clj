(ns jarl.builtins.net
  (:require [clojure.string :as str])
  (:import (java.net InetAddress)))

(defn- shorten-ipv6
  "Compress IPv6 representation following the RFC5952 standard. Example:
   2001:db8:0:0:0:0:2:1 -> 2001:db8::2:1
   Code from https://stackoverflow.com/questions/7043983/ipv6-address-into-compressed-form-in-java"
  [^String ipv6]
  (if-not (str/includes? ipv6 ":")
    ipv6
    (let [r (.replaceAll ipv6 "((?::0\\b){2,}):?(?!\\S*\\b\\1:0\\b)(\\S*)" "::$2")]
      (if (= ipv6 r)
        ipv6
        (.replaceFirst r "^0::" "::" )))))

(defn lookup
  ; Public for testing / mocking
  [name]
  (map #(.getHostAddress ^InetAddress %) (InetAddress/getAllByName name)))

(defn builtin-net-lookup-ip-addr
  "Note: will use caching as provided by the InetAddress class. See
   https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/InetAddress.html
   for details on configuration, if required."
  [{[name] :args}]
  (->> (lookup name)
       (map shorten-ipv6)
       (set)))

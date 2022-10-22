(ns jarl.config.schema
  (:require [clojure.string  :as str]
            [jarl.formatting :as fmt]
            [malli.core      :as m]
            [malli.error     :as me]
            [malli.transform :as mt]
            [malli.util      :as mu]))

(defn fmt-err [s]
  (fn [{:keys [value]} _]
    (fmt/sprintf s value)))

(def Credentials
  [:map
   [:bearer {:optional true}
    [:map
     [:token :string]
     [:token-path :string]
     [:scheme {:default "Bearer"} :string]]]])

(def Service
  [:map {:optional true}
   [:url :string]
   [:response-header-timeout-seconds {:default 10} :int]
   [:headers {:optional true} [:map-of :string :string]]
   [:tls {:default {}}
    [:map
     [:ca-cert {:optional true} [:maybe :string]]
     [:system-ca-required {:default false} :boolean]]]
   [:type [:enum {:default "" :decode/string str/lower-case} "" "oci"]]
   [:credentials {:optional true} Credentials]])

(def Trigger
 [:enum {:default "periodic" :error/fn (fmt-err "invalid trigger mode \"%s\" (want \"periodic\" or \"manual\")")}
  "periodic"
  "manual"])

(def Polling
  [:and
   [:map
    [:min-delay-seconds {:default 60} :int]
    [:max-delay-seconds {:default 120} :int]
    [:long-polling-timeout-seconds {:optional true} [:maybe [:and int? [:> 0]]]]]
   [:fn
    {:error/message "max polling delay must be >= min polling delay"}
    (fn [{:keys [min-delay-seconds max-delay-seconds]}] (<= min-delay-seconds max-delay-seconds))]])

(def Bundle
  [:map
   [:resource {:optional true} :string]
   [:service :string]
   [:trigger Trigger]
   [:polling {:default {}} Polling]])

(def Config
  [:map
   ; Note: while deprecated in OPA, services may also be provided as an array, where the "name" attribute
   ; denotes the name rather than the map key. While we allow that, the `parse` method will normalize the
   ; array form into the map equivalent before comparison to schema is performed.
   [:services {:optional true} [:map-of :keyword Service]]
   [:bundles  {:optional true} [:map-of :keyword Bundle]]
   [:labels   {:optional true} [:map-of :keyword :string]]])

(def decode-transformer
  (mt/transformer mt/default-value-transformer mt/string-transformer))

(defn- vec->map
  "Converts a vector of items with ':name' attributes to a map keyed by :name"
  [v]
  (reduce (fn [m item] (assoc m (:name item) (dissoc item :name))) {} v))

(defn parse
  "Parses provided config, and injects default values"
  [config]
  (let [config (cond-> config (vector? (:services config)) (assoc :services (vec->map (:services config))))]
    (m/decode Config config decode-transformer)))

(def valid? (m/validator Config))

(defn- apply-options
  [config options]
  (cond-> config
          (:closed-maps options) (mu/closed-schema config)))

(defn validate
  "Validates parsed config, with (optional) options. The options currently supported are:
  :closed-maps - whether to deny extra, unsupported, attributes in config. Default: false"
  [config & [options]]
  (let [config (apply-options config options)]
    (if (valid? config)
      {:valid true}
      ; Note: while we will make no attempt to mimic OPA error messages here, we should
      ; consider how to best present these to end-users
      {:valid false :errors (me/humanize (m/explain Config config))})))

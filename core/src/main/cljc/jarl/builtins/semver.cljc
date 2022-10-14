(ns jarl.builtins.semver
  (:require [jarl.exceptions :as errors]
            [vendor.semver.core :as semver]
            [jarl.types :as types]))

(defn- valid? [vsn]
  (and (string? vsn)
       (semver/valid? vsn)))

(defn- quote-str [s]
  (if (string? s)
    (str "\"" s "\"")
    s))

(defn builtin-semver-is-valid
  [{[vsn] :args}]
  (valid? vsn))

(defn builtin-semver-compare
  [{[a b] :args}]
  (when-not (valid? a)
    (throw (errors/builtin-ex "operand 1: %s %s is not a valid SemVer" (types/->rego a) (quote-str a))))
  (when-not (valid? b)
    (throw (errors/builtin-ex "operand 2: %s %s is not a valid SemVer" (types/->rego b) (quote-str b))))
  (semver/compare-strings a b))

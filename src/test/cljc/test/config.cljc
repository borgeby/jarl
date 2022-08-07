(ns test.config
  (:require [jarl.logging :as logging]))

; :min-level overrideable via project.clj, but does not work for ClojureScript as level needs to be set at compile time
(logging/set-log-level :warn)

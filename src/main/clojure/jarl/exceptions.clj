(ns jarl.exceptions
  (:import (se.fylling.jarl BuiltinException UndefinedException)))

(defn builtin-ex [message & args]
  (BuiltinException. (apply format message args)))

(defn undefined-ex [message & args]
  (UndefinedException. (apply format message args)))

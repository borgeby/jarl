(ns jarl.exceptions
  (:import (se.fylling.jarl BuiltinException ConflictException TypeException UndefinedException)))

(defn builtin-ex [message & args]
  (BuiltinException. (apply format message args)))

(defn undefined-ex [message & args]
  (UndefinedException. (apply format message args)))

(defn conflict-ex [message & args]
  (ConflictException. (apply format message args)))

(defn type-ex [message & args]
  (TypeException. (apply format message args)))

(ns jarl.exceptions
  (:import (se.fylling.jarl BuiltinException
                            ConflictException
                            MultipleOutputsConflictException
                            TypeException
                            UndefinedException
                            JarlException)))

(defn builtin-ex [message & args]
  (BuiltinException. (apply format message args)))

(defn undefined-ex [message & args]
  (UndefinedException. (apply format message args)))

(defn conflict-ex [message & args]
  (ConflictException. (apply format message args)))

(defn multiple-outputs-conflict-ex [cause message & args]
  (MultipleOutputsConflictException. (apply format message args) cause))

(defn type-ex [message & args]
  (TypeException. (apply format message args)))

; Must be a better way to do this
(def rego-ex-type-from-class
  (memoize (fn [clazz]
             (condp = clazz
               BuiltinException (.getType (BuiltinException. ""))
               ConflictException (.getType (ConflictException. ""))
               TypeException (.getType (TypeException. ""))
               ; Unknown class
               ""))))

(defmacro try-or
  "Wrap form in try-catch, and return default in case an exception is thrown"
  [form default]
  `(try
     ~form
     (catch Exception _#
       ~default)))

(defn try-or-throw [func ex]
  (try
    (func)
    (catch Throwable _
      (throw ex))))

(defn throws?
  "Return true if invoking func throws exception"
  [func]
  (= :thrown (try (func) (catch Exception _# :thrown))))

(defn resolve-jarl-exception
  "Return e if JarlException, or a symbol that may be resolved as one, else nil"
  [e]
  (if (and (class? e) (.isAssignableFrom JarlException e))
    e
    (when (symbol? e)
      (let [r (ns-resolve 'jarl.exceptions e)]
        (when (.isAssignableFrom JarlException r)
          r)))))

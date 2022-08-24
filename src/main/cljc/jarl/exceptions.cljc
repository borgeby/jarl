(ns jarl.exceptions
  (:require [jarl.formatting :refer [sprintf]]))

(derive ::eval-exception                      ::jarl-exception)
(derive ::undefined-exception                 ::jarl-exception)

(derive ::builtin-exception                   ::eval-exception)
(derive ::conflict-exception                  ::eval-exception)
(derive ::type-exception                      ::eval-exception)

(derive ::assignment-conflict-exception       ::conflict-exception)
(derive ::multiple-outputs-conflict-exception ::assignment-conflict-exception)

(defn ex-type [ex]
  (if (keyword? ex)
    ex
    (::type (ex-data ex))))

(defmulti  rego-type  ex-type)
(defmethod rego-type ::builtin-exception   [_] "eval_builtin_error")
(defmethod rego-type ::conflict-exception  [_] "eval_conflict_error")
(defmethod rego-type ::type-exception      [_] "eval_type_error")
(defmethod rego-type ::undefined-exception [_] "")
(defmethod rego-type :default              [t] (str "unknown error type: " t))

(defn jarl-exception [type message & args]
  (ex-info (apply sprintf message args)
           {::type type}))

(defn builtin-ex [message & args]
  (apply jarl-exception ::builtin-exception message args))

(defn conflict-ex [message & args]
  (apply jarl-exception ::conflict-exception message args))

(defn assignment-conflict-ex [message & args]
  (apply jarl-exception ::assignment-conflict-exception message args))

(defn multiple-outputs-conflict-ex [cause message & args]
  (ex-info (apply sprintf message args)
           {::type ::multiple-outputs-conflict-exception}
           cause))

(defn type-ex [message & args]
  (apply jarl-exception ::type-exception message args))

(defn undefined-ex [message & args]
  (apply jarl-exception ::undefined-exception message args))

(defn jarl-exception? [ex]
  (isa? (ex-type ex) ::jarl-exception))

(defn builtin-ex? [ex]
  (isa? (ex-type ex) ::builtin-exception))

(defn conflict-ex? [ex]
  (isa? (ex-type ex) ::conflict-exception))

(defn assignment-conflict-ex? [ex]
  (isa? (ex-type ex) ::assignment-conflict-exception))

(defn multiple-outputs-conflict-ex? [ex]
  (isa? (ex-type ex) ::multiple-outputs-conflict-exception))

(defn type-ex? [ex]
  (isa? (ex-type ex) ::type-exception))

(defn undefined-ex? [ex]
  (isa? (ex-type ex) ::undefined-exception))

(defn of-type [ex type]
  (isa? (ex-type ex) type))

(defn exception? [x]
  #?(:clj  (instance? Throwable x)
     :cljs (instance? js/Error x)))

(defn get-or-throw [m key ex]
  (if-not (contains? m key)
    (throw ex)
    (get m key)))

; NOTE: Read this before you try and go fancy with exception macros in .cljc files:
; https://stackoverflow.com/questions/41516492/error-handling-in-cljc-macro

(defn try-return
  "Wrap form in try-catch, and return exception if thrown, else nil"
  [func]
  (try
    (func)
    nil
    #?(:clj  (catch Throwable e e)
       :cljs (catch js/Object e e))))

(defn try-or
  "Wrap func in try-catch, and return default in case an exception is thrown"
  [func default]
  (try
    (func)
    #?(:clj  (catch Throwable _ default)
       :cljs (catch js/Object _ default))))

(defn try-or-throw [func ex]
  (try
    (func)
    #?(:clj  (catch Throwable _# (throw ex))
       :cljs (catch :default  _# (throw ex)))))

(defn throws?
  "Return true if invoking func throws exception"
  [func]
  (= :thrown
     (try
       (func)
       #?(:clj  (catch Throwable _# :thrown)
          :cljs (catch :default  _# :thrown)))))

(ns jarl.exceptions
  (:require [clojure.string :as str]
            [jarl.formatting :refer [sprintf]]))

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

(declare ->message)

(defn ex-nested-data
  ([ex key]
   (ex-nested-data ex key nil))
  ([ex k default]
   (let [val   (k (ex-data ex))
         cause (ex-cause ex)]
     (cond
       (some? val) val
       (some? cause) (ex-nested-data cause k default)
       :else default))))

(defn ex-nested-type
  ([ex default]
   (ex-nested-data ex ::type default))
  ([ex]
   (ex-nested-data ex ::type)))
(defn ex-nested-location
  ([ex default]
   (ex-nested-data ex :location default))
  ([ex]
   (ex-nested-data ex :location)))

(defn ex-nested-context
  ([ex default]
   (ex-nested-data ex :context default))
  ([ex]
   (ex-nested-data ex :context)))

(defn ex-nested-msg
  ([ex default]
   (ex-nested-data ex :msg default))
  ([ex]
   (ex-nested-data ex :msg)))

(defn jarl-exception [type location context message & args]
  (let [msg  (apply sprintf message args)
        data (cond-> {::type type
                      :msg   msg}
                     (some? location) (assoc :location location)
                     (some? context) (assoc :context context))]
    (ex-info (->message data msg) data)))

(defn builtin-ex [message & args]
  (ex-info (apply sprintf message args) {::type ::builtin-exception}))

(defn detailed-builtin-ex [cause location context]
  (let [type (ex-nested-type cause ::builtin-exception)
        msg  (ex-nested-msg cause (ex-message cause))
        loc  (ex-nested-location cause location)]
    (jarl-exception type loc context msg)))

(defn conflict-ex [message & args]
  (apply jarl-exception ::conflict-exception nil nil message args))

(defn assignment-conflict-ex [location message & args]
  (apply jarl-exception ::assignment-conflict-exception location nil message args))

(defn multiple-outputs-conflict-ex [cause location message & args]
  (let [loc  (ex-nested-location cause location)]
    (jarl-exception ::multiple-outputs-conflict-exception loc nil message args)))

(defn type-ex [message & args]
  (apply jarl-exception ::type-exception nil nil message args))

(defn undefined-ex [message & args]
  (apply jarl-exception ::undefined-exception nil nil message args))

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

(defn ->message
  "Format a message from data and msg, presumably obtained from existing exception. Example:
  'policy.rego:33: eval_type_error: startswith: operand 2 must be string but got number'"
  [data msg]
  (let [->location (fn [{:keys [file row]}] (str file ":" row))]
    (str/join ": " (cond-> []
                           (:location data) (conj (->location (:location data)))
                           (::type data)    (conj (rego-type (::type data)))
                           (:context data)  (conj (:context data))
                           :always          (conj msg)))))

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

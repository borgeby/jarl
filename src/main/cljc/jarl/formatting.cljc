(ns jarl.formatting
  #?(:cljs (:require [goog.string :as gstring]
                     [goog.string.format])))

; ClojureScript does not have the `format` function, but provided via the Google Closure compiler
; Use this shim for semi-consistent behavior between the two
(defn sprintf [message & args]
  #?(:clj  (apply format message args)
     :cljs (apply gstring/format message args)))

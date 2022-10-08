(ns jarl.builtins.conversions
  (:require [clojure.string :as str]
            [jarl.exceptions :as errors]))

(defn builtin-to-number
  [{[x] :args}]
  (cond
    (nil?     x) 0
    (number?  x) x
    (boolean? x) (if (true? x) 1 0)
    (string?  x) (let [func (if (str/includes? x ".") parse-double parse-long)]
                   (or (func x)
                       (throw (errors/builtin-ex "invalid syntax"))))))

; Deprecated

(defn builtin-cast-array
  [{[x] :args}]
  (if (or (vector? x) (set? x))
    (vec x)
    (throw (errors/type-ex "operand 1 must be one of {array, set}"))))

(defn builtin-cast-set
  [{[x] :args}]
  (if (or (vector? x) (set? x))
    (set x)
    (throw (errors/type-ex "operand 1 must be one of {array, set}"))))

(defn builtin-cast-string
  [{[x] :args}]
  (if (string? x)
    x
    (throw (errors/type-ex "operand 1 must be boolean"))))

(defn builtin-cast-boolean
  [{[x] :args}]
  (if (boolean? x)
    x
    (throw (errors/type-ex "operand 1 must be boolean"))))

(defn builtin-cast-null
  [{[x] :args}]
  (if (nil? x)
    x
    (throw (errors/type-ex "operand 1 must be null"))))

(defn builtin-cast-object
  [{[x] :args}]
  (if (map? x)
    x
    (throw (errors/type-ex "operand 1 must be object"))))

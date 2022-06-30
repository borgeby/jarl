(ns jarl.builtins.conversions
  (:require [jarl.exceptions :as errors]))

(defn builtin-to-number
  [{[x] :args}]
  (if (nil? x)
    0
    (condp instance? x
      Number x
      Boolean (if (true? x) 1 0)
      String (let [func (if (.contains ^String x ".") #(Double/parseDouble x) #(Integer/parseInt x))]
               (try
                 (func)
                 (catch Exception _
                   (throw (errors/builtin-ex "invalid syntax"))))))))

; Deprecated

(defn builtin-cast-array
  [{[x] :args}]
  (if (or (vector? x) (set? x))
    (vec x)
    (throw (errors/type-ex "cast_array: operand 1 must be one of {array, set}"))))

(defn builtin-cast-set
  [{[x] :args}]
  (if (or (vector? x) (set? x))
    (set x)
    (throw (errors/type-ex "cast_set: operand 1 must be one of {array, set}"))))

(defn builtin-cast-string
  [{[x] :args}]
  (if (string? x)
    x
    (throw (errors/type-ex "cast_string: operand 1 must be boolean"))))

(defn builtin-cast-boolean
  [{[x] :args}]
  (if (boolean? x)
    x
    (throw (errors/type-ex "cast_boolean: operand 1 must be boolean"))))

(defn builtin-cast-null
  [{[x] :args}]
  (if (nil? x)
    x
    (throw (errors/type-ex "cast_null: operand 1 must be null"))))

(defn builtin-cast-object
  [{[x] :args}]
  (if (map? x)
    x
    (throw (errors/type-ex "cast_object: operand 1 must be object"))))

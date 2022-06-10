(ns jarl.builtins.conversions
  (:require [jarl.exceptions :as errors]
            [jarl.builtins.utils :refer [check-args]]))

(defn builtin-to-number
  "Implementation of to_number built-in"
  {:builtin "to_number" :args-types ["any"]}
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
  "Implementation of cast_array built-in"
  {:builtin "cast_array" :args-types [(sorted-set "array" "set")]}
  [{[x] :args}]
  (check-args (meta #'builtin-cast-array) x)
  (vec x))

(defn builtin-cast-set
  "Implementation of cast_set built-in"
  {:builtin "cast_set" :args-types [(sorted-set "array" "set")]}
  [{[x] :args}]
  (check-args (meta #'builtin-cast-set) x)
  (set x))

(defn builtin-cast-string
  "Implementation of cast_string built-in"
  {:builtin "cast_string" :args-types ["string"]}
  [{[x] :args}]
  (check-args (meta #'builtin-cast-string) x)
  x)

(defn builtin-cast-boolean
  "Implementation of cast_boolean built-in"
  {:builtin "cast_boolean" :args-types ["boolean"]}
  [{[x] :args}]
  (check-args (meta #'builtin-cast-boolean) x)
  x)

(defn builtin-cast-null
  "Implementation of cast_null built-in"
  {:builtin "cast_null" :args-types ["null"]}
  [{[x] :args}]
  (check-args (meta #'builtin-cast-null) x)
  x)

(defn builtin-cast-object
  "Implementation of cast_object built-in"
  {:builtin "cast_object" :args-types ["object"]}
  [{[x] :args}]
  (check-args (meta #'builtin-cast-object) x)
  x)

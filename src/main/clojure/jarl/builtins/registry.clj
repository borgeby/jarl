(ns jarl.builtins.registry
  (:require [jarl.builtins.array :as array]
            [jarl.builtins.strings :as strings]
            [jarl.builtins.types :as types]))

(def builtins
  {"array.concat" array/builtin-concat
   "array.reverse" array/builtin-reverse
   "array.slice" array/builtin-slice

   "contains" strings/builtin-contains
   "endswith" strings/builtin-endswith
   "format_int" strings/builtin-format-int
   "indexof" strings/builtin-indexof
   "indexof_n" strings/builtin-indexof-n
   "lower" strings/builtin-lower
   "replace" strings/builtin-replace
   "strings.reverse" strings/builtin-strings-reverse
   "split" strings/builtin-split
   "startswith" strings/builtin-startswith
   "substring" strings/builtin-substring
   "trim" strings/builtin-trim
   "trim_left" strings/builtin-trim-left
   "trim_prefix" strings/builtin-trim-prefix
   "trim_right" strings/builtin-trim-right
   "trim_suffix" strings/builtin-trim-suffix
   "trim_space" strings/builtin-trim-space
   "upper" strings/builtin-upper

   "is_number" types/builtin-is-number
   "is_string" types/builtin-is-string
   "is_boolean" types/builtin-is-boolean
   "is_array" types/builtin-is-array
   "is_set" types/builtin-is-set
   "is_object" types/builtin-is-object
   "is_null" types/builtin-is-null
   "type_name" types/builtin-type-name})

(defn get-builtin [name]
  (get builtins name))

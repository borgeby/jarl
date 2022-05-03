(ns jarl.builtins.registry
  (:require [jarl.builtins.numbers :as numbers]
            [jarl.builtins.array :as array]
            [jarl.builtins.strings :as strings]
            [jarl.builtins.types :as types]
            [jarl.builtins.encoding :as encoding]))

(def builtins
  {"plus" numbers/builtin-plus
   "minus" numbers/builtin-minus
   "mul" numbers/builtin-mul
   "div" numbers/builtin-div
   "rem" numbers/builtin-rem
   "round" numbers/builtin-round
   "ceil" numbers/builtin-ceil
   "floor" numbers/builtin-floor
   "abs" numbers/builtin-abs
   "numbers.range" numbers/builtin-numbers-range

   "array.concat" array/builtin-concat
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
   "type_name" types/builtin-type-name

   "base64.encode" encoding/builtin-base64-encode
   "base64.decode" encoding/builtin-base64-decode
   "base64url.encode" encoding/builtin-base64-url-encode
   "base64url.encode_no_pad" encoding/builtin-base64-url-encode-no-pad
   "base64url.decode" encoding/builtin-base64-url-decode
   "urlquery.encode" encoding/builtin-url-query-encode
   "urlquery.decode" encoding/builtin-url-query-decode
   "json.unmarshal" encoding/builtin-json-unmarshal
   "json.is_valid" encoding/builtin-json-is-valid
   "hex.encode" encoding/builtin-hex-encode
   "hex.decode" encoding/builtin-hex-decode})

(defn get-builtin [name]
  (get builtins name))

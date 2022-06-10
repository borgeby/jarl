(ns jarl.builtins.registry
  (:require [jarl.builtins.comparison :as comparison]
            [jarl.builtins.conversions :as conversions]
            [jarl.builtins.numbers :as numbers]
            [jarl.builtins.aggregates :as aggregates]
            [jarl.builtins.array :as array]
            [jarl.builtins.sets :as sets]
            [jarl.builtins.objects :as objects]
            [jarl.builtins.strings :as strings]
            [jarl.builtins.regex :as regex]
            [jarl.builtins.types :as types]
            [jarl.builtins.bits :as bits]
            [jarl.builtins.encoding :as encoding]))

(def builtins
  {"equal"                    comparison/builtin-equal
   "neq"                      comparison/builtin-neq
   "lt"                       comparison/builtin-lt
   "lte"                      comparison/builtin-lte
   "gt"                       comparison/builtin-gt
   "gte"                      comparison/builtin-gte

   "plus"                     numbers/builtin-plus
   "minus"                    numbers/builtin-minus
   "mul"                      numbers/builtin-mul
   "div"                      numbers/builtin-div
   "rem"                      numbers/builtin-rem
   "round"                    numbers/builtin-round
   "ceil"                     numbers/builtin-ceil
   "floor"                    numbers/builtin-floor
   "abs"                      numbers/builtin-abs
   "numbers.range"            numbers/builtin-numbers-range

   "count"                    aggregates/builtin-count
   "sum"                      aggregates/builtin-sum
   "product"                  aggregates/builtin-product
   "max"                      aggregates/builtin-max
   "min"                      aggregates/builtin-min
   "sort"                     aggregates/builtin-sort

   "array.concat"             array/builtin-concat
   "array.reverse"            array/builtin-reverse
   "array.slice"              array/builtin-slice

   "and"                      sets/builtin-and
   "or"                       sets/builtin-or
   "intersection"             sets/builtin-intersection
   "union"                    sets/builtin-union

   "object.get"               objects/builtin-object-get
   "object.remove"            objects/builtin-object-remove
   "object.filter"            objects/builtin-object-filter
   "object.union"             objects/builtin-object-union
   "object.union_n"           objects/builtin-object-union-n

   "concat"                   strings/builtin-concat
   "contains"                 strings/builtin-contains
   "endswith"                 strings/builtin-endswith
   "format_int"               strings/builtin-format-int
   "indexof"                  strings/builtin-indexof
   "indexof_n"                strings/builtin-indexof-n
   "lower"                    strings/builtin-lower
   "replace"                  strings/builtin-replace
   "strings.reverse"          strings/builtin-strings-reverse
   "split"                    strings/builtin-split
   "startswith"               strings/builtin-startswith
   "substring"                strings/builtin-substring
   "trim"                     strings/builtin-trim
   "trim_left"                strings/builtin-trim-left
   "trim_prefix"              strings/builtin-trim-prefix
   "trim_right"               strings/builtin-trim-right
   "trim_suffix"              strings/builtin-trim-suffix
   "trim_space"               strings/builtin-trim-space
   "upper"                    strings/builtin-upper

   "re_match"                 regex/builtin-re-match ; deprecated
   "regex.match"              regex/builtin-regex-match
   "regex.is_valid"           regex/builtin-regex-is-valid
   "regex.split"              regex/builtin-regex-split
   "regex.find_n"             regex/builtin-regex-find-n

   "is_number"                types/builtin-is-number
   "is_string"                types/builtin-is-string
   "is_boolean"               types/builtin-is-boolean
   "is_array"                 types/builtin-is-array
   "is_set"                   types/builtin-is-set
   "is_object"                types/builtin-is-object
   "is_null"                  types/builtin-is-null
   "type_name"                types/builtin-type-name

   "bits.or"                  bits/builtin-bits-or
   "bits.and"                 bits/builtin-bits-and
   "bits.negate"              bits/builtin-bits-negate
   "bits.xor"                 bits/builtin-bits-xor
   "bits.lsh"                 bits/builtin-bits-lsh
   "bits.rsh"                 bits/builtin-bits-rsh

   "to_number"                conversions/builtin-to-number
   "cast_array"               conversions/builtin-cast-array   ; deprecated
   "cast_set"                 conversions/builtin-cast-set     ; deprecated
   "cast_string"              conversions/builtin-cast-string  ; deprecated
   "cast_boolean"             conversions/builtin-cast-boolean ; deprecated
   "cast_null"                conversions/builtin-cast-null    ; deprecated
   "cast_object"              conversions/builtin-cast-object  ; deprecated

   "base64.encode"            encoding/builtin-base64-encode
   "base64.decode"            encoding/builtin-base64-decode
   "base64url.encode"         encoding/builtin-base64-url-encode
   "base64url.encode_no_pad"  encoding/builtin-base64-url-encode-no-pad
   "base64url.decode"         encoding/builtin-base64-url-decode
   "urlquery.encode"          encoding/builtin-url-query-encode
   "urlquery.decode"          encoding/builtin-url-query-decode
   "json.marshal"             encoding/builtin-json-marshal
   "json.unmarshal"           encoding/builtin-json-unmarshal
   "json.is_valid"            encoding/builtin-json-is-valid
   "hex.encode"               encoding/builtin-hex-encode
   "hex.decode"               encoding/builtin-hex-decode
   "yaml.marshal"             encoding/builtin-yaml-marshal
   "yaml.unmarshal"           encoding/builtin-yaml-unmarshal
   "yaml.is_valid"            encoding/builtin-yaml-is-valid})

(defn get-builtin [name]
  (get builtins name))

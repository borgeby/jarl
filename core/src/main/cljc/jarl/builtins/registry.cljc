(ns jarl.builtins.registry
  (:require [jarl.builtins.aggregates      :as aggregates]
            [jarl.builtins.array           :as array]
            [jarl.builtins.bits            :as bits]
            [jarl.builtins.comparison      :as comparison]
            [jarl.builtins.conversions     :as conversions]
            [jarl.builtins.crypto          :as crypto]
            [jarl.builtins.graphs          :as graphs]
            [jarl.builtins.numbers         :as numbers]
            [jarl.builtins.objects         :as objects]
            [jarl.builtins.opa             :as opa]
            [jarl.builtins.semver          :as semver]
            [jarl.builtins.sets            :as sets]
            [jarl.builtins.strings         :as strings]
            [jarl.builtins.time            :as time]
            [jarl.builtins.types           :as types]
            [jarl.builtins.encoding        :as encoding]
            [jarl.builtins.units           :as units]
            [jarl.exceptions               :as errors]
            #?(:clj [jarl.builtins.http    :as http])
            #?(:clj [jarl.builtins.jwt     :as jwt])
            #?(:clj [jarl.builtins.regex   :as regex])))

(def builtins
  #?(:clj
     {; comparison
      "equal"                             comparison/builtin-equal
      "neq"                               comparison/builtin-neq
      "lt"                                comparison/builtin-lt
      "lte"                               comparison/builtin-lte
      "gt"                                comparison/builtin-gt
      "gte"                               comparison/builtin-gte
      ; numbers
      "plus"                              numbers/builtin-plus
      "minus"                             numbers/builtin-minus
      "mul"                               numbers/builtin-mul
      "div"                               numbers/builtin-div
      "rem"                               numbers/builtin-rem
      "round"                             numbers/builtin-round
      "ceil"                              numbers/builtin-ceil
      "floor"                             numbers/builtin-floor
      "abs"                               numbers/builtin-abs
      "numbers.range"                     numbers/builtin-numbers-range
      ; aggregates
      "count"                             aggregates/builtin-count
      "sum"                               aggregates/builtin-sum
      "product"                           aggregates/builtin-product
      "max"                               aggregates/builtin-max
      "min"                               aggregates/builtin-min
      "sort"                              aggregates/builtin-sort
      ; in keyword
      "internal.member_2"                 aggregates/builtin-internal-member-2
      "internal.member_3"                 aggregates/builtin-internal-member-3
      ; deprecated
      "all"                               aggregates/builtin-all
      "any"                               aggregates/builtin-any
      ; arrays
      "array.concat"                      array/builtin-array-concat
      "array.reverse"                     array/builtin-array-reverse
      "array.slice"                       array/builtin-array-slice
      ; sets
      "and"                               sets/builtin-and
      "or"                                sets/builtin-or
      "intersection"                      sets/builtin-intersection
      "union"                             sets/builtin-union
      ; objects
      "object.get"                        objects/builtin-object-get
      "object.remove"                     objects/builtin-object-remove
      "object.subset"                     objects/builtin-object-subset
      "object.filter"                     objects/builtin-object-filter
      "object.union"                      objects/builtin-object-union
      "object.union_n"                    objects/builtin-object-union-n
      ; strings
      "concat"                            strings/builtin-concat
      "contains"                          strings/builtin-contains
      "endswith"                          strings/builtin-endswith
      "format_int"                        strings/builtin-format-int
      "indexof"                           strings/builtin-indexof
      "indexof_n"                         strings/builtin-indexof-n
      "lower"                             strings/builtin-lower
      "replace"                           strings/builtin-replace
      "strings.reverse"                   strings/builtin-strings-reverse
      "split"                             strings/builtin-split
      "startswith"                        strings/builtin-startswith
      "substring"                         strings/builtin-substring
      "trim"                              strings/builtin-trim
      "trim_left"                         strings/builtin-trim-left
      "trim_prefix"                       strings/builtin-trim-prefix
      "trim_right"                        strings/builtin-trim-right
      "trim_suffix"                       strings/builtin-trim-suffix
      "trim_space"                        strings/builtin-trim-space
      "upper"                             strings/builtin-upper
      ;"sprintf"                          strings/builtin-sprintf
      ; regex
      "re_match"                          regex/builtin-re-match ; deprecated
      "regex.match"                       regex/builtin-regex-match
      "regex.is_valid"                    regex/builtin-regex-is-valid
      "regex.split"                       regex/builtin-regex-split
      "regex.find_n"                      regex/builtin-regex-find-n
      ; types
      "is_number"                         types/builtin-is-number
      "is_string"                         types/builtin-is-string
      "is_boolean"                        types/builtin-is-boolean
      "is_array"                          types/builtin-is-array
      "is_set"                            types/builtin-is-set
      "is_object"                         types/builtin-is-object
      "is_null"                           types/builtin-is-null
      "type_name"                         types/builtin-type-name
      ; bits
      "bits.or"                           bits/builtin-bits-or
      "bits.and"                          bits/builtin-bits-and
      "bits.negate"                       bits/builtin-bits-negate
      "bits.xor"                          bits/builtin-bits-xor
      "bits.lsh"                          bits/builtin-bits-lsh
      "bits.rsh"                          bits/builtin-bits-rsh
      ; conversion
      "to_number"                         conversions/builtin-to-number
      "cast_array"                        conversions/builtin-cast-array   ; deprecated
      "cast_set"                          conversions/builtin-cast-set     ; deprecated
      "cast_string"                       conversions/builtin-cast-string  ; deprecated
      "cast_boolean"                      conversions/builtin-cast-boolean ; deprecated
      "cast_null"                         conversions/builtin-cast-null    ; deprecated
      "cast_object"                       conversions/builtin-cast-object  ; deprecated
      ; units
      "units.parse"                       units/builtin-units-parse
      "units.parse_bytes"                 units/builtin-units-parse-bytes
      ; encoding
      "base64.encode"                     encoding/builtin-base64-encode
      "base64.decode"                     encoding/builtin-base64-decode
      "base64url.encode"                  encoding/builtin-base64-url-encode
      "base64url.encode_no_pad"           encoding/builtin-base64-url-encode-no-pad
      "base64url.decode"                  encoding/builtin-base64-url-decode
      "urlquery.encode"                   encoding/builtin-url-query-encode
      "urlquery.encode_object"            encoding/builtin-url-query-encode-object
      "urlquery.decode"                   encoding/builtin-url-query-decode
      "urlquery.decode_object"            encoding/builtin-url-query-decode-object
      "json.marshal"                      encoding/builtin-json-marshal
      "json.unmarshal"                    encoding/builtin-json-unmarshal
      "json.is_valid"                     encoding/builtin-json-is-valid
      "hex.encode"                        encoding/builtin-hex-encode
      "hex.decode"                        encoding/builtin-hex-decode
      "yaml.marshal"                      encoding/builtin-yaml-marshal
      "yaml.unmarshal"                    encoding/builtin-yaml-unmarshal
      "yaml.is_valid"                     encoding/builtin-yaml-is-valid
      ; token signing
      "io.jwt.encode_sign"                jwt/builtin-io-jwt-encode-sign
      "io.jwt.encode_sign_raw"            jwt/builtin-io-jwt-encode-sign-raw
      ; token verification
      "io.jwt.decode"                     jwt/builtin-io-jwt-decode
      "io.jwt.decode_verify"              jwt/builtin-io-jwt-decode-verify
      "io.jwt.verify_es256"               jwt/builtin-io-jwt-verify-es256
      "io.jwt.verify_es384"               jwt/builtin-io-jwt-verify-es384
      "io.jwt.verify_es512"               jwt/builtin-io-jwt-verify-es512
      "io.jwt.verify_hs256"               jwt/builtin-io-jwt-verify-hs256
      "io.jwt.verify_hs384"               jwt/builtin-io-jwt-verify-hs384
      "io.jwt.verify_hs512"               jwt/builtin-io-jwt-verify-hs512
      "io.jwt.verify_ps256"               jwt/builtin-io-jwt-verify-ps256
      "io.jwt.verify_ps384"               jwt/builtin-io-jwt-verify-ps384
      "io.jwt.verify_ps512"               jwt/builtin-io-jwt-verify-ps512
      "io.jwt.verify_rs256"               jwt/builtin-io-jwt-verify-rs256
      "io.jwt.verify_rs384"               jwt/builtin-io-jwt-verify-rs384
      "io.jwt.verify_rs512"               jwt/builtin-io-jwt-verify-rs512
      ; time
      "time.add_date"                     time/builtin-time-add-date
      "time.clock"                        time/builtin-time-clock
      "time.date"                         time/builtin-time-date
      ;"time.diff"                        time/builtin-time-diff
      "time.now_ns"                       time/builtin-time-now-ns
      "time.weekday"                      time/builtin-time-weekday
      "time.parse_duration_ns"            time/builtin-time-parse-duration-ns
      "time.parse_ns"                     time/builtin-time-parse-ns
      "time.parse_rfc3339_ns"             time/builtin-time-parse-rfc3339-ns
      ; crypto
      "crypto.hmac.md5"                   crypto/builtin-crypto-hmac-md5
      "crypto.hmac.sha1"                  crypto/builtin-crypto-hmac-sha1
      "crypto.hmac.sha256"                crypto/builtin-crypto-hmac-sha256
      "crypto.hmac.sha512"                crypto/builtin-crypto-hmac-sha512
      "crypto.md5"                        crypto/builtin-crypto-md5
      "crypto.sha1"                       crypto/builtin-crypto-sha1
      "crypto.sha256"                     crypto/builtin-crypto-sha256
      "crypto.x509.parse_rsa_private_key" crypto/builtin-crypto-x509-parse-rsa-private-key
      ; graphs
      "graph.reachable"                   graphs/builtin-graph-reachable
      ;"graph.reachable_paths"            graphs/builtin-graph-reachable-paths
      ;"walk"                             graphs/builtin-walk
      ; http
      "http.send"                         http/builtin-http-send
      ; semver
      "semver.compare"                    semver/builtin-semver-compare
      "semver.is_valid"                   semver/builtin-semver-is-valid
      ; rego
      "rego.metadata.chain"               #(throw (errors/builtin-ex "inlined by compiler, should never be invoked"))
      "rego.metadata.rule"                #(throw (errors/builtin-ex "inlined by compiler, should never be invoked"))
      ; opa
      "opa.runtime"                       opa/builtin-opa-runtime}
     :cljs
     {; comparison
      "equal"                             comparison/builtin-equal
      "neq"                               comparison/builtin-neq
      "lt"                                comparison/builtin-lt
      "lte"                               comparison/builtin-lte
      "gt"                                comparison/builtin-gt
      "gte"                               comparison/builtin-gte
      ; numbers
      "plus"                              numbers/builtin-plus
      "minus"                             numbers/builtin-minus
      "mul"                               numbers/builtin-mul
      "div"                               numbers/builtin-div
      "rem"                               numbers/builtin-rem
      "round"                             numbers/builtin-round
      "ceil"                              numbers/builtin-ceil
      "floor"                             numbers/builtin-floor
      "abs"                               numbers/builtin-abs
      "numbers.range"                     numbers/builtin-numbers-range
      ; aggregates
      "count"                             aggregates/builtin-count
      "sum"                               aggregates/builtin-sum
      "product"                           aggregates/builtin-product
      "max"                               aggregates/builtin-max
      "min"                               aggregates/builtin-min
      "sort"                              aggregates/builtin-sort
      "internal.member_2"                 aggregates/builtin-internal-member-2
      "internal.member_3"                 aggregates/builtin-internal-member-3
      "all"                               aggregates/builtin-all
      "any"                               aggregates/builtin-any
      ; arrays
      "array.concat"                      array/builtin-array-concat
      "array.reverse"                     array/builtin-array-reverse
      "array.slice"                       array/builtin-array-slice
      ; sets
      "and"                               sets/builtin-and
      "or"                                sets/builtin-or
      "intersection"                      sets/builtin-intersection
      "union"                             sets/builtin-union
      ; objects
      "object.get"                        objects/builtin-object-get
      "object.remove"                     objects/builtin-object-remove
      "object.subset"                     objects/builtin-object-subset
      "object.filter"                     objects/builtin-object-filter
      "object.union"                      objects/builtin-object-union
      "object.union_n"                    objects/builtin-object-union-n
      ; strings
      "concat"                            strings/builtin-concat
      "contains"                          strings/builtin-contains
      "endswith"                          strings/builtin-endswith
      "format_int"                        strings/builtin-format-int
      ;"indexof"                           strings/builtin-indexof
      ;"indexof_n"                         strings/builtin-indexof-n
      "lower"                             strings/builtin-lower
      "replace"                           strings/builtin-replace
      "strings.reverse"                   strings/builtin-strings-reverse
      "split"                             strings/builtin-split
      "startswith"                        strings/builtin-startswith
      ;"substring"                        strings/builtin-substring
      "trim"                              strings/builtin-trim
      "trim_left"                         strings/builtin-trim-left
      "trim_prefix"                       strings/builtin-trim-prefix
      "trim_right"                        strings/builtin-trim-right
      "trim_suffix"                       strings/builtin-trim-suffix
      "trim_space"                        strings/builtin-trim-space
      "upper"                             strings/builtin-upper
      ; conversion
      "to_number"                         conversions/builtin-to-number
      "cast_array"                        conversions/builtin-cast-array   ; deprecated
      "cast_set"                          conversions/builtin-cast-set     ; deprecated
      "cast_string"                       conversions/builtin-cast-string  ; deprecated
      "cast_boolean"                      conversions/builtin-cast-boolean ; deprecated
      "cast_null"                         conversions/builtin-cast-null    ; deprecated
      "cast_object"                       conversions/builtin-cast-object  ; deprecated
      ; units
      "units.parse"                       units/builtin-units-parse
      "units.parse_bytes"                 units/builtin-units-parse-bytes
      ; types
      "is_number"                         types/builtin-is-number
      "is_string"                         types/builtin-is-string
      "is_boolean"                        types/builtin-is-boolean
      "is_array"                          types/builtin-is-array
      "is_set"                            types/builtin-is-set
      "is_object"                         types/builtin-is-object
      "is_null"                           types/builtin-is-null
      "type_name"                         types/builtin-type-name
      ; bits
      "bits.or"                           bits/builtin-bits-or
      "bits.and"                          bits/builtin-bits-and
      "bits.negate"                       bits/builtin-bits-negate
      "bits.xor"                          bits/builtin-bits-xor
      "bits.lsh"                          bits/builtin-bits-lsh
      "bits.rsh"                          bits/builtin-bits-rsh
      ; encoding
      "base64.encode"                     encoding/builtin-base64-encode
      "base64.decode"                     encoding/builtin-base64-decode
      "base64url.encode"                  encoding/builtin-base64-url-encode
      "base64url.encode_no_pad"           encoding/builtin-base64-url-encode-no-pad
      "base64url.decode"                  encoding/builtin-base64-url-decode
      "urlquery.encode"                   encoding/builtin-url-query-encode
      "urlquery.encode_object"            encoding/builtin-url-query-encode-object
      "urlquery.decode"                   encoding/builtin-url-query-decode
      "urlquery.decode_object"            encoding/builtin-url-query-decode-object
      "json.marshal"                      encoding/builtin-json-marshal
      "json.unmarshal"                    encoding/builtin-json-unmarshal
      "json.is_valid"                     encoding/builtin-json-is-valid
      "hex.encode"                        encoding/builtin-hex-encode
      "hex.decode"                        encoding/builtin-hex-decode
      ;"yaml.marshal"                     encoding/builtin-yaml-marshal
      ;"yaml.unmarshal"                   encoding/builtin-yaml-unmarshal
      ;"yaml.is_valid"                    encoding/builtin-yaml-is-valid
      ; time
      "time.add_date"                     time/builtin-time-add-date
      "time.clock"                        time/builtin-time-clock
      "time.date"                         time/builtin-time-date
      ;"time.diff"                        time/builtin-time-diff
      "time.now_ns"                       time/builtin-time-now-ns
      "time.weekday"                      time/builtin-time-weekday
      "time.parse_duration_ns"            time/builtin-time-parse-duration-ns
      "time.parse_ns"                     time/builtin-time-parse-ns
      "time.parse_rfc3339_ns"             time/builtin-time-parse-rfc3339-ns
      ; crypto
      "crypto.hmac.md5"                   crypto/builtin-crypto-hmac-md5
      "crypto.hmac.sha1"                  crypto/builtin-crypto-hmac-sha1
      "crypto.hmac.sha256"                crypto/builtin-crypto-hmac-sha256
      "crypto.hmac.sha512"                crypto/builtin-crypto-hmac-sha512
      "crypto.md5"                        crypto/builtin-crypto-md5
      "crypto.sha1"                       crypto/builtin-crypto-sha1
      "crypto.sha256"                     crypto/builtin-crypto-sha256
      ; graphs
      "graph.reachable"                   graphs/builtin-graph-reachable
      ;"graph.reachable_paths"            graphs/builtin-graph-reachable-paths
      ;"walk"                             graphs/builtin-walk
      ; semver
      "semver.compare"                    semver/builtin-semver-compare
      "semver.is_valid"                   semver/builtin-semver-is-valid
      ; rego
      "rego.metadata.chain"               #(throw (errors/builtin-ex "inlined by compiler, should never be invoked"))
      "rego.metadata.rule"                #(throw (errors/builtin-ex "inlined by compiler, should never be invoked"))
      ; opa
      "opa.runtime"                       opa/builtin-opa-runtime
      }))

(defn get-builtin [name]
  (get builtins name))

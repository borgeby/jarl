# Built-in Functions

## Supported built-in functions

### Comparison

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `==`              |  ✅  |  ✅   |    ✅    |
| `!=`              |  ✅  |  ✅   |    ✅    |
| `<`               |  ✅  |  ✅   |    ✅    |
| `<=`              |  ✅  |  ✅   |    ✅    |
| `>`               |  ✅  |  ✅   |    ✅    |
| `>=`              |  ✅  |  ✅   |    ✅    |

### Numbers

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `+`               |  ✅  |  ✅   |    ✅    |
| `-`               |  ✅  |  ✅   |    ✅    |
| `*`               |  ✅  |  ✅   |    ✅    |
| `/`               |  ✅  |  ✅   |    ✅    |
| `%`               |  ✅  |  ✅   |    ✅    |
| `round`           |  ✅  |  ✅   |    ✅    |
| `ceil`            |  ✅  |  ✅   |    ✅    |
| `floor`           |  ✅  |  ✅   |    ✅    |
| `abs`             |  ✅  |  ✅   |    ✅    |
| `numbers.range`   |  ✅  |  ✅   |    ✅    |
| `rand.intn`       |  ❌  |  ❌   |    ❌    |

Note: ClojureScript doesn't handle [BigInt](https://gist.github.com/mfikes/9fc981ed7a190b8e9b2912eee98fdd5e) /
[BigDecimal](https://blog.janetacarr.com/thoughts-on-clojurescript-and-bigdecimal/) values like Clojure. While we could
probably work around this, these are generally not used in policy, and as such not prioritized.

### Aggregates

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `count`           |  ✅  |  ✅   |    ✅    |
| `sum`             |  ✅  |  ✅   |    ✅    |
| `product`         |  ✅  |  ✅   |    ✅    |
| `max`             |  ✅  |  ✅   |    ✅    |
| `min`             |  ✅  |  ✅   |    ✅    |
| `sort`            |  ✅  |  ✅   |    ✅    |

### Arrays

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `array.concat`    |  ✅  |  ✅   |    ✅    |
| `array.reverse`   |  ✅  |  ✅   |    ✅    |
| `array.slice`     |  ✅  |  ✅   |    ✅    |

### Sets

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `&`               |  ✅  |   ✅  |    ✅    |
| `\|`              |  ✅  |   ✅  |    ✅    |
| `-`               |  ✅  |   ✅  |    ✅    |
| `intersection`    |  ✅  |   ✅  |    ✅    |
| `union`           |  ✅  |   ✅  |    ✅    |

### Objects

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `object.get`      |  ✅  |  ✅   |    ✅    |
| `object.remove`   |  ✅  |  ✅   |    ✅    |
| `object.union`    |  ✅  |  ✅   |    ✅    |
| `object.union_n`  |  ✅  |  ✅   |    ✅    |
| `object.filter`   |  ✅  |  ✅   |    ✅    |
| `json.remove`     |  ❌  |  ❌   |    ❌    |
| `json.patch`      |  ❌  |  ❌   |    ❌    |

### Strings

| Built-in Function   | JVM | Node | Browser |
|---------------------|:---:|:----:|:-------:|
| `concat`            |  ✅  |  ❌   |    ❌    |
| `contains`          |  ✅  |  ❌   |    ❌    |
| `endswith`          |  ✅  |  ❌   |    ❌    |
| `format_int`        |  ✅  |  ❌   |    ❌    |
| `indexof`           |  ✅  |  ❌   |    ❌    |
| `indexof_n`         |  ✅  |  ❌   |    ❌    |
| `lower`             |  ✅  |  ❌   |    ❌    |
| `replace`           |  ✅  |  ❌   |    ❌    |
| `strings.reverse`   |  ✅  |  ❌   |    ❌    |
| `strings.replace_n` |  ❌  |  ❌   |    ❌    |
| `split`             |  ✅  |  ❌   |    ❌    |
| `sprintf`           |  ❌  |  ❌   |    ❌    |
| `startswith`        |  ✅  |  ❌   |    ❌    |
| `substring`         |  ✅  |  ❌   |    ❌    |
| `trim`              |  ✅  |  ❌   |    ❌    |
| `trim_left`         |  ✅  |  ❌   |    ❌    |
| `trim_prefix`       |  ✅  |  ❌   |    ❌    |
| `trim_right`        |  ✅  |  ❌   |    ❌    |
| `trim_suffix`       |  ✅  |  ❌   |    ❌    |
| `trim_space`        |  ✅  |  ❌   |    ❌    |
| `upper`             |  ✅  |  ❌   |    ❌    |

### Regex

| Built-in Function                  | JVM | Node | Browser |
|------------------------------------|:---:|:----:|:-------:|
| `regex.match`                      |  ✅  |  ❌   |    ❌    |
| `regex.is_valid`                   |  ✅  |  ❌   |    ❌    |
| `regex.split`                      |  ✅  |  ❌   |    ❌    |
| `regex.globs_match`                |  ❌  |  ❌   |    ❌    |
| `regex.template_match`             |  ❌  |  ❌   |    ❌    |
| `regex.find_n`                     |  ✅  |  ❌   |    ❌    |
| `regex.find_all_string_submatch_n` |  ❌  |  ❌   |    ❌    |

Clojure: Rego like regexp engine provided by [re2j](https://github.com/google/re2j)

### Glob

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `glob.match`      |  ❌  |  ❌   |    ❌    |
| `glob.quote_meta` |  ❌  |  ❌   |    ❌    |

### Bitwise

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `bits.or`         |  ✅  |  ❌   |    ❌    |
| `bits.and`        |  ✅  |  ❌   |    ❌    |
| `bits.negate`     |  ✅  |  ❌   |    ❌    |
| `bits.xor`        |  ✅  |  ❌   |    ❌    |
| `bits.lsh`        |  ✅  |  ❌   |    ❌    |
| `bits.rsh`        |  ✅  |  ❌   |    ❌    |

### Conversions

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `to_number`       |  ✅  |  ✅   |    ✅    |

### Units

| Built-in Function   | JVM | Node | Browser |
|---------------------|:---:|:----:|:-------:|
| `units.parse`       |  ❌  |  ❌   |    ❌    |
| `units.parse_bytes` |  ❌  |  ❌   |    ❌    |

### Types

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `is_number`       |  ✅  |  ✅   |    ✅    |
| `is_string`       |  ✅  |  ✅   |    ✅    |
| `is_boolean`      |  ✅  |  ✅   |    ✅    |
| `is_array`        |  ✅  |  ✅   |    ✅    |
| `is_set`          |  ✅  |  ✅   |    ✅    |
| `is_object`       |  ✅  |  ✅   |    ✅    |
| `is_null`         |  ✅  |  ✅   |    ✅    |
| `type_name`       |  ✅  |  ✅   |    ✅    |

### Encoding

| Built-in Function         | JVM | Node | Browser |
|---------------------------|:---:|:----:|:-------:|
| `base64.encode`           |  ✅  |  ✅   |    ✅    |
| `base64.decode`           |  ✅  |  ✅   |    ✅    |
| `base64url.encode`        |  ✅  |  ✅   |    ✅    |
| `base64url.encode_no_pad` |  ✅  |  ✅   |    ✅    |
| `base64url.decode`        |  ✅  |  ✅   |    ✅    |
| `urlquery.encode`         |  ✅  |  ✅   |    ✅    |
| `urlquery.encode_object`  |  ✅  |  ✅   |    ✅    |
| `urlquery.decode`         |  ✅  |  ✅   |    ✅    |
| `urlquery.decode_object`  |  ✅  |  ✅   |    ✅    |
| `json.marshal`            |  ✅  |  ✅   |    ✅    |
| `json.unmarshal`          |  ✅  |  ✅   |    ✅    |
| `json.is_valid`           |  ✅  |  ✅   |    ✅    |
| `yaml.marshal`            |  ✅  |  ❌   |    ❌    |
| `yaml.unmarshal`          |  ✅  |  ❌   |    ❌    |
| `yaml.is_valid`           |  ✅  |  ❌   |    ❌    |
| `hex.encode`              |  ✅  |  ✅   |    ✅    |
| `hex.decode`              |  ✅  |  ✅   |    ✅    |

### Token Signing

For JavaScript support, https://github.com/panva/jose looks like the best candidate

| Built-in Function        | JVM | Node | Browser |
|--------------------------|:---:|:----:|:-------:|
| `io.jwt.encode_sign`     |  ✅  |  ❌   |    ❌    |
| `io.jwt.encode_sign_raw` |  ✅  |  ❌   |    ❌    |

### Token Verification

| Built-in Function      | JVM | Node | Browser |
|------------------------|:---:|:----:|:-------:|
| `io.jwt.decode`        |  ✅  |  ❌   |    ❌    |
| `io.jwt.decode_verify` |  ✅  |  ❌   |    ❌    |
| `io.jwt.verify_es256`  |  ✅  |  ❌   |    ❌    |
| `io.jwt.verify_es384`  |  ✅  |  ❌   |    ❌    |
| `io.jwt.verify_es512`  |  ✅  |  ❌   |    ❌    |
| `io.jwt.verify_hs256`  |  ✅  |  ❌   |    ❌    |
| `io.jwt.verify_hs384`  |  ✅  |  ❌   |    ❌    |
| `io.jwt.verify_hs512`  |  ✅  |  ❌   |    ❌    |
| `io.jwt.verify_ps256`  |  ✅  |  ❌   |    ❌    |
| `io.jwt.verify_ps384`  |  ✅  |  ❌   |    ❌    |
| `io.jwt.verify_ps512`  |  ✅  |  ❌   |    ❌    |
| `io.jwt.verify_rs256`  |  ✅  |  ❌   |    ❌    |
| `io.jwt.verify_rs384`  |  ✅  |  ❌   |    ❌    |
| `io.jwt.verify_rs512`  |  ✅  |  ❌   |    ❌    |

### Time

TODO: Use https://github.com/henryw374/cljc.java-time or https://github.com/juxt/tick (high-level version)

| Built-in Function        | JVM | Node | Browser |
|--------------------------|:---:|:----:|:-------:|
| `time.add_date`          |  ✅  |  ❌   |    ❌    |
| `time.clock`             |  ✅  |  ❌   |    ❌    |
| `time.date`              |  ✅  |  ❌   |    ❌    |
| `time.diff`              |  ❌  |  ❌   |    ❌    |
| `time.now_ns`            |  ✅  |  ❌   |    ❌    |
| `time.parse_duration_ns` |  ✅  |  ❌   |    ❌    |
| `time.parse_ns`          |  ✅  |  ❌   |    ❌    |
| `time.parse_rfc3339_ns`  |  ✅  |  ❌   |    ❌    |
| `time.weekday`           |  ✅  |  ❌   |    ❌    |

### Cryptography

| Built-in Function                           | JVM | Node | Browser |
|---------------------------------------------|:---:|:----:|:-------:|
| `crypto.hmac.md5`                           |  ✅  |  ✅   |    ✅    |
| `crypto.hmac.sha1`                          |  ✅  |  ✅   |    ✅    |
| `crypto.hmac.sha256`                        |  ✅  |  ✅   |    ✅    |
| `crypto.hmac.sha512`                        |  ✅  |  ✅   |    ✅    |
| `crypto.md5`                                |  ✅  |  ✅   |    ✅    |
| `crypto.sha1`                               |  ✅  |  ✅   |    ✅    |
| `crypto.sha256`                             |  ✅  |  ✅   |    ✅    |
| `crypto.x509.parse_and_verify_certificates` |  ❌  |  ❌   |    ❌    |
| `crypto.x509.parse_certificate_request`     |  ❌  |  ❌   |    ❌    |
| `crypto.x509.parse_certificates`            |  ❌  |  ❌   |    ❌    |
| `crypto.x509.parse_rsa_private_key`         |  ✅  |  ❌   |    ❌    |

Note: Contrary to the Clojure versions, the ClojureScript HMAC functions won't accept unicode (i.e. non-ascii) input
Note: `crypto.x509.parse_rsa_private_key` currently only works with PKCS8 formatted private keys, **not** PKCS1

### Graphs

| Built-in Function       | JVM | Node | Browser |
|-------------------------|:---:|:----:|:-------:|
| `graph.reachable`       |  ✅  |  ✅   |    ✅    |
| `graph.reachable_paths` |  ❌  |  ❌   |    ❌    |
| `walk`                  |  ❌  |  ❌   |    ❌    |

### GraphQL

| Built-in Function          | JVM | Node | Browser |
|----------------------------|:---:|:----:|:-------:|
| `graphql.is_valid`         |  ❌  |  ❌   |    ❌    |
| `graphql.parse`            |  ❌  |  ❌   |    ❌    |
| `graphql.parse_and_verify` |  ❌  |  ❌   |    ❌    |
| `graphql.parse_query`      |  ❌  |  ❌   |    ❌    |
| `graphql.parse_schema`     |  ❌  |  ❌   |    ❌    |

### HTTP

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `http.send`       |  ❌  |  ❌   |    ❌    |

### Net

| Built-in Function           | JVM | Node | Browser |
|-----------------------------|:---:|:----:|:-------:|
| `net.cidr_contains`         |  ❌  |  ❌   |    ❌    |
| `net.cidr_contains_matches` |  ❌  |  ❌   |    ❌    |
| `net.cidr_expand`           |  ❌  |  ❌   |    ❌    |
| `net.cidr_intersects`       |  ❌  |  ❌   |    ❌    |
| `net.cidr_merge`            |  ❌  |  ❌   |    ❌    |
| `net.cidr_overlap`          |  ❌  |  ❌   |    ❌    |
| `net.lookup_ip_addr`        |  ❌  |  ❌   |    ❌    |

### UUID

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `uuid.rfc4122`    |  ❌  |  ❌   |    ❌    |

### Semantic Versions

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `semver.compare`  |  ✅  |  ✅   |    ✅    |
| `semver.is_valid` |  ✅  |  ✅   |    ✅    |

### Rego

| Built-in Function     | JVM | Node | Browser |
|-----------------------|:---:|:----:|:-------:|
| `rego.metadata.chain` |  ✅  |  ✅   |    ✅    |
| `rego.metadata.rule`  |  ✅  |  ✅   |    ✅    |
| `rego.parse_module`   |  ❌  |  ❌   |    ❌    |

Note: The `rego.metadata.*` functions are inlined by the compiler, and never really exists as built-in functions. There
is thus no need to implement them in IR implementations.

### Debugging

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `print`           |  ❌  |  ❌   |    ❌    |

Note: `print` calls are erased from plans, so likely can't be supported without support in OPA, or through some hack.

### Tracing

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `trace`           |  ❌  |  ❌   |    ❌    |

### OPA

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `opa.runtime`     |  ✅  |  ❌   |    ❌    |

### Deprecated

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `re_match`        |  ✅  |  ❌   |    ❌    |
| `cast_array`      |  ✅  |  ✅   |    ✅    |
| `cast_set`        |  ✅  |  ✅   |    ✅    |
| `cast_string`     |  ✅  |  ✅   |    ✅    |
| `cast_boolean`    |  ✅  |  ✅   |    ✅    |
| `cast_null`       |  ✅  |  ✅   |    ✅    |
| `cast_object`     |  ✅  |  ✅   |    ✅    |

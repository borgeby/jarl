# Jarl

The _JVM Alternative for the Rego Language_ (JARL) is an OPA evaluator for the JVM, written in Clojure.

## Usage

Simple example policy compiled to plan by OPA, and executed by Jarl.

**policy.rego**
```rego
package policy

import future.keywords

default allow := false

allow {
    "admin" in input.user.roles
}
```
```shell
opa build --target plan --entrypoint policy/allow policy.rego
```
This creates a bundle, containing the `plan.json` file. Since this is the only file we care for here, let's extract it
from the bundle:
```shell
tar -zxf bundle.tar.gz /plan.json && rm bundle.tar.gz
```
We now have a plan file that we may submit for execution by Jarl!
```shell
lein run plan.json '{"user": {"roles": ["admin"]}}'
```

Note that the above constitutes a simple flow for development and testing only. Production artifacts, or a fixed API for
integrations, will be here at a later point in time.

## Supported built-in functions

### Comparison

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `==`              |  ✅  |  ❌   |    ❌    |
| `!=`              |  ✅  |  ❌   |    ❌    |
| `<`               |  ✅  |  ❌   |    ❌    |
| `<=`              |  ✅  |  ❌   |    ❌    |
| `>`               |  ✅  |  ❌   |    ❌    |
| `>=`              |  ✅  |  ❌   |    ❌    |

### Numbers

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `+`               |  ✅  |  ❌   |    ❌    |
| `-`               |  ✅  |  ❌   |    ❌    |
| `*`               |  ✅  |  ❌   |    ❌    |
| `/`               |  ✅  |  ❌   |    ❌    |
| `%`               |  ✅  |  ❌   |    ❌    |
| `round`           |  ✅  |  ❌   |    ❌    |
| `ceil`            |  ✅  |  ❌   |    ❌    |
| `floor`           |  ✅  |  ❌   |    ❌    |
| `abs`             |  ✅  |  ❌   |    ❌    |
| `numbers.range`   |  ✅  |  ❌   |    ❌    |
| `rand.intn`       |  ❌  |  ❌   |    ❌    |

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
| `array.concat`    |  ✅  |  ❌   |    ❌    |
| `array.reverse`   |  ✅  |  ❌   |    ❌    |
| `array.slice`     |  ✅  |  ❌   |    ❌    |

### Sets

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `&`               |  ✅  |   ❌  |    ❌    |
| `\|`              |  ✅  |   ❌  |    ❌    |
| `-`               |  ✅  |   ❌  |    ❌    |
| `intersection`    |  ✅  |   ❌  |    ❌    |
| `union`           |  ✅  |   ❌  |    ❌    |

### Objects

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `object.get`      |  ✅  |  ❌   |    ❌    |
| `object.remove`   |  ✅  |  ❌   |    ❌    |
| `object.union`    |  ✅  |  ❌   |    ❌    |
| `object.union_n`  |  ✅  |  ❌   |    ❌    |
| `object.filter`   |  ✅  |  ❌   |    ❌    |
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
| `to_number`       |  ✅  |  ❌   |    ❌    |

### Units

| Built-in Function   | JVM | Node | Browser |
|---------------------|:---:|:----:|:-------:|
| `units.parse`       |  ❌  |  ❌   |    ❌    |
| `units.parse_bytes` |  ❌  |  ❌   |    ❌    |

### Types

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `is_number`       |  ✅  |  ❌   |    ❌    |
| `is_string`       |  ✅  |  ❌   |    ❌    |
| `is_boolean`      |  ✅  |  ❌   |    ❌    |
| `is_array`        |  ✅  |  ❌   |    ❌    |
| `is_set`          |  ✅  |  ❌   |    ❌    |
| `is_object`       |  ✅  |  ❌   |    ❌    |
| `is_null`         |  ✅  |  ❌   |    ❌    |
| `type_name`       |  ✅  |  ❌   |    ❌    |

### Encoding

| Built-in Function         | JVM | Node | Browser |
|---------------------------|:---:|:----:|:-------:|
| `base64.encode`           |  ✅  |  ❌   |    ❌    |
| `base64.decode`           |  ✅  |  ❌   |    ❌    |
| `base64url.encode`        |  ✅  |  ❌   |    ❌    |
| `base64url.encode_no_pad` |  ✅  |  ❌   |    ❌    |
| `base64url.decode`        |  ✅  |  ❌   |    ❌    |
| `urlquery.encode`         |  ✅  |  ❌   |    ❌    |
| `urlquery.encode_object`  |  ✅  |  ❌   |    ❌    |
| `urlquery.decode`         |  ✅  |  ❌   |    ❌    |
| `urlquery.decode_object`  |  ✅  |  ❌   |    ❌    |
| `json.marshal`            |  ✅  |  ❌   |    ❌    |
| `json.unmarshal`          |  ✅  |  ❌   |    ❌    |
| `json.is_valid`           |  ✅  |  ❌   |    ❌    |
| `yaml.marshal`            |  ✅  |  ❌   |    ❌    |
| `yaml.unmarshal`          |  ✅  |  ❌   |    ❌    |
| `yaml.is_valid`           |  ✅  |  ❌   |    ❌    |
| `hex.encode`              |  ✅  |  ❌   |    ❌    |
| `hex.decode`              |  ✅  |  ❌   |    ❌    |

### Token Signing

| Built-in Function        | JVM | Node | Browser |
|--------------------------|:---:|:----:|:-------:|
| `io.jwt.encode_sign`     |  ❌  |  ❌   |    ❌    |
| `io.jwt.encode_sign_raw` |  ❌  |  ❌   |    ❌    |

### Token Verification


| Built-in Function      | JVM | Node | Browser |
|------------------------|:---:|:----:|:-------:|
| `io.jwt.decode`        |  ❌  |  ❌   |    ❌    |
| `io.jwt.decode_verify` |  ❌  |  ❌   |    ❌    |
| `io.jwt.verify_es256`  |  ❌  |  ❌   |    ❌    |
| `io.jwt.verify_es384`  |  ❌  |  ❌   |    ❌    |
| `io.jwt.verify_es512`  |  ❌  |  ❌   |    ❌    |
| `io.jwt.verify_hs256`  |  ❌  |  ❌   |    ❌    |
| `io.jwt.verify_hs384`  |  ❌  |  ❌   |    ❌    |
| `io.jwt.verify_hs512`  |  ❌  |  ❌   |    ❌    |
| `io.jwt.verify_ps256`  |  ❌  |  ❌   |    ❌    |
| `io.jwt.verify_ps384`  |  ❌  |  ❌   |    ❌    |
| `io.jwt.verify_ps512`  |  ❌  |  ❌   |    ❌    |
| `io.jwt.verify_rs256`  |  ❌  |  ❌   |    ❌    |
| `io.jwt.verify_rs384`  |  ❌  |  ❌   |    ❌    |
| `io.jwt.verify_rs512`  |  ❌  |  ❌   |    ❌    |

### Time

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
| `crypto.hmac.md5`                           |  ✅  |  ❌   |    ❌    |
| `crypto.hmac.sha1`                          |  ✅  |  ❌   |    ❌    |
| `crypto.hmac.sha256`                        |  ✅  |  ❌   |    ❌    |
| `crypto.hmac.sha512`                        |  ✅  |  ❌   |    ❌    |
| `crypto.md5`                                |  ✅  |  ❌   |    ❌    |
| `crypto.sha1`                               |  ✅  |  ❌   |    ❌    |
| `crypto.sha256`                             |  ✅  |  ❌   |    ❌    |
| `crypto.x509.parse_and_verify_certificates` |  ❌  |  ❌   |    ❌    |
| `crypto.x509.parse_certificate_request`     |  ❌  |  ❌   |    ❌    |
| `crypto.x509.parse_certificates`            |  ❌  |  ❌   |    ❌    |
| `crypto.x509.parse_rsa_private_key`         |  ❌  |  ❌   |    ❌    |

### Graphs

| Built-in Function       | JVM | Node | Browser |
|-------------------------|:---:|:----:|:-------:|
| `graph.reachable`       |  ❌  |  ❌   |    ❌    |
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
| `semver.compare`  |  ❌  |  ❌   |    ❌    |
| `semver.is_valid` |  ❌  |  ❌   |    ❌    |

### Rego

| Built-in Function     | JVM | Node | Browser |
|-----------------------|:---:|:----:|:-------:|
| `rego.metadata.chain` |  ❌  |  ❌   |    ❌    |
| `rego.metadata.rule`  |  ❌  |  ❌   |    ❌    |
| `rego.parse_module`   |  ❌  |  ❌   |    ❌    |

### Debugging

Note: `print` calls are erased from plans, so likely can't be supported without support in OPA, or through some hack.

| Built-in Function | JVM | Node | Browser |
|-------------------|:---:|:----:|:-------:|
| `print`           |  ❌  |  ❌   |    ❌    |

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
| `cast_array`      |  ✅  |  ❌   |    ❌    |
| `cast_set`        |  ✅  |  ❌   |    ❌    |
| `cast_string`     |  ✅  |  ❌   |    ❌    |
| `cast_boolean`    |  ✅  |  ❌   |    ❌    |
| `cast_null`       |  ✅  |  ❌   |    ❌    |
| `cast_object`     |  ✅  |  ❌   |    ❌    |

## Development

### Testing

* `lein test` to run all tests
* `lein test :unit` to run the Clojure unit tests
* `lein test :compliance` to run the OPA compliance test suite

#### Compliance Tests

Jarl uses plans generated from the "[YAML test suite](https://github.com/open-policy-agent/opa/tree/main/test/cases/testdata)"
of OPA in order to ensure that the result of running a plan via Jarl is the same as it would be when running the
corresponding policy in OPA. This ensures that we stay compliant with at least the documented behavior of OPA. Now,
there are obviously tons of things not tested there, but having more than 1000 tests at least provides some level of
assurance. When encountering behavior which likely differs between OPA and Jarl, it is recommended to consider adding a
test for that to the OPA test suite, and then have it added to Jarl via the [opa-compliance-test](https://github.com/johanfylling/opa-compliance-test)
tool.

In order to run the compliance tests, these must first be **generated**. This process entails taking the JSON files
found under `src/test/resources/compliance` and turning them into Clojure/ClojureScript test cases — a process done by
the [compliance test generator](./src/test/clojure/test/compliance/generator.clj). Running the generator is as easy as
executing the main function in that namespace, which can conveniently be done with:

```shell
lein gen-compliance
```
If you only want to generate tests for a single target, you may optionally provide either `:clj` or `:cljs` as an
argument to the above command.

NOTE that you need only to generate the compliance tests cases when there have been either changes coming in upstream,
or you have added new builtin functions for either target.

### Linters

* `lein kibit` to run [kibit](https://github.com/jonase/kibit)
* `lein eastwood` ro run [eastwood](https://github.com/jonase/eastwood)
* `lein clj-kondo --lint src test` to run [clj-kondo](https://github.com/clj-kondo/clj-kondo)

### ClojureScript

Jarl currently has experimental support for ClojureScript as an alternative to Clojure. In order to avoid duplication of
code, we use `.cljc` files and [reader conditionals](https://clojure.org/guides/reader_conditionals) where possible, and
try to use `.clj` (Clojure) and `.cljs` (ClojureScript) files only when a particular namespace relies extensively on
features found in the host system (such as functions related to date and time).

Most notable things currently missing for full ClojureScript support:

* Unit tests don't currently work with the `testingbuiltin` macro. As we rely heavily on this macro, fixing this should
  be high up on the list of priorities.
* Node is currently assumed as the JS environment. Only a few places rely on Node-specific functionality, and those
  should be clearly isolated as such. Running Jarl both in Node and in the browser is definitely our goal.

#### Building ClojureScript

To build all targets (`main`, `test`, `compliance`):
```shell
lein cljsbuild once
```
The above command may optionally be followed by the name of one of the targets to build only that. Additionally, if 
you're hacking on ClojureScript for a while, you may prefer `lein cljsbuild auto` to have it automatically build your
code on changes. This is _much_ faster than building once.

#### Running ClojureScript

Running Jarl core:
```shell
node target/cljs-main.js
```
Running unit tests:
```shell
node target/cljs-test.js
```
Running compliance tests:
```shell
node target/cljs-compliance.js
```

#### REPL

https://github.com/nrepl/piggieback

```shell
$ lein repl
user=> (require 'cljs.repl.node)
nil
user=> (cider.piggieback/cljs-repl (cljs.repl.node/repl-env))
To quit, type: :cljs/quit
nil
```

## License

Copyright 2022 Johan Fylling, Anders Eknert

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

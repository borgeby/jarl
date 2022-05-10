# Jarl

The _JVM Alternative for the Rego Language_ (JARL) is an OPA evaluator for the JVM, written in Clojure.

## Usage

FIXME

## Supported built-in functions

### Comparison

- [x] `==`
- [x] `!=`
- [x] `<`
- [x] `<=`
- [x] `>`
- [x] `>=`

### Numbers

- [x] `+`
- [x] `-`
- [x] `*`
- [x] `/`
- [x] `%`
- [x] `round`
- [x] `ceil`
- [x] `floor`
- [x] `abs`
- [x] `numbers.range`
- [ ] `rand.intn`

### Aggregates

- [x] `count`
- [x] `sum`
- [x] `product`
- [x] `max`
- [x] `min`
- [x] `sort`

### Arrays

- [x] `array.concat` 
- [x] `array.reverse`
- [x] `array.slice`

### Sets

- [x] `&`
- [x] `|`
- [x] `-`
- [x] `intersection`
- [x] `union`

### Objects

- [x] `object.get`
- [x] `object.remove`
- [x] `object.union`
- [x] `object.union_n`
- [x] `object.filter`
- [ ] `json.filter`
- [ ] `json.remove`
- [ ] `json.patch`

### Strings

- [x] `concat`
- [x] `contains`
- [x] `endswith`
- [x] `format_int`
- [x] `indexof`
- [x] `indexof_n`
- [x] `lower`
- [x] `replace`
- [x] `strings.reverse`
- [ ] `strings.replace_n`
- [x] `split`
- [ ] `sprintf`
- [x] `startswith`
- [x] `substring`
- [x] `trim`
- [x] `trim_left`
- [x] `trim_prefix`
- [x] `trim_right`
- [x] `trim_suffix`
- [x] `trim_space`
- [x] `upper`

### Regex

https://github.com/google/re2j

- [x] `regex.match`
- [x] `regex.is_valid`
- [x] `regex.split`
- [ ] `regex.globs_match`
- [ ] `regex.template_match`
- [x] `regex.find_n`
- [ ] `regex.find_all_string_submatch_n`

### Types

- [x] `is_number`
- [x] `is_string`
- [x] `is_boolean`
- [x] `is_array`
- [x] `is_set`
- [x] `is_object`
- [x] `is_null`
- [x] `type_name`

### Glob

- [ ] `glob.match`
- [ ] `glob.quote_meta`

### Bitwise

- [x] `bits.or`
- [x] `bits.and`
- [x] `bits.negate`
- [x] `bits.xor`
- [x] `bits.lsh`
- [x] `bits.rsh`

### Encoding

- [x] `base64.encode`
- [x] `base64.decode`
- [x] `base64url.encode`
- [x] `base64url.encode_no_pad`
- [x] `base64url.decode`
- [x] `urlquery.encode`
- [ ] `urlquery.encode_object`
- [x] `urlquery.decode`
- [ ] `urlquery.decode_object`
- [ ] `json.marshal`
- [x] `json.unmarshal`
- [x] `json.is_valid`
- [ ] `yaml.marshal`
- [ ] `yaml.unmarshal`
- [ ] `yaml.is_valid`
- [x] `hex.encode`
- [x] `hex.decode`

## Development

### Linters

* `lein kibit` to run [kibit](https://github.com/jonase/kibit)
* `lein eastwood` ro run [eastwood](https://github.com/jonase/eastwood)
* `lein clj-kondo --lint src test` to run [clj-kondo](https://github.com/clj-kondo/clj-kondo)
* `lein lint` to run all linters

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

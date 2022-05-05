# Jarl

The _JVM Alternative for the Rego Language_ (JARL) is an OPA evaluator for the JVM, written in Clojure.

## Usage

FIXME

## Supported built-in functions

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

### Arrays

- [x] `array.concat` 
- [x] `array.reverse`
- [x] `array.slice`

### Strings

- [ ] `concat`
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

### Types

- [x] `is_number`
- [x] `is_string`
- [x] `is_boolean`
- [x] `is_array`
- [x] `is_set`
- [x] `is_object`
- [x] `is_null`
- [x] `type_name`

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

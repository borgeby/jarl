# Jarl

The _JVM Alternative for the Rego Language_ (JARL) is an OPA evaluator for the JVM, written in Clojure.

## Usage

FIXME

## Supported built-in functions

### Arrays

- [x] `array.concat` 
- [x] `array.reverse`
- [x] `array.slice`

### Strings

- [] `concat`
- [x] `contains`
- [x] `endswith`
- [x] `format_int`
- [x] `indexof`
- [x] `indexof_n`
- [x] `lower`
- [x] `replace`
- [x] `strings.reverse`
- [] `strings.replace_n`
- [x] `split`
- [] `sprintf`
- [x] `startswith`
- [x] `substring`
- [] `trim`
- [] `trim_left`
- [x] `trim_prefix`
- [] `trim_right`
- [x] `trim_suffix`
- [x] `trim_space`
- [x] `upper`

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

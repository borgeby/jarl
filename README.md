# opa4j

An OPA evaluator for the JVM, written in Clojure.

Project name is subject to change.

## Usage

FIXME

## Supported built-in functions

- [x] `array.concat` 
- [ ] `array.reverse`
- [ ] `array.slice`

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

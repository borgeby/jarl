# Jarl

![build](https://github.com/borgeby/jarl/actions/workflows/check.yaml/badge.svg)
[![codecov](https://codecov.io/gh/borgeby/jarl/branch/main/graph/badge.svg?token=PHGLRDWE39)](https://codecov.io/gh/borgeby/jarl)

Jarl is an [Open Policy Agent](https://www.openpolicyagent.org/) (OPA) evaluator for the JVM and Javascript, written in Clojure(Script). Jarl allows you to evaluate policy directly in your JVM language or Javascript environment of choice. See [this blog](https://blog.openpolicyagent.org/i-have-a-plan-exploring-the-opa-intermediate-representation-ir-format-7319cd94b37d) for an introduction to the intermediate representation (IR) format of OPA that Jarl uses for evaluation, and why you may want to consider that as an option.

While originally an abbreviation for the "JVM Alternative for the Rego Language", Javascript support — for both the browser
and Node.js — has been made possible by ClojureScript, and it's not unthinkable that more platforms will be added to the
[support matrix](https://github.com/johanfylling/jarl/blob/main/doc/builtins.md) in the future.

## Usage

Simple example policy compiled to plan by OPA, and executed by Jarl.

**policy.rego**
```rego
package policy

import future.keywords.if
import future.keywords.in

default allow := false

# METADATA
# entrypoint: true
allow if "admin" in input.user.roles
```
```shell
opa build --target plan policy.rego
```

We now have a bunde containing the `plan.json` file that we may submit for execution by Jarl!

Before we do, we'll need to create an input to use for evaluation.

**input.json**
```json
{
    "user": {
        "roles": [
            "admin"
        ]
    }
}
```

**Evaluate**
```shell
lein run bundle.tar.gz --input input.json
```

**Output**
```json
[{"result":true}]
```

Note that the above constitutes a simple flow for development and testing only. Production artifacts, or a fixed API for
integrations, will be here at a later point in time.

### Clojure

In the `core` directory, run `lein repl` to launch a REPL in the `jarl.core` namespace:

```clojure
(jarl.logging/set-log-level :warn)

(def input {"user" {"roles" ["admin"]}})
(def data {})

(-> (slurp "path/to/plan.json")
    (parser/parse-json)
    (eval-plan "policy/allow" input data))

; [{"result" true}]
```

### Java

Jarl provides a simple API for evaluating policies.

#### Evaluating a Plan

```java
var file = new File("path/to/plan.json");
var input = Map.of("user", "alice");
Map<String, ?> data = Map.of();
var allowed = Jarl.builder(file)
        .build()
        .getPlan("my_policy/allow")
        .eval(input, data)
        .allowed();
if (allowed) {
  ...
}
```

## Built-in Functions

While still in an early stage of development, Jarl already supports [most of the built-in functions](doc/builtins.md)
provided by OPA. Jarl intends not just to support all built-in functions out of the box, but to make it trivial to
implement custom built-in functions for any platform, or even replace existing implementations with custom ones.

## Development

See the [development guide](doc/development.md).


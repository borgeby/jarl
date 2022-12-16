# Jarl Development

## Testing

### Unit Tests

* `lein test` to run all Clojure tests
* `lein test :unit` to run the Clojure unit tests
* `lein cljs-build-test` to build the ClojureScript unit tests, and `node target/cljs-test.js` to run them

### Compliance Tests

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
or you have added new builtin functions for either target. Once generated, the tests may be run with the
following commands:

* `lein test :compliance` to run the OPA compliance test suite for Clojure
* `lein cljs-build-compliance` to build the OPA compliance test suite for ClojureScript, and `node target/cljs-compliance.js` to run it

### Benchmarking

This project has limited benchmarking capabilities, and can measure relative performance for both Jarl and OPA over time.
In support of this, there are two commands:

* `lein perf-jarl`: Will benchmark Jarl, appending the results to `perf/jarl-perf.csv`; labeling them with the current git revision and label (if any).

  The `Benchmark Jarl` GitHub action will run this command and create a PR with the result. This action can be run manually but is also run on pushed tags.
* `lein perf-opa`: Will benchmark the OPA version pointed to by the `OPA` environment variable, alternatively the OPA version found on `PATH`. The result is appended to `perf/opa-perf.csv`, with the OPA version as label.

  The `Benchmark OPA` GitHub action will run this command and create a PR with the result. This action can be run manually, and takes the OPA version as input parameter.

`perf/perf.html` is a simple Web page that illustrates the data in `perf/jarl-perf.csv` and `perf/opa-perf.csv` in chart form.
A live version of this page is published [here](https://johanfylling.github.io/jarl/perf/perf.html).

The above benchmarking commands utilize the IR generated by `test/resources/rego/perf/policy.rego`, where a metric is generated for each contained entrypoint/plan.

### Profiling

Run the code in `core/src/test/clj/test/profile.clj`. Alternatively, from a REPL, run the following in order to have a profiler instrument a function (like the compliance test runner below):

```clojure
(require '[clojure.test :refer [run-tests]]
         '[clj-async-profiler.core :as prof]
         '[test.compliance.generated.tests])

(prof/profile (run-tests 'test.compliance.generated.tests))
(prof/serve-ui 8888)
```

Note that the JVM process needs to be started with `-Djdk.attach.allowAttachSelf` in order for profiling to work.

## Linters

* `lein kibit` to run [kibit](https://github.com/jonase/kibit)
* `lein eastwood` ro run [eastwood](https://github.com/jonase/eastwood)
* `lein clj-kondo --lint src test` to run [clj-kondo](https://github.com/clj-kondo/clj-kondo)

## ClojureScript

Jarl currently has experimental support for ClojureScript as an alternative to Clojure. In order to avoid duplication of
code, we use `.cljc` files and [reader conditionals](https://clojure.org/guides/reader_conditionals) where possible, and
try to use `.clj` (Clojure) and `.cljs` (ClojureScript) files only when a particular namespace relies extensively on
features found in the host system (such as functions related to date and time).

### Building ClojureScript

To build a target (`main`, `test`, `compliance`) using lein:
```shell
lein cljs-build-<target>
```
Or, using the `shadow-cljs` command direcly:
```shell
shadow-cljs compile main
```

### Running ClojureScript

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

### REPL

See the shadow-cljs [docum(entation](https://shadow-cljs.github.io/docs/UsersGuide.html) for how to launch a REPL,
and how to connect to it from your editor of choice.

For IntelliJ IDEA and Cursive, the flow would be:

1. Start a watch: `shadow-cljs watch main`
2. Run the application: `node target/cljs-main.js`
3. IntelliJ Run Configuration: Remote REPL, Connection Type -> nREPL, User port from nREPL file — Use standard port
   file.
4. Inside of REPL: `(shadow/repl :main)`

## Logging

Jarl uses [timbre](https://github.com/ptaoussanis/timbre) for logging, with
[configuration]https://github.com/ptaoussanis/timbre#configuration provided as a regular Clojure(Script) map. This map
may be modified using any of the available methods listed in the docs. The default level is `:warn`, which should not
log anything unless something unexpected happens.

When running the Clojure tests (`lein test :unit`, `lein test :compliance`), you may change the level provided in the
`project.clj` file under the `:test` profile. For ClojureScript, you'll currently need to modify the level in the
`test.config` namespace before building, as this file is sourced into the cljs tests.
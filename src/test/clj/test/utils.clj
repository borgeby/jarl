(ns test.utils
  (:require [clojure.string :as str]
            [jarl.builtins.registry]
            [jarl.exceptions :as errors]
            [jarl.types :as types]))

(defmacro testing-builtin
  "Given the name of a builtin (as named in OPA, e.g. 'base64.encode'),
  and a series of inputs to use when calling the function, and the output
  to expect, i.e.:

  (testing-builtin \"count\"
    ; args-list    expected
    [#{1 2 3}]     3
    [(range 5)]    5)

  Expands to:

  (testing \"count\"
    (is (= (builtin-count {:args [#{1 2 3}]}) 3)
    (is (= (builtin-count {:args [(range 5)]}) 5))

  Expected exception may be tried using a vector with first element as the exception type, and the second element
  a string that will be compiled to a regex for a match on the error message:

  (testing \"sum\"
    [2 0] [BuiltinException \"divide by zero\"]))"
  [name & forms]
  (let [cljs? (some? (:ns &env))
        is (if cljs? 'cljs.test/is      'clojure.test/is)
        testing  (if cljs? 'cljs.test/testing 'clojure.test/testing)
        func-name (str "builtin-" (str/replace name #"[\._]" "-"))
        pairs (partition 2 forms)
        stmts (map (fn [[args expect]]
                     (if (vector? expect)
                       (if (isa? (first expect) :jarl.exceptions/jarl-exception)
                         (let [ex-type (errors/rego-type (first expect))
                               expect-pattern (re-pattern (second expect))]
                           `(let [provided-ex# (jarl.exceptions/try-return #((jarl.builtins.registry/get-builtin ~name) {:args ~args}))
                                  provided-msg# (ex-message provided-ex#)]
                              (~is (= (jarl.exceptions/rego-type provided-ex#) ~ex-type))
                              (~is (re-find ~expect-pattern provided-msg#)
                                (str "Expected match for pattern '" ~expect-pattern "' but '" provided-msg# "' does not match"))))
                         ; else - not exception
                         `(~is (types/rego-equal? ((jarl.builtins.registry/get-builtin ~name) {:args ~args}) ~expect)))
                       ; else - not vector
                       (if (map? args)
                         ; allow providing the entire request map if needed
                         `(~is (types/rego-equal? ((jarl.builtins.registry/get-builtin ~name) ~args) ~expect)
                            (str "For input: " ~args))
                         ; but since most tests only care for the args, a vector of those is more convenient
                         `(~is (types/rego-equal? ((jarl.builtins.registry/get-builtin ~name) {:args ~args}) ~expect)
                            (str "For input: " ~args)))))
                   pairs)]
    `(~testing ~func-name ~@stmts)))

(ns test.utils
  (:require [clojure.test :refer [is]]
            [clojure.string :as str]
            [jarl.exceptions :as errors]
            [jarl.builtins.registry :as registry]))

; inspired by https://gist.github.com/joelittlejohn/2ecc1256e5d184d78f30fd6c4641099e

(defn add-test
  "Add a test to the given namespace."
  [name ns test-fn & [metadata]]
  (intern ns (with-meta (symbol name) (merge metadata {:test #(test-fn)})) (fn [])))

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
  (let [func (registry/get-builtin name)
        func-name (str "builtin-" (str/replace name #"[\._]" "-"))
        pairs (partition 2 forms)
        stmts (map (fn [[args expect]]
                     (if (vector? expect)
                       (if-let [expect-ex (errors/resolve-jarl-exception (first expect))]
                         (let [ex-type (errors/rego-ex-type-from-class expect-ex)
                               ex-type-str (if (= ex-type "") "" (str ex-type ": "))
                               expect-pattern (re-pattern (str ex-type-str name ": " (second expect)))
                               provided-ex (try (func {:args args}) (catch Exception e e))]
                           `(do
                              (is (= ~(type provided-ex) ~expect-ex))
                              (is (re-find ~expect-pattern ~(ex-message provided-ex))
                                  ~(str "Expected match for pattern '" expect-pattern "' but '" (ex-message provided-ex) "' does not match"))))
                         ; else - not exception
                         `(is (= (~func {:args ~args}) ~expect)))
                       ; else - not vector
                       `(is (= (~func {:args ~args}) ~expect))))
                   pairs)]
    `(clojure.test/testing ~func-name
       ~@stmts)))

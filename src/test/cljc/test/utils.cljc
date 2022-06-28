(ns test.utils
  (:require #?(:clj  [clojure.test     :refer [is testing]]
               :cljs [cljs.test :refer-macros [is testing]])
            [clojure.string :as str]
            [jarl.exceptions :as errors]
            [jarl.builtins.registry :as registry]))

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
                       (if (isa? (first expect) :jarl.exceptions/jarl-exception)
                         (let [ex-type (errors/rego-type (first expect))
                               ex-type-str (if (= ex-type "") "" (str ex-type ": "))
                               expect-pattern (re-pattern (str ex-type-str name ": " (second expect)))
                               provided-ex (errors/try-return #(func {:args args}))]
                           `(do
                              (is (= ~(errors/rego-type provided-ex) ~ex-type))
                              (is (re-find ~expect-pattern ~(ex-message provided-ex))
                                  ~(str "Expected match for pattern '" expect-pattern "' but '" (ex-message provided-ex) "' does not match"))))
                         ; else - not exception
                         `(is (= (~func {:args ~args}) ~expect)))
                       ; else - not vector
                       (if (map? args)
                         ; allow providing the entire request map if needed
                         `(is (= (~func ~args) ~expect) (str "For input: " ~args))
                         ; but since most tests only care for the args, a vector of those is more convenient
                         `(is (= (~func {:args ~args}) ~expect) (str "For input: " ~args)))))
                   pairs)]
    `(testing ~func-name
       ~@stmts)))

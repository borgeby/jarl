(ns jarl.builtins.opa)

(defn builtin-opa-runtime
  "Implementation of opa.runtime built-in"
  {:builtin "opa.runtime" :args-types []}
  [{bctx :builtin-context}]
  {"commit" ""
   ; "config" key not present unless running as server - implement later
   "env" (:env bctx)
   ; use Jarl version here later
   "version" "jarl-dev"})

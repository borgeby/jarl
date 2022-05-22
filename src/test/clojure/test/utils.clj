(ns test.utils
  (:require [clojure.test :refer :all]))

; inspired by https://gist.github.com/joelittlejohn/2ecc1256e5d184d78f30fd6c4641099e

(defn add-test
  "Add a test to the given namespace."
  [name ns test-fn & [metadata]]
  (intern ns (with-meta (symbol name) (merge metadata {:test #(test-fn)})) (fn [])))
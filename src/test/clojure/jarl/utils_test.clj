(ns jarl.utils-test
  (:require [clojure.test :refer [deftest testing is]]
            [jarl.utils :refer [indexed-map->array]]))

(deftest indexed-map->array-test
  (testing "indexed-map->array"
    (is (= (indexed-map->array {}) []))
    (is (= (indexed-map->array {5 "foo"}) [nil nil nil nil nil "foo"]))
    (is (= (indexed-map->array {0 "a" 3 "d"}) ["a" nil nil "d"]))))

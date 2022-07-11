(ns jarl.utils-test
  (:require #?(:clj  [clojure.test :refer [deftest testing is]]
               :cljs [cljs.test :refer [deftest testing is]])
            [jarl.utils :refer [indexed-map->vector]]))

(deftest indexed-map->array-test
  (testing "indexed-map->array"
    (is (= (indexed-map->vector {}) []))
    (is (= (indexed-map->vector {5 "foo"}) [nil nil nil nil nil "foo"]))
    (is (= (indexed-map->vector {0 "a" 3 "d"}) ["a" nil nil "d"]))))

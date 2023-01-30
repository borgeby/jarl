(ns jarl.builtins.net-test
  (:require [clojure.test :refer [deftest testing is]]
            [jarl.builtins.net :as net]))

(deftest net-lookup-ip-addr-test
  (testing "net.lookup_ip_addr"
    (with-redefs [net/lookup (fn [_] '("23.185.0.2" "2620:12a:8000:0:0:0:0:2" "2620:12a:8001:0:0:0:0:2"))]
      (is (= (net/builtin-net-lookup-ip-addr {:args ["example.org"]}) #{"23.185.0.2"
                                                                        "2620:12a:8000::2"
                                                                        "2620:12a:8001::2"})))))

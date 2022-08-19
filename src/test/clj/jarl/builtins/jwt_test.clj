(ns jarl.builtins.jwt-test
  (:require [clojure.test :refer [deftest testing is]]
            [jarl.builtins.jwt :as jwt]
            [jarl.utils :refer [time-now-ns]])
  (:import (java.util.concurrent TimeUnit)))

; some basic sanity tests here only - mainly tested by the compliance suite

(deftest jwt-verification-test
  (testing "hs256"
    (is (= (jwt/builtin-io-jwt-verify-hs256
             {:args
              ["eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.VfiYwvArp2lNV6UgpwgqrqfbJp9QpMdv07M8ZI4u4Vw"
               "foobar"]})
           true))
    (is (= (jwt/builtin-io-jwt-verify-hs256
             {:args
              ["eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.VfiYwvArp2lNV6UgpwgqrqfbJp9QpMdv07M8ZI4u4Vw"
               "bar"]})
           false))))

(deftest decode-test
  (testing "decode"
    (is (= (jwt/decode "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.VfiYwvArp2lNV6UgpwgqrqfbJp9QpMdv07M8ZI4u4Vw")
           [{"alg" "HS256" "typ" "JWT"}
            {"iat" 1516239022 "name" "John Doe" "sub"  "1234567890"}
            "55f898c2f02ba7694d57a520a7082aaea7db269f50a4c76fd3b33c648e2ee15c"]))))

(defn- nanos->seconds [time]
  (.convert TimeUnit/SECONDS time TimeUnit/NANOSECONDS))

(deftest verify-time-test
  (testing "nbf and exp"
    (let [now (time-now-ns)
          nbf (dec (nanos->seconds now))
          exp (inc (nanos->seconds now))]
      (is (= (jwt/verify-time {"exp" exp "nbf" nbf} {"time" now}) true)))))

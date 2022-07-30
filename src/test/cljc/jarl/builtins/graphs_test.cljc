(ns jarl.builtins.graphs-test
  (:require  [test.utils   :refer [testing-builtin]]
             #?(:clj  [clojure.test :refer [deftest testing]]
                :cljs [cljs.test    :refer [deftest testing]])))

(deftest builtin-graph-reachable-test
  (testing "simple graph"
    (let [org-chart-graph {"ceo"             ["human_resources"]
                           "human_resources" ["staffing"]
                           "staffing"        ["internships"]
                           "internships"     []}]

      (testing-builtin "graph.reachable"
        [org-chart-graph #{"ceo"}]             #{"ceo" "human_resources" "internships" "staffing"}
        [org-chart-graph #{"human_resources"}] #{      "human_resources" "internships" "staffing"}
        [org-chart-graph #{"staffing"}]        #{                        "internships" "staffing"}
        [org-chart-graph #{"internships"}]     #{                        "internships"           })))

  (testing "simple graph using sets"
    (let [org-chart-graph {"ceo"             #{"human_resources"}
                           "human_resources" #{"staffing"}
                           "staffing"        #{"internships"}
                           "internships"     #{}}]

      (testing-builtin "graph.reachable"
        [org-chart-graph #{"ceo"}]             #{"ceo" "human_resources" "internships" "staffing"}
        [org-chart-graph #{"human_resources"}] #{      "human_resources" "internships" "staffing"}
        [org-chart-graph #{"staffing"}]        #{                        "internships" "staffing"}
        [org-chart-graph #{"internships"}]     #{                        "internships"           })))

  (testing "multiple children"
    (let [org-chart-graph {"ceo"             ["human_resources" "staffing"]
                           "human_resources" []
                           "staffing"        ["internships" "janitors"]
                           "internships"     []
                           "janitors"        ["cleaners"]
                           "cleaners"        []}]

      (testing-builtin "graph.reachable"
        [org-chart-graph #{"ceo"}] #{"ceo" "human_resources" "internships" "staffing" "janitors" "cleaners"})))

  (testing "recursive graph"
    (let [org-chart-graph {"ceo"             ["human_resources" "staffing"]
                           "human_resources" []
                           "staffing"        ["ceo" "janitors"]
                           "internships"     []
                           "janitors"        ["cleaners"]
                           "cleaners"        []}]

      (testing-builtin "graph.reachable"
        [org-chart-graph #{"ceo"}] #{"ceo" "human_resources" "staffing" "janitors" "cleaners"})))

  (testing "multiple initial"
    (let [org-chart-graph {"ceo"             ["staffing"]
                           "human_resources" []
                           "staffing"        []
                           "internships"     []
                           "janitors"        ["cleaners"]
                           "cleaners"        []}]

      (testing-builtin "graph.reachable"
        [org-chart-graph #{"ceo" "janitors"}] #{"ceo" "staffing" "janitors" "cleaners"})))

  (testing "empty graph"
    (testing-builtin "graph.reachable"
      [{} #{"ceo"}] #{})))

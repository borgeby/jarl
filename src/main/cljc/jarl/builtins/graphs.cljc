(ns jarl.builtins.graphs)

(defn- graph-seq [graph root]
  (let [visited (atom #{})] ; needed in order to avoid recursive references in the graph
    (tree-seq
      (fn branch? [node]
        (when (and (string? node)
                   (seq (get graph node))
                   (not (contains? @visited node)))
          (swap! visited conj node)))
      (fn children [node]
        (get graph node))
      root)))

(defn builtin-graph-reachable
  [{[graph initial] :args}]
  (if (empty? graph)
    #{}
    (into #{} (mapcat (partial graph-seq graph) initial))))

(defn builtin-graph-reachable-paths
  [{[graph initial] :args}]
  (println "not yet"))

(defn builtin-walk
  [{[x output] :args}]
  (println "not yet"))

(ns jarl.encoding.yaml
  (:require #?(:clj [clj-yaml.core :as yaml])))

(defn read-str [s]
  #?(:clj  (yaml/parse-string s :keywords false)
     :cljs (throw (ex-info "not implemented yet" {:function "jarl.encoding.yaml/read-str"
                                                  :arg s}))))

(defn write-str [x]
  #?(:clj  ; Notable difference from OPA: this YAML marshaller does not re-order the output, so we'll sort it beforehand
           (yaml/generate-string (cond->> x (map? x) (into (sorted-map))) :dumper-options {:flow-style :block})
     :cljs (throw (ex-info "not implemented yet" {:function "jarl.encoding.yaml/write-str"
                                                  :arg x}))))

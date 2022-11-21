(ns jarl.encoding.yaml
  (:require #?(:clj  [clj-yaml.core :as yaml])
            #?(:cljs ["js-yaml" :as js-yaml])))

(defn read-str [s]
  #?(:clj  (yaml/parse-string s :keywords false)
     :cljs (js->clj (js-yaml/load s))))

(defn write-str [x]
  #?(:clj  ; Notable difference from OPA: this YAML marshaller does not re-order the output, so we'll sort it beforehand
           (yaml/generate-string (cond->> x (map? x) (into (sorted-map))) :dumper-options {:flow-style :block})
     :cljs (js-yaml/dump (clj->js x))))

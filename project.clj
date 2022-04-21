(defproject jarl "0.1.0-SNAPSHOT"
  :description "The JVM Alternative for the Rego Language (JARL)"
  :url "https://github.com/johanfylling/jarl"
  :license {:name "Apache License Version 2.0"
            :url  "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/data.json "2.4.0"]]
  :plugins [[lein-ancient "1.0.0-RC3"]
            [lein-kibit "0.1.8"]
            [jonase/eastwood "1.2.3"]
            [com.github.clj-kondo/lein-clj-kondo "0.1.3"]]
  :repl-options {:init-ns jarl.core}
  :main jarl.core
  :aot [jarl.core jarl.parser jarl.api]
  :direct-linking true
  :aliases {"lint" ["do" ["eastwood"] ["kibit"] ["clj-kondo" "--lint" "src"]]}
  :source-paths ["src/main/clojure"]
  :java-source-paths ["src/main/java"]
  :resource-paths ["src/main/resources"]
  :test-paths ["src/test/clojure"]
  :profiles {:dev  {:dependencies [[junit/junit "4.13.2"]]}
             :test {:java-source-paths ["src/test/java"]
                    :resource-paths    ["src/test/resources"]}}
  )

(defproject jarl "0.1.0-SNAPSHOT"
  :description "Jarl, The JVM Alternative for the Rego Language"
  :url "https://github.com/johanfylling/jarl"
  :license {:name "Apache License Version 2.0"
            :url  "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/data.json "2.4.0"]
                 [org.clojure/tools.logging "1.2.4"]
                 [com.google.re2j/re2j "1.6"]]
  :repl-options {:init-ns jarl.core}
  :main jarl.core
  :aot [jarl.core jarl.parser jarl.api]
  :direct-linking true
  :aliases {"clj-kondo" ["with-profile" "+clj-kondo" "clj-kondo" "--lint" "src"]
            "eastwood" ["with-profile" "+eastwood" "eastwood"]
            "kibit" ["with-profile" "+kibit" "kibit"]}
  :source-paths ["src/main/clojure"]
  :java-source-paths ["src/main/java"]
  :resource-paths ["src/main/resources"]
  :test-paths ["src/test/clojure"]
  :profiles {:dev  {:dependencies [[junit/junit "4.13.2"]]
                    :global-vars  {*warn-on-reflection* true}
                    :plugins      [[lein-ancient "1.0.0-RC3"]]}
             :test {:dependencies      [[org.apache.logging.log4j/log4j-core "2.17.2"]
                                        [org.apache.logging.log4j/log4j-api "2.17.2"]]
                    :java-source-paths ["src/test/java"]
                    :resource-paths    ["src/test/resources"]
                    :jvm-opts          ["-Dclojure.tools.logging.factory=clojure.tools.logging.impl/log4j2-factory"]}
             :clj-kondo {:plugins [[com.github.clj-kondo/lein-clj-kondo "0.1.4"]]}
             :eastwood {:plugins [[jonase/eastwood "1.2.3"]]}
             :kibit {:plugins [[lein-kibit "0.1.8"]]}})

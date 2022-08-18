(defproject jarl "0.1.0-SNAPSHOT"
  :description "Jarl, The JVM Alternative for the Rego Language"
  :url "https://github.com/johanfylling/jarl"
  :license {:name "Apache License Version 2.0"
            :url  "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/clojurescript "1.11.60"]
                 [org.clojure/data.json "2.4.0"]
                 [com.taoensso/timbre "5.2.1"]
                 [com.fzakaria/slf4j-timbre "0.3.21"]
                 [com.google.re2j/re2j "1.7"]
                 [clj-commons/clj-yaml "0.7.108"]
                 [org.bitbucket.b_c/jose4j "0.7.12"]
                 [org.clojure/tools.cli "1.0.206"]]
  :repl-options {:init-ns jarl.core}
  :main jarl.core
  :aot [jarl.core jarl.parser jarl.api]
  :aliases {"clj-kondo"           ["with-profile" "+clj-kondo" "clj-kondo" "--debug" "--parallel" "--lint" "src"]
            "eastwood"            ["with-profile" "+eastwood" "eastwood"]
            "kibit"               ["with-profile" "+kibit" "kibit"]
            "perf-opa"            ["with-profile" "+test" "run" "-m" "test.benchmark" "opa"]
            "perf-jarl"           ["with-profile" "+test" "run" "-m" "test.benchmark" "jarl"]
            "gen-compliance"      ["with-profile" "+test" "run" "-m" "test.compliance.generator"]
            "rebl"                ["trampoline" "run" "-m" "rebel-readline.main"]}
  :source-paths ["src/main/clojure" "src/main/cljc"]
  :java-source-paths ["src/main/java"]
  :resource-paths ["src/main/resources"]
  :test-paths ["src/test/clojure" "src/test/cljc"]
  :test-selectors {:unit (complement :compliance)
                   :compliance       :compliance
                   :performance      :performance}
  :profiles {:dev  {:dependencies   [[junit/junit "4.13.2"]
                                     [com.bhauman/rebel-readline "0.1.4"]
                                     [com.clojure-goes-fast/clj-async-profiler "1.0.0"]
                                     [cider/piggieback "0.5.3"]
                                     [criterium "0.4.6"]
                                     [zprint "1.2.4"]]
                    :resource-paths ["src/test/resources"]
                    :jvm-opts       ["-Djdk.attach.allowAttachSelf"]
                    :repl-options   {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
                    :plugins        [[lein-cljsbuild "1.1.8"]
                                     [lein-ancient "1.0.0-RC3"]
                                     [lein-cloverage "1.2.4"]]
                    :eastwood {:exclude-linters [:constant-test]
                               :exclude-namespaces [test.profile]}}
             :test {:injections [(require 'test.config)
                                 (taoensso.timbre/set-level! :warn)]}
             :clj-kondo {:plugins [[com.github.clj-kondo/lein-clj-kondo "0.2.1"]]}
             :eastwood {:plugins [[jonase/eastwood "1.2.3"]]}
             ; note that kibit currently seems to not support .cljc files well:
             ; https://github.com/jonase/kibit/issues/246
             :kibit {:plugins [[lein-kibit "0.1.8"]]}}
  :cljsbuild
  {:builds
   {:main
    {:source-paths ^:replace ["src/main/clojurescript" "src/main/cljc"]
     :test-paths ["src/test/clojurescript"]
     :compiler {:target :nodejs
                :output-to "target/cljs-main.js"
                :main jarl.core}}
    :test
    {:source-paths ["src/main/clojurescript" "src/main/cljc" "src/test/clojurescript" "src/test/cljc"]
     :test-paths ["src/test/clojurescript" "src/test/cljc"]
     :compiler {:target :nodejs
                :preloads [test.preloads]
                :output-to "target/cljs-test.js"
                :main test.unit-tests}}
    :compliance
    {:source-paths ^:replace ["src/main/clojurescript" "src/main/cljc" "src/test/clojurescript" "src/test/cljc"]
     :test-paths ["src/test/clojurescript" "src/test/cljc"]
     :compiler {:target :nodejs
                :preloads [test.preloads]
                :output-to "target/cljs-compliance.js"
                :main test.compliance-tests}}}
   :test-commands
   {"unit"      ["node" "target/cljs-test.js"]
    "compliance"["node" "target/cljs-compliance.js"]}})

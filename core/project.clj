(defproject jarl "0.1.0-SNAPSHOT"
  :description "Jarl, The JVM Alternative for the Rego Language"
  :url "https://github.com/borgeby/jarl"
  :license {:name "Apache License Version 2.0"
            :url  "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [; clj
                 [org.clojure/clojure "1.11.1"]
                 [org.clojure/data.json "2.4.0"]
                 [com.fzakaria/slf4j-timbre "0.3.21"]
                 [com.google.re2j/re2j "1.7"]
                 [clj-commons/clj-yaml "1.0.26"]
                 [org.bitbucket.b_c/jose4j "0.9.2"]
                 [org.apache.commons/commons-compress "1.22"]
                 [org.clj-commons/clj-http-lite "1.0.13"]
                 ; cljc
                 [org.clojure/tools.cli "1.0.214"]
                 [metosin/malli "0.9.2" :exclusions [org.clojure/tools.reader]]
                 [camel-snake-kebab "0.4.3"]
                 [com.taoensso/timbre "6.0.4"]
                 [by.borge/clj-json-pointer "1.0.0"]
                 [tick "0.5.0"]]
  :repl-options {:init-ns jarl.core}
  :main jarl.core
  :aot [jarl.core jarl.parser jarl.api]
  :javac-options ["-target" "11" "-source" "11"]
  :aliases {"clj-kondo"             ["with-profile" "+clj-kondo" "clj-kondo" "--debug" "--parallel" "--lint" "src"]
            "eastwood"              ["with-profile" "+eastwood" "eastwood"]
            "kibit"                 ["with-profile" "+kibit" "kibit"]
            "perf-opa"              ["with-profile" "+test" "run" "-m" "test.benchmark" "opa"]
            "perf-jarl"             ["with-profile" "+test" "run" "-m" "test.benchmark" "jarl"]
            "gen-compliance"        ["with-profile" "+test" "run" "-m" "test.compliance.generator"]
            "cljs-build-main"       ["with-profile" "+cljs" "run" "-m" "shadow.cljs.devtools.cli" "compile" "main"]
            "cljs-build-test"       ["with-profile" "+cljs" "run" "-m" "shadow.cljs.devtools.cli" "compile" "test"]
            "cljs-build-compliance" ["with-profile" "+cljs" "run" "-m" "shadow.cljs.devtools.cli" "compile" "compliance"]}
  :source-paths ["src/main/cljc" "src/main/clj"]
  :java-source-paths ["src/main/java"]
  :resource-paths ["src/main/resources"]
  :test-paths ["src/test/clj" "src/test/cljc"]
  :test-selectors {:unit (complement :compliance)
                   :compliance       :compliance
                   :performance      :performance}
  :profiles {:dev       {:dependencies   [[com.clojure-goes-fast/clj-async-profiler "1.0.3"]
                                          [criterium "0.4.6"]
                                          [zprint "1.2.4"]
                                          [nrepl "1.0.0"]]
                         :resource-paths ["src/test/resources"]
                         :jvm-opts       ["-Djdk.attach.allowAttachSelf"]
                         :plugins        [[lein-ancient "1.0.0-RC3"]
                                          [lein-cloverage "1.2.4"]]
                         :eastwood       {:exclude-linters    [:constant-test]
                                          :exclude-namespaces [test.profile]}}
             :test      {:injections [(require 'test.config)
                                      (taoensso.timbre/set-level! :warn)]}
             :clj-kondo {:plugins [[com.github.clj-kondo/lein-clj-kondo "0.2.1"]]}
             :eastwood  {:plugins [[jonase/eastwood "1.2.3"]]}
             ; note that kibit currently seems to not support .cljc files well:
             ; https://github.com/jonase/kibit/issues/246
             :kibit     {:plugins [[lein-kibit "0.1.8"]]}
             :cljs      {:dependencies [[org.clojure/clojurescript "1.11.60"
                                         ; more recent version provided by shadow-cljs
                                         :exclusions [com.google.javascript/closure-compiler-unshaded]]
                                        [thheller/shadow-cljs "2.20.14"]]
                         :source-paths ["src/main/cljs" "src/main/cljc" "src/test/cljs" "src/test/cljc"]}})

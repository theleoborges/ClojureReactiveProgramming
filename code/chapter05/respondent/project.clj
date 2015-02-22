(defproject clojure-reactive-programming/respondent "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [org.clojure/clojurescript "0.0-2202"]]

  :plugins [[com.keminglabs/cljx "0.3.2"]
            [lein-cljsbuild "1.0.3"]
            [com.cemerick/clojurescript.test "0.3.0"]]
  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :clj}

                  {:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :cljs}]}
  :hooks [cljx.hooks]

  :profiles {:test {
                    :cljx {:builds [{:source-paths ["test/cljx"]
                                     :output-path "target/test/classes"
                                     :rules :clj}

                                    {:source-paths ["test/cljx"]
                                     :output-path "target/test/classes"
                                     :rules :cljs}]}
                    :cljsbuild
                    {:builds ^:replace [{:source-paths ["target/classes" "target/test/classes"]
                                         :compiler {:output-to "target/test.js"}}]
                     :test-commands {"unit-tests" ["phantomjs" :runner "target/test.js"]}}}}


  :cljsbuild
  {:builds [{:source-paths ["target/classes"]
             :compiler {:output-to "target/main.js"}}]})

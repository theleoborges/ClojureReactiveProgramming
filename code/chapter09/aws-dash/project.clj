(defproject aws-dash "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :min-lein-version "2.3.4"

  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [om "0.5.0"]
                 [com.facebook/react "0.9.0"]
                 [cljs-http "0.1.20"]
                 [com.cognitect/transit-cljs "0.8.192"]]

  :plugins [[lein-cljsbuild "1.0.3"]]

  :hooks [leiningen.cljsbuild]

  :cljsbuild
  {:builds {:aws-dash
            {:source-paths ["src/cljs"]
             :compiler
             {:output-to "dev-resources/public/js/aws_dash.js"
              :optimizations :advanced
              :pretty-print false}}}})

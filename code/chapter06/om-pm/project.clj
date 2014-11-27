(defproject om-pm "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :min-lein-version "2.3.4"

  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2277"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]
                 [om "0.7.1"]
                 [com.facebook/react "0.11.1"]]

  :plugins [[lein-cljsbuild "1.0.3"]]

  :hooks [leiningen.cljsbuild]

  :cljsbuild
  {:builds {:om-pm
            {:source-paths ["src/cljs"]
             :compiler
             {:output-to "dev-resources/public/js/om_pm.js"
              :optimizations :advanced
              :pretty-print false}}}})

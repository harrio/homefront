(defproject homefront "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.4"]
                 [ring-server "0.3.0"]
                 [serial-port "1.1.2"]
                 [liberator "0.11.0"]
                 [cheshire "5.3.1"]
                 [clj-time "0.6.0"]
                 [lib-noir "0.8.1"]]
  :dev-dependencies [[lein-eclipse "1.0.0"]]
  :plugins [[lein-ring "0.8.7"] [no-man-is-an-island/lein-eclipse "2.0.0"]]
  :ring {:handler homefront.handler/app
         :init homefront.handler/init
         :destroy homefront.handler/destroy}
  :aot :all
  :profiles
  {:production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}}
   :dev
   {:dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.2.0"]]}})

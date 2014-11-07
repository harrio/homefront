(defproject homefront "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :repl-options {:init-ns homefront.repl}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.4"]
                 [ring-server "0.3.0"]
                 [liberator "0.11.0"]
                 [cheshire "5.3.1"]
                 [clj-time "0.6.0"]
                 [lib-noir "0.8.1"]
                 [http-kit "2.1.16"]
                 [ring-basic-authentication "1.0.5"]
                 [ring/ring-json "0.3.0"]
                 [korma "0.3.0"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]
                 [prismatic/schema "0.3.1" :exclusions [org.clojure/tools.reader]]
                 [org.clojure/tools.reader "0.8.8"]
                 [environ "1.0.0"]
                 [silasdavis/at-at "1.2.0"]]
  :dev-dependencies [[lein-eclipse "1.0.0"]]
  :plugins [[lein-ring "0.8.7"] [no-man-is-an-island/lein-eclipse "2.0.0"]]
  :ring {:handler homefront.handler/app
         :init homefront.handler/init
         :destroy homefront.handler/destroy}
  :aot :all
  :main homefront.main
  :uberjar-name "homefront.jar"
  :profiles
  {:production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}}
   :dev
   {:dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.2.0"]]}})

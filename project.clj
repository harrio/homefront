(defproject homefront "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :repl-options {:init-ns homefront.repl}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [hiccup "1.0.5"]
                 [ring-server "0.4.0"]
                 [liberator "0.14.1"]
                 [cheshire "5.6.3"]
                 [clj-time "0.12.0"]
                 [lib-noir "0.9.9"]
                 [http-kit "2.2.0"]
                 [ring-basic-authentication "1.0.5"]
                 [ring/ring-json "0.4.0"]
                 [korma "0.4.3"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]
                 [prismatic/schema "1.1.3" :exclusions [org.clojure/tools.reader]]
                 [org.clojure/tools.reader "0.10.0"]
                 [environ "1.1.0"]
                 [silasdavis/at-at "1.2.0"]
                 [com.draines/postal "2.0.1"]
                 [clojurewerkz/machine_head "1.0.0-beta8"]
                 [com.taoensso/timbre "4.7.4"]
                 [clj-http "3.2.0"]]
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
    {:open-browser? false, :stacktraces? true, :auto-reload? false}}
   :dev
   {:env {:homefront-user "foo" :homefront-pwd "bar"}
    :dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.5.0"]]}})

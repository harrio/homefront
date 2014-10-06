(ns homefront.handler
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [cheshire.core :refer :all]
            [homefront.routes.home :refer :all]
            [ring.middleware.basic-authentication :refer :all]
            [ring.middleware.json :refer :all]
            [environ.core :refer [env]]))

(def user
  (env :homefront-user))

(def password
  (env :homefront-pwd))

(defn read-config []
  (parse-stream (clojure.java.io/reader (clojure.java.io/resource "config.json"))))

(defn init []
  (println "homefront is starting")
  )

(defn destroy []
  (println "homefront is shutting down")
  )

(defn authenticated? [name pass]
  (and (= name user)
       (= pass password)))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (routes home-routes app-routes)
      (handler/site)
      (wrap-base-url)
      (wrap-basic-authentication authenticated?)
      (wrap-json-body)))



(ns homefront.handler
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [homefront.routes.home :refer [home-routes]]
            [homefront.sensor :refer :all]))

(defn init []
  (println "homefront is starting")
  (start-serial)
  )

(defn destroy []
  (println "homefront is shutting down")
  (stop-serial)
  )

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (routes home-routes app-routes)
      (handler/site)
      (wrap-base-url)))



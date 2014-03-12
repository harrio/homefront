(ns homefront.routes.home
  (:require [compojure.core :refer :all]
            [liberator.core :refer [defresource resource request-method-in]]
            [cheshire.core :refer [generate-string]]
            [noir.io :as io]
            [clojure.java.io :refer [file]]
            [homefront.sensor :refer [temps]]))

(defresource home
  :available-media-types ["text/html"]
  :exists?
  (fn [context]
    [(io/get-resource "/index.html")
     {::file (file (str (io/resource-path) "/index.html"))}])
  :handle-ok
  (fn [{{{resource :resource} :route-params} :request}]
    (clojure.java.io/input-stream (io/get-resource "/index.html")))
  :last-modified
   (fn [{{{resource :resource} :route-params} :request}]
    (.lastModified (file (str (io/resource-path) "/index.html")))))

(defresource get-temps
  :allowed-methods [:get]
  :handle-ok (fn [_] generate-string (:data @temps))
  :available-media-types ["application/json"])

(defroutes home-routes
  (ANY "/" request home)
  (ANY "/temps" request get-temps))

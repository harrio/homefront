(ns homefront.routes.home
  (:require [compojure.core :refer :all]
            [liberator.core :refer [defresource resource request-method-in]]
            [cheshire.core :refer [generate-string]]
            [cheshire.generate :refer :all]
            [clj-time.format :refer :all]
            [noir.io :as io]
            [clojure.java.io :refer [file]]
            [homefront.sensor :refer :all]))


(add-encoder org.joda.time.DateTime 
             (fn [dt jsonGenerator] (.writeString jsonGenerator (unparse (formatters :basic-date-time) dt))))

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

(defresource get-sensors
  :allowed-methods [:get]
  :handle-ok (fn [_] (generate-string (get-sensor-data)))
  :available-media-types ["application/json"])

(defroutes home-routes
  (ANY "/" request home)
  (ANY "/sensors" request get-sensors)
  (POST "/saveData" {body :body} (println (slurp body))))

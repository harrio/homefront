(ns homefront.routes.home
  (:require [compojure.core :refer :all]
            [liberator.core :refer [defresource resource request-method-in]]
            [cheshire.core :refer [generate-string]]
            [cheshire.generate :refer :all]
            [clj-time.format :refer :all]
            [noir.io :as io]
            [clojure.java.io :refer [file]]
            [homefront.sensor :refer :all]
            [homefront.db :refer :all]
            monger.json))


(add-encoder org.joda.time.DateTime 
             (fn [dt jsonGenerator] (.writeString jsonGenerator (unparse (formatters :basic-date-time) dt))))

(def time-formatter (formatter "dd.MM.yyyyhh:mm:ss"))

(defn parse-time [time-str]
  (parse time-str))

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
  :handle-ok (fn [ctx] 
               (let [start-time (get-in ctx [:request :params :start])
                     end-time (get-in ctx [:request :params :end])]
                 (println "get sensors" start-time end-time)
                 (generate-string (get-grouped-sensor-data (parse-time start-time) (parse-time end-time)))))
  :available-media-types ["application/json"])

(defresource save-data
  :allowed-methods [:post]
  :post! (fn [ctx]
           (let [body (get-in ctx [:request :body])]
             (insert-sensor-json body)))
  :available-media-types ["application/json"])

(defroutes home-routes
  (ANY "/" request home)
  (GET "/sensors" request get-sensors)
  (POST "/saveData" request save-data))

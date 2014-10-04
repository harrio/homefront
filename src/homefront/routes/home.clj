(ns homefront.routes.home
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [liberator.core :refer [defresource resource request-method-in]]
            [cheshire.core :refer [generate-string]]
            [cheshire.generate :refer :all]
            [clj-time.format :refer :all]
            [noir.io :as io]
            [clojure.java.io :refer [file]]
            [homefront.db :refer [find-sensors
                                  get-grouped-sensor-data
                                  get-single-sensor-data
                                  insert-sensor-data-json
                                  save-sensor-json
                                  remove-sensor]]
            [homefront.database :refer :all]
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

(defresource sensors
  :allowed-methods [:get]
  :handle-ok (fn [ctx]
               (generate-string (get-sensors)))
  :available-media-types ["application/json"])

(defresource sensor-data
  :allowed-methods [:get]
  :handle-ok (fn [ctx]
               (let [start-time (get-in ctx [:request :params :start])
                     end-time (get-in ctx [:request :params :end])]
                 (println "get sensors" start-time end-time)
                 (generate-string (get-sensors-with-data (parse-time start-time) (parse-time end-time)))))
  :available-media-types ["application/json"])

(defresource single-sensor-data
  :allowed-methods [:get]
  :handle-ok (fn [ctx]
               (let [mac (get-in ctx [:request :params :mac])
                     start-time (get-in ctx [:request :params :start])
                     end-time (get-in ctx [:request :params :end])]
                 (println "get sensors" mac start-time end-time)
                 (generate-string (get-single-sensor-data mac (parse-time start-time) (parse-time end-time)))))
  :available-media-types ["application/json"])

(defresource save-data
  :allowed-methods [:post]
  :post! (fn [ctx]
           (let [body (get-in ctx [:request :body])]
             (insert-sensor-data-json body)))
  :handle-ok "ok"
  :available-media-types ["application/json"])

(defresource save-sensor
  :allowed-methods [:post]
  :post! (fn [ctx]
           (let [body (get-in ctx [:request :body])]
             (save-sensor-json body)))
  :handle-ok "ok"
  :available-media-types ["application/json"])

(defresource delete-sensor [sensor]
  :allowed-methods [:delete]
  :delete! (remove-sensor sensor)
  :handle-ok "ok")

(defroutes home-routes
  (ANY "/" request home)
  (GET "/sensors" request sensors)
  (GET "/sensorData" request sensor-data)
  (GET "/singleSensorData" request single-sensor-data)
  (POST "/saveData" request save-data)
  (POST "/saveSensor" request save-sensor)
  (DELETE "/deleteSensor/:sensor" [sensor] (delete-sensor sensor)))


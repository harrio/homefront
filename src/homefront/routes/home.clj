(ns homefront.routes.home
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [liberator.core :refer [defresource resource request-method-in]]
            [cheshire.core :refer [generate-string]]
            [cheshire.generate :refer :all]
            [clj-time.format :refer :all]
            [noir.io :as io]
            [clj-time.core :as time]
            [clojure.java.io :refer [file]]
            [homefront.db.data :refer :all]
            [homefront.db.admin :refer :all]
            [homefront.db.cat :as cat]))


(add-encoder org.joda.time.DateTime
             (fn [dt jsonGenerator] (.writeString jsonGenerator (unparse (formatters :date-time-no-ms) dt))))

(def time-formatter (formatter "dd.MM.yyyyhh:mm:ss"))
(def pebble-formatter (formatter-local "dd.MM.yyyy H:mm"))
(def time-formatter-in (formatter "yyyy-MM-dd'T'H:mm:ss.SSSZ"))

(def hel-tz "Europe/Helsinki")

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

(defresource groups
  :allowed-methods [:get]
  :handle-ok (fn [ctx]
               (generate-string (get-groups)))
  :available-media-types ["application/json"])

(defresource sensor-data
  :allowed-methods [:get]
  :handle-ok (fn [ctx]
               (let [start-time (get-in ctx [:request :params :start])
                     end-time (get-in ctx [:request :params :end])]
                 (generate-string (get-sensors-with-data (parse-time start-time) (parse-time end-time)))))
  :available-media-types ["application/json"])

(defresource latest-sensor-data
  :allowed-methods [:get]
  :handle-ok (fn [ctx]
                 (generate-string (get-groups-with-latest-data)))
  :available-media-types ["application/json"])

(defresource group-data
  :allowed-methods [:get]
  :handle-ok (fn [ctx]
               (let [start-time (get-in ctx [:request :params :start])
                     end-time (get-in ctx [:request :params :end])]
                 (generate-string (get-groups-with-data (parse-time start-time) (parse-time end-time)))))
  :available-media-types ["application/json"])

(defresource group-humidity-data
  :allowed-methods [:get]
  :handle-ok (fn [ctx]
               (let [start-time (get-in ctx [:request :params :start])
                     end-time (get-in ctx [:request :params :end])]
                 (generate-string (get-groups-with-humidity-data (parse-time start-time) (parse-time end-time)))))
  :available-media-types ["application/json"])


(defresource save-data
  :allowed-methods [:post]
  :post! (fn [ctx]
           (let [body (get-in ctx [:request :body])]
             (insert-sensor-data body)))
  :handle-ok "ok"
  :available-media-types ["application/json"])

(defresource save-sensor
  :allowed-methods [:post]
  :post! (fn [ctx]
           (let [body (get-in ctx [:request :body])]
             (save-sensor-db body)))
  :handle-ok "ok"
  :available-media-types ["application/json"])

(defresource delete-sensor [sensor]
  :allowed-methods [:delete]
  :delete! (remove-sensor sensor)
  :handle-ok "ok")

(defresource save-group
  :allowed-methods [:post]
  :post! (fn [ctx]
           (let [body (get-in ctx [:request :body])]
             (save-group-db body)))
  :handle-ok "ok"
  :available-media-types ["application/json"])

(defresource delete-group [group]
  :allowed-methods [:delete]
  :delete! (remove-group group)
  :handle-ok "ok")

(defresource feed-cat
             :allowed-methods [:get]
             :handle-ok (fn [ctx]
                          (str (or (cat/feed-cat?) "0")))
             :available-media-types ["text/plain"])

(defresource cat-fed [id]
             :allowed-methods [:get]
             :handle-ok (fn [ctx]
                          (cat/cat-fed (read-string id))
                          "0")
             :available-media-types ["text/plain"])

(defresource fed-cat
             :allowed-methods [:get]
             :handle-ok (fn [ctx]
                          (cat/cat-fed (read-string (get-in ctx [:request :params :q])))
                          "0")
             :available-media-types ["text/plain"])

(defresource feedings
             :allowed-methods [:get]
             :handle-ok (fn [ctx]
                          (generate-string (cat/get-feedings)))
             :available-media-types ["application/json"])

(defresource next-feeding
             :allowed-methods [:get]
             :handle-ok (fn [ctx]
                          (let [f (cat/get-next-feeding)]
                            (generate-string
                              (if f
                                (update-in f [:time] (fn [time]
                                                       (let [zoned (time/to-time-zone time (time/time-zone-for-id hel-tz))]
                                                         (unparse pebble-formatter zoned))))
                                f)
                              )))
             :available-media-types ["application/json"])

(defresource last-feeding
             :allowed-methods [:get]
             :handle-ok (fn [ctx]
                          (let [f (cat/get-last-ready-feeding)]
                            (generate-string
                              (if f
                                (update-in f [:time]
                                           (fn [time]
                                             (let [zoned (time/to-time-zone time (time/time-zone-for-id hel-tz))]
                                               (unparse pebble-formatter zoned))))
                                f)
                              )))
             :available-media-types ["application/json"])

(defresource save-feeding
             :allowed-methods [:post]
             :post! (fn [ctx]
                      (let [body (get-in ctx [:request :body])]
                        (if (:time body)
                          (cat/insert-feeding (parse time-formatter-in (:time body)))
                          (cat/insert-feeding (time/plus (time/now) (time/hours (read-string (:offset body))))))))
             :handle-ok "ok"
             :available-media-types ["application/json"])

(defresource cat-heartbeat
             :allowed-methods [:get]
             :handle-ok (fn [ctx]
                          (generate-string (cat/get-cat-heartbeat)))
             :available-media-types ["application/json"])

(defroutes home-routes
  (ANY "/" request home)
  (GET "/sensors" request sensors)
  (GET "/groups" request groups)
  (GET "/sensorData" request sensor-data)
  (GET "/groupData" request group-data)
  (GET "/groupHumidityData" request group-humidity-data)
  (POST "/saveData" request save-data)
  (POST "/saveSensor" request save-sensor)
  (DELETE "/deleteSensor/:sensor" [sensor] (delete-sensor sensor))
  (POST "/saveGroup" request save-group)
  (DELETE "/deleteGroup/:group" [group] (delete-group group))
  (GET "/feedings" request feedings)
  (GET "/nextFeeding" request next-feeding)
  (GET "/lastFeeding" request last-feeding)
  (POST "/saveFeeding" request save-feeding)
  (GET "/catHeartbeat" request cat-heartbeat))

(defroutes open-routes
           (GET "/latestSensorData" request latest-sensor-data)
           (GET "/feedCat" request feed-cat)
           (GET "/catFed/:id" [id] (cat-fed id))
           (GET "/fedCat" request fed-cat))


(ns homefront.scheduler
  (:require [overtone.at-at :as at]
            [clj-http.client :as client]
            [cheshire.core :refer [parse-string]]
            [postal.core :refer [send-message]]
            [homefront.db.data :refer [insert-sensor-data get-dead-sensors get-floor-temps get-floor-hums]]
            [homefront.mail :refer [send-probes-dead]]
            [homefront.mqtt :refer [publish-data]]))

(def at-pool (at/mk-pool))

(defn- collect-names [probes]
  (map #(:name %) probes))

(defn- check-dead-sensors []
  (let [dead (get-dead-sensors)]
    (if-not (empty? dead)
      (send-probes-dead (collect-names dead)))))

(defn- publish-mqtt-data []
  (publish-data (get-floor-temps) (get-floor-hums))
  )

(defn- fetch-weather []
  (let [temp (-> (client/get "http://yle.fi/saa/resources/ajax/saa-api/current-weather.action?ids=634963")
                 :body
                 parse-string
                 first
                 (get "temperature"))]
    (insert-sensor-data {:mac "weather" :data [{:key "1" :temp temp}]})))

(defn schedule-probe-watch []
  (at/every (* 60 60 1000) check-dead-sensors at-pool))

(defn schedule-mqtt-publish []
  (at/every (* 60 1000) publish-mqtt-data at-pool))

(defn schedule-weather []
  (at/every (* 60 1000) fetch-weather at-pool))




(ns homefront.scheduler
  (:require [overtone.at-at :as at]
            [postal.core :refer [send-message]]
            [homefront.db.data :refer [get-dead-sensors get-floor-temps get-floor-hums]]
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

(defn schedule-probe-watch []
  (at/every (* 60 60 1000) check-dead-sensors at-pool))

(defn schedule-mqtt-publish []
  (at/every (* 60 1000) publish-mqtt-data at-pool))





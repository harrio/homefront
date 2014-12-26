(ns homefront.scheduler
  (:require [overtone.at-at :as at]
            [postal.core :refer [send-message]]
            [homefront.db.data :refer [get-dead-sensors]]
            [homefront.mail :refer [send-probes-dead]]))

(def at-pool (at/mk-pool))

(defn- collect-names [probes]
  (map #(:name %) probes))

(defn- check-dead-sensors []
  (let [dead (get-dead-sensors)]
    (if-not (empty? dead)
      (send-probes-dead (collect-names dead)))))

(defn schedule-probe-watch []
  (at/every 600000 check-dead-sensors at-pool))





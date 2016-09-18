(ns homefront.main
  (:require [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.3rd-party.rotor :as rotor])
  (:use homefront.handler
        [org.httpkit.server :only [run-server]]
        [homefront.scheduler :refer [schedule-probe-watch schedule-mqtt-publish schedule-weather]]
        [homefront.mqtt :as mqtt])
  (:gen-class))
(defn -main [& [port]]
  (let [port (if port (Integer/parseInt port) 3000)]
    (timbre/set-config!
      {:min-level :debug
       :enabled? true
       :output-fn timbre/default-output-fn})

    (timbre/merge-config!
      {:appenders
       {:rotor (rotor/rotor-appender  {:path "./log/homefront.log"})}})
    #_(timbre/merge-config!
      {:appenders {:spit (appenders/spit-appender {:fname "/home/root/deploy/log/homefront.log"})}})

    (run-server app {:port port})
    (timbre/info (str "Started server on port " port))
    (mqtt/init-mqtt)
    (timbre/info "Starting probe watch")
    (schedule-probe-watch)
    (timbre/info "Starting mqtt publish")
    (schedule-mqtt-publish)
    (schedule-weather)
    ))
  


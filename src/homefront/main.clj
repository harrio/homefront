(ns homefront.main
  (:use homefront.handler
        [org.httpkit.server :only [run-server]]
        [homefront.scheduler :refer [schedule-probe-watch schedule-mqtt-publish]]
        [homefront.mqtt :as mqtt])
  (:gen-class))
(defn -main [& [port]]
  (let [port (if port (Integer/parseInt port) 3000)]
    (run-server app {:port port})
    (println (str "Started server on port " port))
    (mqtt/init-mqtt)
    (println "Starting probe watch")
    (schedule-probe-watch)
    (println "Starting mqtt publish")
    (schedule-mqtt-publish)))
  


(ns homefront.mqtt
  (:require [clojurewerkz.machine-head.client :as mh]
            [environ.core :refer [env]]
            [clj-time.format :refer [formatter-local unparse]]
            [clj-time.core :as time]))

(def mqtt-formatter (formatter-local "ddMMyyyyHHmmss"))
(def hel-tz "Europe/Helsinki")
(def mqtt-addr (str "tcp://" (env :mqtt-host) ":1883"))
(def mqtt-opts {:username (env :mqtt-user) :password (env :mqtt-pwd)})

(defn send-time []
  (let [id (mh/generate-id)
        conn (mh/connect mqtt-addr id mqtt-opts)
        zoned (time/to-time-zone (time/now) (time/time-zone-for-id hel-tz))]
    (mh/publish conn "time" (unparse mqtt-formatter zoned))
    (mh/disconnect conn)
        ))

(defn init-mqtt []
  (let [id   (mh/generate-id)
        conn (mh/connect mqtt-addr id mqtt-opts)]
    (mh/subscribe conn ["timereq"] (fn [^String topic _ ^bytes payload]
                                     (println (String. payload "UTF-8"))
                                     (send-time))))
  )

(defn publish-data [floor-temps floor-hums]
  (let [id (mh/generate-id)
        conn (mh/connect mqtt-addr id mqtt-opts)]
    (doseq [temp floor-temps]
      (mh/publish conn (str "temp/" (:floor temp)) (str (:floor temp) (:temp temp))))
    (doseq [hum floor-hums]
      (mh/publish conn (str "hum/" (:floor hum)) (str (:floor hum) (:hum hum))))
    (mh/disconnect conn)
    ))

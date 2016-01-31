(ns homefront.mqtt
  (:require [clojurewerkz.machine-head.client :as mh]
            [environ.core :refer [env]]
            [clj-time.format :refer [formatter-local unparse]]
            [clj-time.core :as time]
            [cheshire.core :refer [parse-string]]
            [homefront.db.data :refer [insert-sensor-data]]))

(def mqtt-formatter (formatter-local "ddMMyyyyHHmmss"))
(def hel-tz "Europe/Helsinki")
(def mqtt-addr (str "tcp://" (env :mqtt-host) ":1883"))
(def mqtt-opts {:username (env :mqtt-user) :password (env :mqtt-pwd)})

(def mqtt-client (atom nil))

(defn send-time []
  (let [zoned (time/to-time-zone (time/now) (time/time-zone-for-id hel-tz))]
    (mh/publish @mqtt-client "time" (unparse mqtt-formatter zoned))
        ))

(defn init-mqtt []
  (let [id   (mh/generate-id)
        conn (mh/connect mqtt-addr id mqtt-opts)]
    (reset! mqtt-client conn)
    (mh/subscribe @mqtt-client ["timereq" "temphum"]
                  (fn [^String topic _ ^bytes payload]
                    (case topic
                      "timereq" (send-time)
                      "temphum" (let [json-payload (parse-string (String. payload "UTF-8") true)]
                                  (insert-sensor-data json-payload)))
                    ))
    )
  )

(defn publish-data [floor-temps floor-hums]
  (doseq [temp floor-temps]
    (mh/publish @mqtt-client (str "temp/" (:floor temp)) (str (:floor temp) (:temp temp))))
  (doseq [hum floor-hums]
    (mh/publish @mqtt-client (str "hum/" (:floor hum)) (str (:floor hum) (:hum hum))))
  )

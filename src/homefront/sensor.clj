(ns homefront.sensor
  (:require [cheshire.core :refer :all]
            [clj-time.core :as time]
            [overtone.at-at :refer :all]))

(defrecord PortHandle [path port])
(defrecord PortData [data timestamp])

(def config (atom {}))
(def ports (atom {}))
(def temps (atom {}))

(def at-pool (mk-pool))

(defn probe-data [data]
  (fn [probe]
    (if (nil? data)
      {}
      (let [this-data (first (filter #(= (probe "id") (% :id)) data))]
        { :id (probe "id") :name (probe "name") :temp (this-data :temp) :hum (this-data :hum) }))))

(defn get-probe-data [sensor]
  (let [probes (sensor "probes")
        temps (@temps (sensor "path"))]
    { :timestamp (:timestamp temps) :values (map (probe-data (:data temps)) probes) }))

(defn sensor-data [sensor] 
  (let [data (@temps (sensor "path"))]
    { :name (sensor "name") :data (get-probe-data sensor) }))
                     
(defn get-sensor-data [] 
  (map sensor-data (@config "sensors")))

(defn remove-port [path]
  (println "Remove port " path)
  (swap! ports dissoc path))

(defn add-port [path port]
  (let [new-port (PortHandle. path port)]
    (swap! ports assoc path new-port)))

(defn set-temps [path temp-data]
  (swap! temps assoc path (PortData. temp-data (time/now))))

(defn read-input-line [path input-stream]
  (let [in (java.io.BufferedReader. (java.io.InputStreamReader. input-stream))]
    (let [data (parse-string (.readLine in) true)]
      (println (time/now) path data)
      (set-temps path data)
      )
  ))

(defn no-fresh-data [path] 
  (let [last-received (:timestamp (@temps path))]
    (or (nil? last-received) (time/before? (time/plus last-received (time/seconds 30)) (time/now)))))

(def not-nil? (complement nil?))



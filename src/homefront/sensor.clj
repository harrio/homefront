(ns homefront.sensor
  (:require [serial-port :as serial]
            [cheshire.core :refer :all]
            [clj-time.core :as time]
            [overtone.at-at :refer :all]))

(defrecord PortHandle [path port])
(defrecord PortData [data timestamp])

(def config {})
(def ports (atom {}))
(def temps (atom {}))

(def at-pool (mk-pool))

(declare open-serial)
(declare close-serial)

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
      (println (time/now) data)
      (set-temps path data)
      )
  ))

(defn no-fresh-data [path] 
  (let [last-received (:timestamp (@temps path))]
    (or (nil? last-received) (time/before? (time/plus last-received (time/seconds 30)) (time/now)))))

(defn check-data [sensor]
  (let [path (sensor "path")]
    (println "Check " path)
    (if (or (nil? (@ports path))
            (no-fresh-data path))
      (do (println "Data timeout... reconnect") 
        (close-serial path)
        (open-serial sensor))
      (println "Data OK"))))

(defn check-serials []
  (println "Check data")
  (dorun (map check-data (config "sensors")))) 

(defn start-serial [config]
  (def config config)
  (dorun (map open-serial (config "sensors")))
  (def sched (interspaced 10000 check-serials at-pool :initial-delay 10000)))

(defn stop-serial []
  (stop sched)
  (dorun (map close-serial @ports))
  )

;"/dev/tty.HC-06-DevB"
(defn open-serial [sensor]
  (let [path (sensor "path")]
    (println "Opening serial " path)
    (try 
      (do
        (def port (serial/open path))
        (serial/listen port #(read-input-line path %))
        (add-port path port))
      (catch Exception e 
        (do 
          (println "Serial failed " (.getMessage e))
          )))))

(defn port-bound? [sym]
  (or (nil? sym) (if-let [v (resolve sym)]
    (bound? v)
    false)))

(def not-nil? (complement nil?))

(defn close-serial [path]
  (println "Close serial")
  (let [port (@ports path)]
    (if (not-nil? port)
      (do 
        (serial/remove-listener port)
        (serial/close port)
        (remove-port port)
        )
      (println "Nothing to close"))))


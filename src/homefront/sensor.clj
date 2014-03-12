(ns homefront.sensor
  (:require [serial-port :as serial]
            [cheshire.core :refer :all]
            [clj-time.core :as time]
            [overtone.at-at :refer :all]))


(def temps (atom {:timestamp nil}))
(def at-pool (mk-pool))

(declare open-serial)
(declare close-serial)

(defn set-temps [temp-data]
  (swap! temps (fn[old new] new) temp-data))

(defn read-input-line [input-stream]
  (let [in (java.io.BufferedReader. (java.io.InputStreamReader. input-stream))]
    (let [data (parse-string (.readLine in) true)]
      (println (time/now) data)
      (set-temps { :data data :timestamp (time/now) })
      )
  ))

(defn no-fresh-data [] 
  (let [last-received (:timestamp @temps)]
    (or (nil? last-received) (time/before? (time/plus last-received (time/seconds 30)) (time/now)))))

(defn check-serial []
  (println "Check data")
  (if (no-fresh-data)
    (do (println "Data timeout... reconnect") 
      (close-serial)
      (open-serial))
    (println "Data OK")))

(defn start-serial []
  (open-serial)
  (def sched (interspaced 10000 check-serial at-pool :initial-delay 10000)))

(defn stop-serial []
  (stop sched)
  (close-serial)
  )

(defn open-serial []
  (println "Opening serial...")
  (try 
    (do 
      (def port (serial/open "/dev/tty.HC-06-DevB"))
      (serial/listen port #(read-input-line %))
      )
    (catch Exception e (println "Serial failed " (.getMessage e)))))

(defn port-bound? [sym]
  (if-let [v (resolve sym)]
    (bound? v)
    false))

(defn close-serial []
  (println "Close serial")
  (if (port-bound? 'port)
    (do 
      (serial/remove-listener port)
      (serial/close port))
    (println "Nothing to close")))


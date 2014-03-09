(ns homefront.sensor
  (:require [serial-port :as serial]
            [cheshire.core :refer :all]
            [clj-time.core :as time]))

(def temps (atom []))

(defn set-temps [new-temps]
  (swap! temps (fn[old new] new) new-temps))

(defn read-input-line [input-stream]
  (let [in (java.io.BufferedReader. (java.io.InputStreamReader. input-stream))]
    (let [data (parse-string (.readLine in) true)]
      (println (time/now) data)
      (set-temps data)
      )
  ))

(defn open-serial []
  (def port (serial/open "/dev/tty.HC-06-DevB"))
  (serial/listen port #(read-input-line %)))

(defn close-serial []
  (serial/remove-listener port)
  (serial/close port))
(ns homefront.db
  (:require [monger.core :refer [connect! set-db!]]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [cheshire.core :refer :all]
            monger.joda-time)
  (:import [org.bson.types ObjectId]))

(connect!)
(set-db! (monger.core/get-db "homefront"))

(defn insert-sensor-data [data]
  (let [oid (ObjectId.)]
    (mc/insert "sensordata" (merge data {:_id oid}))))

(defn insert-sensor-json [json]
  (println "insert " json)
  (insert-sensor-data (parse-string json)))

(defn find-sensor-data [start-time end-time]
  (mc/find-maps "sensordata" { :time { $gte start-time $lte end-time }}))
  


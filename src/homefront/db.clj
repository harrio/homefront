(ns homefront.db
  (:require [monger.core :refer [connect! set-db!]]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [cheshire.core :refer :all]
            [clj-time.core :as time]
            monger.joda-time)
  (:import [org.bson.types ObjectId]))

(connect!)
(set-db! (monger.core/get-db "homefront"))

(defn apply-to-map-values [m func]
  (into {} (for [[key value] m] [key (func value)])))

(defn insert-sensor-data [data-obj]
  (doseq [entry (data-obj "data")]
    (let [oid (ObjectId.)]
      (mc/insert "sensordata" (merge entry { :addr (data-obj "addr") :_id oid :time (time/now) })))))

(defn insert-sensor-json [json]
  (insert-sensor-data (parse-string json)))

(defn find-sensor-data [start-time end-time]
  (mc/find-maps "sensordata" { :time { $gte start-time $lte end-time }}))

(defn get-grouped-sensor-data [start-time end-time]
  (vals (apply-to-map-values (group-by :addr (find-sensor-data start-time end-time)) #(group-by :id %))))
  


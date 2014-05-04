(ns homefront.db
  (:refer-clojure :exclude [sort find])
  (:require [monger.core :refer [connect! set-db!]]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [cheshire.core :refer :all]
            [clj-time.core :as time]
            monger.joda-time
            [monger.query :refer :all])
  (:import [org.bson.types ObjectId]))

(connect!)
(set-db! (monger.core/get-db "homefront"))

(defn apply-to-map-values [m func]
  (into {} (for [[key value] m] [key (func value)])))

(defn insert-sensor-data [data-obj]
  (doseq [entry (data-obj "data")]
    (let [oid (ObjectId.)]
      (mc/insert "sensordata" (merge entry { :addr (data-obj "addr") :_id oid :time (time/now) })))))

(defn insert-sensor-data-json [json]
  (insert-sensor-data (parse-string json)))

(defn find-sensor-data [start-time end-time]
  (with-collection "sensordata" 
    (find { :time { $gte start-time $lte end-time }})
    (sort (array-map :time 1))))

(defn find-single-sensor-data [mac start-time end-time]
  (mc/find-maps "sensordata" { :addr mac :time { $gte start-time $lte end-time }}))

(defn get-grouped-sensor-data [start-time end-time]
  (vals (apply-to-map-values (group-by :addr (find-sensor-data start-time end-time)) #(vals (group-by :id %)))))

(defn get-single-sensor-data [mac start-time end-time]
  (group-by :id (find-single-sensor-data mac start-time end-time)))

(defn find-sensors []
  (println "sensors")
  (mc/find-maps "sensor"))
  
(defn save-sensor [data-obj]
  (println data-obj)
  (if (contains? data-obj "_id")
    (let [oid (ObjectId. (data-obj "_id"))]
      (mc/update-by-id "sensor" oid (dissoc data-obj "_id")))
    (let [oid (ObjectId.)]
      (mc/insert "sensor" (merge data-obj { :_id oid  })))))

(defn save-sensor-json [json]
  (save-sensor json))

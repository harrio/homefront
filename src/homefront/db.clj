(ns homefront.db
  (:refer-clojure :exclude [sort find])
  (:require [monger.core :refer [connect! set-db!]]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [cheshire.core :refer :all]
            [clj-time.core :as time]
            [clj-time.periodic :as time-period]
            monger.joda-time
            [monger.query :refer :all])
  (:import [org.bson.types ObjectId]))

;(connect!)
;(set-db! (monger.core/get-db "homefront"))

(defn apply-to-map-values [m func]
  (into {} (for [[key value] m] [key (func value)])))

(defn insert-sensor-data [data-obj]
  (doseq [entry (data-obj "data")]
      (mc/insert "sensordata" (merge entry { :addr (data-obj "addr") :time (time/now) }))))

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

(defn remove-sensor [sensor-id]
  (let [oid (ObjectId. sensor-id)]
    (mc/remove-by-id "sensor" oid)))

(defn time-range
  "Return a lazy sequence of DateTime's from start to end, incremented
  by 'step' units of time."
  [start end step]
  (let [inf-range (time-period/periodic-seq start step)
        below-end? (fn [t] (time/within? (time/interval start end)
                                         t))]
    (take-while below-end? inf-range)))

(defn hour-range [start-time end-time]
  (time-range start-time
              end-time
              (time/hours 1)))

(defn insert-test-data [start-time end-time]
  (doseq [sensor (find-sensors)]
    (println (sensor :key))
    (doseq [probe (sensor :probes)]
      (println "  " (probe :key))
      (doseq [t (hour-range (time/date-time 2014 05 01 00 00) (time/date-time 2014 05 10 16 00))]
        (mc/insert "tempdata" { :sensor (sensor :key) :probe (probe :key) :value (time/hour t) :time t})))))

(defn find-temp-data [sensors start-time end-time]
  (with-collection "tempdata"
    (find { :time { $gte start-time $lte end-time } :sensor {$in sensors} })
    (sort (array-map :time 1))))

(defn get-grouped-temp-data [sensors start-time end-time]
  (let [data (find-temp-data sensors start-time end-time)]
    (into {} (for [[k v] (group-by :sensor data)]
                      [k (group-by :probe v)]))))

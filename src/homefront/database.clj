(ns homefront.database
  (:import java.sql.Date
           org.joda.time.LocalDate)
  (:require [clojure.string :as str]
            [korma.db :refer :all]
            [korma.core :refer :all]
            [cheshire.core :refer [generate-string]]
            [clj-time.coerce :as time-coerce]
            [clj-time.core :as time]
            [clj-time.periodic :as time-period]))

(def pg (postgres {:db "homefront"
                   :user "homefront"
                   :host "localhost"
                   :port 5432 }))

(declare sensor probe temperature)

(defentity sensor
  (pk :sensor_id)
  (has-many probe))

(defentity probe
  (pk :probe_id)
  (belongs-to sensor)
  (has-many measurement))

(defentity measurement
  (pk :temp_id)
  (belongs-to probe))

(defn get-sensors []
  (select sensor
          (with probe)))

(defn time-range
  "Return a lazy sequence of DateTime's from start to end, incremented
  by 'step' units of time."
  [start end step]
  (let [inf-range (time-period/periodic-seq start step)
        below-end? (fn [t] (time/within? (time/interval start end)
                                         t))]
    (take-while below-end? inf-range)))

(defn convert-instances-of [c f m]
  (clojure.walk/postwalk #(if (instance? c %) (f %) %) m))

(defn joda-datetime->sql-timestamp [m]
  (convert-instances-of org.joda.time.DateTime
                        time-coerce/to-sql-time
                        m))

(defn sql-timestamp->joda-datetime [m]
  (convert-instances-of java.sql.Timestamp
                        time-coerce/from-sql-time
                        m))

(defn hour-range [start-time end-time]
  (time-range start-time
              end-time
              (time/hours 1)))

(defn insert-test-data []
  (doseq [sensor (get-sensors)]
    (println (sensor :key))
    (doseq [probe (sensor :probe)]
      (println "  " (probe :key))
      (doseq [t (hour-range (time/date-time 2014 05 01 00 00) (time/date-time 2014 05 10 16 00))]
        (insert temperature (values { :probe_id (probe :probe_id) :value (time/hour t) :time (joda-datetime->sql-timestamp t)}))))))

(defn get-sensor-data [sensor_id start-time end-time]
  (select measurement
          (where {:probe_id [in (subselect probe
                                           (fields :probe_id)
                                           (where {:sensor_id sensor_id}))]
                  :time [between [(joda-datetime->sql-timestamp start-time) (joda-datetime->sql-timestamp end-time)]]
                  })))

(defn get-sensors-with-data [start-time end-time]
  (select sensor
          (fields :sensor_id :name)
          (with probe
                (fields :probe_id :name)
                (with measurement
                      (fields :time :value)
                      (where {:time [between [(joda-datetime->sql-timestamp start-time) (joda-datetime->sql-timestamp end-time)]]})))))

(get-sensor-data 2 (time/date-time 2014 05 01 01 00) (time/date-time 2014 05 01 02 00))
(get-sensors-with-data (time/date-time 2014 05 01 01 00) (time/date-time 2014 05 01 02 00))






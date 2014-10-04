(ns homefront.database
  (:import java.sql.Date
           org.joda.time.LocalDate)
  (:require [clojure.string :as str]
            korma.db
            [korma.core :as sql]
            [cheshire.core :refer [generate-string parse-string ]]
            [clj-time.coerce :as time-coerce]
            [clj-time.core :as time]
            [clj-time.periodic :as time-period]
            [homefront.models.schema :refer :all]))

(korma.db/defdb pg (korma.db/postgres {:db "homefront"
                   :user "homefront"
                   :host "localhost"
                   :port 5432 }))

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

(defn ^:private to-local-date-default-tz
  [date]
  (let [dt (time-coerce/to-date-time date)]
    (time-coerce/to-local-date (time/to-time-zone dt (time/default-time-zone)))))

(defn sql-date->joda-date [m]
  (convert-instances-of java.sql.Date
                        to-local-date-default-tz
                        m))

(defn joda-date->sql-date [m]
  (convert-instances-of org.joda.time.LocalDate
                        time-coerce/to-sql-date
                        m))

(defmacro defentity
  "Wrapperi Korman defentitylle, lisää yleiset prepare/transform-funktiot."
  [ent & body]
  `(sql/defentity ~ent
     (sql/prepare joda-date->sql-date)
     (sql/prepare joda-datetime->sql-timestamp)
     (sql/transform sql-date->joda-date)
     (sql/transform sql-timestamp->joda-datetime)
     ~@body))

(declare sensor probe temperature humidity)

(defentity sensor
  (sql/pk :sensor_id)
  (sql/has-many probe))

(defentity probe
  (sql/pk :probe_id)
  (sql/belongs-to sensor)
  (sql/has-many temperature))

(defentity temperature
  (sql/pk :temp_id)
  (sql/belongs-to probe))

(defentity humidity
  (sql/pk :hum_id)
  (sql/belongs-to probe))

(defn get-sensors []
  (sql/select sensor
          (sql/with probe)))

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

(defn insert-test-data []
  (doseq [sensor (get-sensors)]
    (println (sensor :key))
    (doseq [probe (sensor :probe)]
      (println "  " (probe :key))
      (doseq [t (hour-range (time/date-time 2014 05 01 00 00) (time/date-time 2014 05 10 16 00))]
        (sql/insert temperature (sql/values { :probe_id (probe :probe_id) :value (time/hour t) :time (joda-datetime->sql-timestamp t)}))))))

(defn get-sensor-data [sensor_id start-time end-time]
  (sql/select temperature
          (sql/where {:probe_id [in (sql/subselect probe
                                           (sql/fields :probe_id)
                                           (sql/where {:sensor_id sensor_id}))]
                  :time [between [(joda-datetime->sql-timestamp start-time) (joda-datetime->sql-timestamp end-time)]]
                  })))

(defn get-sensors-with-data [start-time end-time]
  (let [data (sql/select sensor
          (sql/fields :sensor_id :name)
          (sql/with probe
                (sql/fields :probe_id :name)
                (sql/with temperature
                      (sql/fields :time :value)
                      (sql/where {:time [between [(joda-datetime->sql-timestamp start-time) (joda-datetime->sql-timestamp end-time)]]}))))]
    (validate-sensors-with-data data)
    data))

;(defn insert-sensor-data [data-obj]
;  (doseq [entry (data-obj "data")]
;      (mc/insert "sensordata" (merge entry { :addr (data-obj "addr") :time (time/now) }))))

;(defn insert-sensor-data-json [json]
;  (insert-sensor-data (parse-string json)))

;(get-sensor-data 2 (time/date-time 2014 05 01 01 00) (time/date-time 2014 05 01 02 00))
(get-sensors-with-data (time/date-time 2014 05 01 01 00) (time/date-time 2014 05 01 02 00))






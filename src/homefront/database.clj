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
            [homefront.models.schema :refer :all]
            [homefront.util :as util]))

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

(defn- to-local-date-default-tz
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
  (let [sensors (sql/select sensor
          (sql/with probe))]
    (validate-sensors sensors)
    sensors))

(defn get-probes [mac]
  (sql/select probe
              (sql/where {:sensor_id [in (sql/subselect sensor
                                                        (sql/fields :sensor_id)
                                                        (sql/where {:mac mac}))]})))

(defn get-probe [mac key]
  (println mac key)
  (first (sql/select probe
              (sql/where {:sensor_id [in (sql/subselect sensor
                                                        (sql/fields :sensor_id)
                                                        (sql/where {:mac mac}
                                                                   ))]
                          :key key}))))

(get-probe "00:13:12:31:25:81" 1)

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
              (time/minutes 5)))

(defn- insert-temperature [temp]
  (sql/insert temperature (sql/values temp)))

(defn- insert-test-data []
  (doseq [sensor (get-sensors)]
    (println (sensor :key))
    (doseq [probe (sensor :probe)]
      (println "  " (probe :key))
      (doseq [t (hour-range (time/date-time 2014 10 8 00 00) (time/date-time 2014 10 12 23 00))]
        (insert-temperature { :probe_id (probe :probe_id) :value (+ 18 (* 5 (rand))) :time (joda-datetime->sql-timestamp t)})))))

(defn- delete-temperatures [temp-ids]
  (sql/delete temperature
              (sql/where {:temp_id [in temp-ids]})))

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


;(get-sensor-data 2 (time/date-time 2014 05 01 01 00) (time/date-time 2014 05 01 02 00))
;(get-sensors-with-data (time/date-time 2014 05 01 01 00) (time/date-time 2014 05 01 02 00))


(defn- update-probe [probe]
  (sql/update probe
              (sql/set-fields {:key (:key probe) :name (:name probe) :humidity (:humidity probe)})
              (sql/where {:probe_id (:probe_id probe)})))

(defn- insert-probe [probe sensor-id]
  (sql/insert probe
              (sql/values {:sensor_id sensor-id :key (:key probe) :name (:name probe) :humidity (:humidity probe)})))

(defn- save-probe [probe sensor-id]
  (if (:probe_id probe)
    (update-probe probe)
    (insert-probe probe sensor-id)))

(defn- update-sensor [sensor]
  (sql/update sensor
              (sql/set-fields {:mac (:mac sensor) :name (:name sensor)})
              (sql/where {:sensor_id (:sensor_id sensor)}))
  (doseq [probe (:probe sensor)]
    (save-probe probe (:sensor_id sensor))))

(defn- insert-sensor [sensor]
  (let [sensor-id (sql/insert sensor
              (sql/values {:mac (:mac sensor) :name (:name sensor)}))]
    (doseq [probe (:probe sensor)]
      (save-probe probe sensor-id))))

(defn- save-sensor [sensor]
  (validate-sensor sensor)
  (if (:sensor_id sensor)
    (update-sensor sensor)
    (insert-sensor sensor)))

(defn save-sensor-json [json]
  (save-sensor (parse-string json)))

(defn remove-sensor [sensor]
  (println "delete " sensor))

(defn- get-probe-data-last-hour [probe-id]
  (sql/exec-raw ["select * from temperature
             where probe_id = ?
             and extract(hour from time) = extract(hour from now()) - 1
             and time::date = now()::date
                 and aggregation is null order by time" [probe-id]] :results))

;(get-probe-data-last-hour 2)


(defn- make-aggregated-temp [probe-id]
  (let [last-hr-values (get-probe-data-last-hour probe-id)]
    (if (seq last-hr-values)
      {:aggregate {:probe_id probe-id
         :value (util/median-value last-hr-values)
         :time (time/today-at (time/hour (sql-timestamp->joda-datetime (:time (first last-hr-values)))) 00)
         :aggregation 1}
       :deleted-values (map #(:temp_id %) last-hr-values)}
      nil)))

;(make-aggregated-temp 2)

(defn- aggregate-temperatures [probe]
  (let [aggregated-temp (make-aggregated-temp (:probe_id probe))]
    (when aggregated-temp
      (insert-temperature (:aggregate aggregated-temp))
      (delete-temperatures (:deleted-values aggregated-temp)))))

;(aggregate-temperatures 1)

(defn- insert-probe-data [probe data]
  (sql/insert temperature (sql/values { :probe_id (:probe_id probe) :value (:temp data) :time (time/now)}))
  (if (:hum data)
    (sql/insert humidity (sql/values { :probe_id (:probe_id probe) :value (:hum data) :time (time/now)}))))

(defn- insert-sensor-data [data-obj]
  (validate-sensor-data-in data-obj)
  (doseq [data (:data data-obj)]
    (let [probe (get-probe (:mac data-obj) (:key data))]
      (println data probe)
      (insert-probe-data probe data)
      (aggregate-temperatures probe)
      )))


;(insert-sensor-data {:mac "00:13:12:31:25:81" :data [{:key 1 :temp 2000} {:key 2 :temp 3000 :hum 4000 :st 4}]})

(defn insert-sensor-data-json [json]
  (insert-sensor-data (parse-string json)))

(ns homefront.db.data
  (:import java.sql.Date
           org.joda.time.LocalDate)
  (:require [clojure.string :as str]
            [korma.db :as db]
            [korma.core :as sql]
            [cheshire.core :refer [generate-string parse-string ]]
            [clj-time.coerce :as time-coerce]
            [clj-time.core :as time]
            [homefront.models.schema :refer :all]
            [homefront.util :as util]
            [homefront.db.util :refer :all]
            [homefront.db.entity :refer :all]))

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

(defn- insert-temperature [temp]
  (sql/insert temperature (sql/values temp)))

(defn- insert-humidity [hum]
  (sql/insert humidity (sql/values hum)))

(defn- delete-temperatures [temp-ids]
  (sql/delete temperature
              (sql/where {:temp_id [in temp-ids]})))

(defn- delete-humidities [hum-ids]
  (sql/delete humidity
              (sql/where {:hum_id [in hum-ids]})))

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
                      (sql/where {:time [between [(joda-datetime->sql-timestamp start-time) (joda-datetime->sql-timestamp end-time)]]})
                      (sql/order :time :ASC))))]
    (validate-sensors-with-data data)
    data))

(defn get-groups-with-latest-data []
  (let [data (sql/select probegroup
          (sql/fields :group_id :name)
          (sql/with probe
                (sql/fields :probe_id :name)
                (sql/with temperature
                      (sql/fields :time :value)
                      (sql/limit 2)
                      (sql/order :time :DESC))
                (sql/with humidity
                      (sql/fields :time :value)
                      (sql/limit 2)
                      (sql/order :time :DESC))))]
    (validate-groups-with-latest-data data)
    data))

(defn get-probes-with-latest-data []
  (let [data (sql/select probe
                         (sql/fields :probe_id :name)
                         (sql/with temperature
                                   (sql/fields :time :value)
                                   (sql/limit 1)
                                   (sql/order :time :DESC))
                         )]
    data))

(defn get-groups-with-data [start-time end-time]
  (let [data (sql/select probegroup
                         (sql/fields :group_id :name)
                         (sql/with probe
                           (sql/fields :probe_id :name)
                           (sql/with temperature
                             (sql/fields :time :value)
                             (sql/where {:time [between [(joda-datetime->sql-timestamp start-time) (joda-datetime->sql-timestamp end-time)]]})
                             (sql/order :time :ASC)))
                         (sql/order :index :ASC))]
    (validate-groups-with-data data)
    data))

(defn- has-humidity [probe]
  (:humidity probe))

(defn- filter-group-with-humidity-probes [groups]
  (filter #(some has-humidity (:probe %1)) groups))

(defn get-groups-with-humidity-data [start-time end-time]
  (let [data (filter-group-with-humidity-probes
               (sql/select probegroup
                           (sql/fields :group_id :name)
                           (sql/with probe
                                     (sql/fields :probe_id :name)
                                     (sql/where {:humidity true})
                                     (sql/with humidity
                                               (sql/fields :time :value)
                                               (sql/where {:time [between [(joda-datetime->sql-timestamp start-time) (joda-datetime->sql-timestamp end-time)]]})
                                               (sql/order :time :ASC)))
                           (sql/order :index :ASC)))]

    (validate-groups-with-humidity-data data)
    data))

(defn- get-temp-data-last-hour [probe-id]
  (sql/exec-raw ["select * from temperature where probe_id = ?
                 and (extract(hour from time) = extract(hour from now()) - 1
                 and time::date = now()::date
                 or extract(hour from time) = 23
                 and time::date = now()::date - 1)
                 and aggregation is null order by time" [probe-id]] :results))

(defn- get-hum-data-last-hour [probe-id]
  (sql/exec-raw ["select * from humidity where probe_id = ?
                 and (extract(hour from time) = extract(hour from now()) - 1
                 and time::date = now()::date
                 or extract(hour from time) = 23
                 and time::date = now()::date - 1)
                 and aggregation is null order by time" [probe-id]] :results))

(defn make-aggregated-value [probe-id last-hour-getter id-column]
  (let [last-hr-values (last-hour-getter probe-id)]
    (if (seq last-hr-values)
      (let [base-time (sql-timestamp->joda-datetime (:time (first last-hr-values)))]
        {:aggregate {:probe_id probe-id
           :value (util/median-value last-hr-values)
           :time (time/date-time
                  (time/year base-time)
                  (time/month base-time)
                  (time/day base-time)
                  (time/hour base-time)
                  00 00)
           :aggregation 1}
         :deleted-values (map #(id-column %) last-hr-values)})
        nil)))

(defn- aggregate-temperatures [probe]
  (let [aggregated-temp (make-aggregated-value (:probe_id probe) get-temp-data-last-hour :temp_id)]
    (when aggregated-temp
      (insert-temperature (:aggregate aggregated-temp))
      (delete-temperatures (:deleted-values aggregated-temp)))))

(defn- aggregate-humidities [probe]
  (let [aggregated-hum (make-aggregated-value (:probe_id probe) get-hum-data-last-hour :hum_id)]
    (when aggregated-hum
      (insert-humidity (:aggregate aggregated-hum))
      (delete-humidities (:deleted-values aggregated-hum)))))

(defn- insert-probe-data [probe data]
  (sql/insert temperature (sql/values { :probe_id (:probe_id probe) :value (:temp data)}))
  (aggregate-temperatures probe)
  (when (:hum data)
    (sql/insert humidity (sql/values { :probe_id (:probe_id probe) :value (:hum data)}))
    (aggregate-humidities probe)))

(defn insert-sensor-data [data-obj]
  (validate-sensor-data-in data-obj)
  (doseq [data (:data data-obj)]
    (let [probe (get-probe (:mac data-obj) (:key data))]
      (println data probe)
      (insert-probe-data probe data)
      )))

(defn- no-recent-data? [probe-data]
  (let [temp (first (:temperature probe-data))]
    (if temp
      (time/before? (:time temp) (time/minus (time/now) (time/minutes 60))))))

(defn get-dead-sensors []
  (filter no-recent-data? (get-probes-with-latest-data)))

(defn floor->temp [data]
  {:floor (:floor data) :temp (format "%02d"
                             (Math/round
                               (.doubleValue
                                 (:value
                                   (first (:temperature
                                            (first (:probe data))))))))})

(defn floor->hum [data]
  {:floor (:floor data) :hum (format "%02d"
                             (Math/round
                               (.doubleValue
                                 (:value
                                   (first (:humidity
                                            (first (:probe data))))))))})

(defn get-floor-temps []
  (let [data (sql/select sensor
                         (sql/fields :sensor_id :floor)
                         (sql/where {:floor [not= nil]})
                         (sql/with probe
                                   (sql/fields :probe_id)
                                   (sql/where {:humidity true})
                                   (sql/with temperature
                                             (sql/fields :value)
                                             (sql/limit 1)
                                             (sql/order :time :DESC))))]
    (map floor->temp data)))

(defn get-floor-hums []
  (let [data (sql/select sensor
                         (sql/fields :sensor_id :floor)
                         (sql/where {:floor [not= nil]})
                         (sql/with probe
                                   (sql/fields :probe_id)
                                   (sql/where {:humidity true})
                                   (sql/with humidity
                                             (sql/fields :value)
                                             (sql/limit 1)
                                             (sql/order :time :DESC))))]
    (map floor->hum data)))
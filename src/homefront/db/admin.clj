(ns homefront.db.admin
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

(defn get-sensors []
  (let [sensors (sql/select sensor
          (sql/with probe))]
    (validate-sensors sensors)
    sensors))

(defn get-groups []
  (let [groups (sql/select probegroup)]
    (validate-groups groups)
    groups))

(defn- update-probe [probe-data]
  (sql/update probe
              (sql/set-fields {:key (:key probe-data) :name (:name probe-data) :humidity (:humidity probe-data) :group_id (:group_id probe-data)})
              (sql/where {:probe_id (:probe_id probe-data)})))

(defn- insert-probe [probe-data sensor-id]
  (sql/insert probe
              (sql/values {:sensor_id sensor-id :key (:key probe-data) :name (:name probe-data) :humidity (:humidity probe-data) :group_id (:group_id probe-data)})))

(defn- save-probe [probe-data sensor-id]
  (if (:probe_id probe-data)
    (update-probe probe-data)
    (insert-probe probe-data sensor-id)))

(defn- update-sensor [sensor-data]
  (sql/update sensor
              (sql/set-fields {:mac (:mac sensor-data) :name (:name sensor-data)})
              (sql/where {:sensor_id (:sensor_id sensor-data)}))
  (doseq [probe (:probe sensor-data)]
    (save-probe probe (:sensor_id sensor-data))))

(defn- insert-sensor [sensor-data]
  (let [sensor-id (:sensor_id (sql/insert sensor
              (sql/values {:mac (:mac sensor-data) :name (:name sensor-data)})))]
    (doseq [probe (:probe sensor-data)]
      (save-probe probe sensor-id))))

(defn save-sensor-db [sensor]
  (validate-sensor sensor)
  (db/transaction
    (if (:sensor_id sensor)
      (update-sensor sensor)
      (insert-sensor sensor))))


(defn- update-group [group-data]
  (sql/update probegroup
              (sql/set-fields {:name (:name group-data) :index (:index group-data)})
              (sql/where {:group_id (:group_id group-data)})))

(defn- insert-group [group-data]
  (sql/insert probegroup
              (sql/values {:name (:name group-data) :index (:index group-data)})))

(defn- transform-group [group]
  (if (= java.lang.String (type (:index group)))
    (assoc group :index (Integer/parseInt (:index group)))
    group))


(defn save-group-db [group]
  (let [int-index-group (transform-group group)]
    (validate-group int-index-group)
    (db/transaction
      (if (:group_id int-index-group)
        (update-group int-index-group)
        (insert-group int-index-group)))))

(defn remove-sensor [sensor]
  (println "delete " sensor))

(defn remove-group [group]
  (println "delete " group))

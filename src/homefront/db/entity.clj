(ns homefront.db.entity
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
            [homefront.db.util :refer :all]))

(defmacro defentity
  "Wrapperi Korman defentitylle, lisää yleiset prepare/transform-funktiot."
  [ent & body]
  `(sql/defentity ~ent
     (sql/prepare joda-date->sql-date)
     (sql/prepare joda-datetime->sql-timestamp)
     (sql/transform sql-date->joda-date)
     (sql/transform sql-timestamp->joda-datetime)
     ~@body))

(declare sensor probe probe-in-group probegroup temperature humidity)

(defentity sensor
  (sql/pk :sensor_id)
  (sql/has-many probe))

(defentity probe
  (sql/pk :probe_id)
  (sql/belongs-to probegroup {:fk :group_id})
  (sql/belongs-to sensor)
  (sql/has-many temperature)
  (sql/has-many humidity))

(defentity probegroup
  (sql/pk :group_id)
  (sql/has-many probe {:fk :group_id}))

(defentity temperature
  (sql/pk :temp_id)
  (sql/belongs-to probe))

(defentity humidity
  (sql/pk :hum_id)
  (sql/belongs-to probe))

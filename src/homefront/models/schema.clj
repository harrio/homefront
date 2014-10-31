(ns homefront.models.schema
  (:require [schema.core :as s]
            [schema.coerce :as c]
            [clj-time.core :as time])
  (import org.joda.time.DateTime))

(def Sensor-data-in
  {:mac s/Str
   :data [{:key s/Str :temp s/Num (s/optional-key :hum) s/Num (s/optional-key :st) s/Num}]
   })

(def Probe
  {(s/optional-key :probe_id) s/Num
   (s/optional-key :sensor_id) s/Num
   (s/optional-key :group_id) s/Num
   :key s/Str
   :name s/Str
   :humidity s/Bool})

(def Sensor
  {(s/optional-key :sensor_id) s/Num
   :mac s/Str
   :name s/Str
   (s/optional-key :key) s/Str
   (s/optional-key :active) s/Bool
   :probe [Probe]})

(def Sensors [Sensor])

(def Probe-group
  {(s/optional-key :group_id) s/Num
   :name s/Str
   :index s/Num})

(def Probe-groups [Probe-group])

(def Temperature-in
  {:temp_id s/Num
   :probe_id s/Num
   :value s/Num
   :time org.joda.time.DateTime})

(def Temperature-out
  (dissoc Temperature-in :temp_id :probe_id))

(def Humidity-in
  {:hum_id s/Num
   :probe_id s/Num
   :value s/Num
   :time org.joda.time.DateTime})

(def Humidity-out
  (dissoc Humidity-in :hum_id :probe_id))

(def Groups-with-latest-data
  [{:group_id s/Num
    :name s/Str
    :probe [{:probe_id s/Num
      :name s/Str
      :temperature [Temperature-out]
      :humidity [Temperature-out]}]}])

(def Sensors-with-data
  [{:sensor_id s/Num
    :name s/Str
    :probe [{:probe_id s/Num
      :name s/Str
      :temperature [Temperature-out]}]}])

(def Probe-groups-with-data
  [{:group_id s/Num
    :name s/Str
    :probe [{:probe_id s/Num
      :name s/Str
      :temperature [Temperature-out]}]}])

(def Probe-groups-with-humidity-data
  [{:group_id s/Num
    :name s/Str
    :probe [{:probe_id s/Num
      :name s/Str
      :humidity [Humidity-out]}]}])

(defn validate-sensors-with-data [data]
  (s/validate Sensors-with-data data))

(defn validate-groups-with-latest-data [data]
  (s/validate Groups-with-latest-data data))

(defn validate-sensor-data-in [data]
  (s/validate Sensor-data-in data))

(defn validate-sensor [sensor]
  (s/validate Sensor sensor))

(defn validate-sensors [sensors]
  (s/validate Sensors sensors))

(defn validate-group [group]
  (s/validate Probe-group group))

(defn validate-groups [groups]
  (s/validate Probe-groups groups))

(defn validate-groups-with-data [groups]
  (s/validate Probe-groups-with-data groups))

(defn validate-groups-with-humidity-data [groups]
  (s/validate Probe-groups-with-humidity-data groups))

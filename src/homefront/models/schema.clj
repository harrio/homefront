(ns homefront.models.schema
  (:require [schema.core :as s]
            [clj-time.core :as time])
  (import org.joda.time.DateTime))

(def Probe
  {:probe_id s/Num
   :sensor_id s/Num
   :key s/Num
   :name s/Str
   :humidity s/Bool})

(def Sensor
  {:sensor_id s/Num
   :mac s/Str
   :name s/Str})

(s/validate Sensor {:sensor_id 1, :mac "mac", :name "name"})

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

(def Sensors-with-data
  [{:sensor_id s/Num
    :name s/Str
    :probe [{:probe_id s/Num
      :name s/Str
      :temperature [Temperature-out]}]}])

(defn validate-sensors-with-data [data]
  (s/validate Sensors-with-data data))



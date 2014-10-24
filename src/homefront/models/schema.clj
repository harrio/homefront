(ns homefront.models.schema
  (:require [schema.core :as s]
            [clj-time.core :as time])
  (import org.joda.time.DateTime))

(def Sensor-data-in
  {:mac s/Str
   :data [{:key s/Str :temp s/Num (s/optional-key :hum) s/Num (s/optional-key :st) s/Num}]
   })

(def Probe
  {(s/optional-key :probe_id) s/Num
   (s/optional-key :sensor_id) s/Num
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

(defn validate-sensor-data-in [data]
  (s/validate Sensor-data-in data))

(defn validate-sensor [sensor]
  (s/validate Sensor sensor))

(defn validate-sensors [sensors]
  (s/validate Sensors sensors))

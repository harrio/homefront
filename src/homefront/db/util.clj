(ns homefront.db.util
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
            [environ.core :refer [env]]))

(def db-port (Integer/parseInt (or (env :homefront-db-port) "5432")))

(db/defdb pg (korma.db/postgres {:db "homefront"
                   :user "homefront"
                   :password "homefront"
                   :host "localhost"
                   :port db-port}))

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

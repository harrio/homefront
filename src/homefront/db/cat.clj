(ns homefront.db.cat
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

(defn feed-cat? []
  (db/transaction
    (sql/delete heartbeat (sql/where {:key "cat"}))
    (sql/insert heartbeat (sql/values {:key "cat"  :time (time/now)})))
  (db/transaction
    (let [unfed (first (sql/select feeding
                                   (sql/where {:time   [<= (joda-datetime->sql-timestamp (time/now))]
                                               :status "P"})))
          feed-id (:feed_id unfed)]
      (if unfed
        (do
          (sql/update feeding
                      (sql/set-fields {:status "S"})
                      (sql/where {:feed_id feed-id}))
          feed-id)
        nil)))
  )

(defn cat-fed [feed-id]
  (db/transaction
    (sql/update feeding
                (sql/set-fields {:status "R"})
                (sql/where {:feed_id feed-id}))))

(defn get-feedings []
  (sql/select feeding))

(defn get-next-feeding []
  (first (sql/select feeding
                     (sql/where {:status "P"})
                     (sql/order :time :ASC)
                     (sql/limit 1))))

(defn get-last-ready-feeding []
  (first (sql/select feeding
                     (sql/where {:status "R"})
                     (sql/order :time :DESC)
                     (sql/limit 1))))

(defn insert-feeding [time]
  (db/transaction
    (sql/insert feeding (sql/values
                          {:time (joda-datetime->sql-timestamp time)
                           :status "P"}))))

(defn get-cat-heartbeat []
  (first (sql/select heartbeat
                     (sql/where {:key "cat"}))))
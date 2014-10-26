(ns homefront.test.database
  (:require [clojure.test :refer :all]
            [homefront.database :refer :all]
            [korma.db :as db]
            [korma.core :as sql]
            [clj-time.core :as time]))

(def this-aggr-datetime (time/date-time 2014 12 24 12 00 00))
(def prev-aggr-datetime (time/date-time 2014 12 24 23 00 00))

(def this-day-values [{:temp_id 1 :probe_id 1 :value 10 :time (time/date-time 2014 12 24 12 10 00)}
                      {:temp_id 2 :probe_id 1 :value 20 :time (time/date-time 2014 12 24 12 20 00)}])

(def prev-day-values [{:temp_id 1 :probe_id 1 :value 10 :time (time/date-time 2014 12 24 23 10 00)}
                      {:temp_id 2 :probe_id 1 :value 20 :time (time/date-time 2014 12 24 23 20 00)}])

(deftest aggregate-this-day-test
  (let [aggr (make-aggregated-value 1 (fn [a] this-day-values) :temp_id)]
    (is (= (get-in aggr [:aggregate :value]) 15))
    (is (= (get-in aggr [:aggregate :time]) this-aggr-datetime))
    (is (= (:deleted-values aggr) '(1 2)))))

(deftest aggregate-prev-day-test
  (let [aggr (make-aggregated-value 1 (fn [a] prev-day-values) :temp_id)]
    (is (= (get-in aggr [:aggregate :value]) 15))
    (is (= (get-in aggr [:aggregate :time]) prev-aggr-datetime))
    (is (= (:deleted-values aggr) '(1 2)))))

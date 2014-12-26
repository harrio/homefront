(ns homefront.mail
  (:require [postal.core :refer [send-message]]
            [environ.core :refer [env]]
            [clojure.string :refer [join]]))

(def conn {:host "smtp.gmail.com"
           :ssl true
           :user (env :homefront-mail-user)
           :pass (env :homefront-mail-pwd)})

(defn send-probes-dead [probes]
  (println "sending")
  (println (:user conn) (:pass conn) (env :homefront-mail-receiver))
  (send-message conn {:from (env :homefront-mail-user)
                      :to (env :homefront-mail-receiver)
                      :subject "Homefront sensor problem"
                      :body (join ", " probes)}))

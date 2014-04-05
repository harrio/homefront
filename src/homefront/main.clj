(ns homefront.main
  (:use homefront.handler
        [org.httpkit.server :only [run-server]])
  (:gen-class))
(defn -main [& [port]]
  (let [port (if port (Integer/parseInt port) 3000)]
    (run-server app {:port port})
    (println (str "Started server on port " port))))
  


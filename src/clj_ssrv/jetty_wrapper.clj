(ns clj-ssrv.jetty-wrapper
  (:import [org.eclipse.jetty.server
            Request
            Server]
           [org.eclipse.jetty.webapp WebAppContext])
  (:require [clojure.java.io :as io]
            [clj-ssrv.config :as cfg])
  )


(def ^:private SERVER nil)

(defn running? [] (and SERVER (.isStarted SERVER)))

(defn stop-server []
  (when SERVER (.stop SERVER)))

(defn start-server []
  (let [webapp (WebAppContext.)]
    (stop-server)
    (def SERVER (Server. (read-string (cfg/get-prop "amazon.rvs-sandbox-port"))))
    (.setContextPath webapp "/")
    (.setWar webapp (.getFile (io/resource "RVSSandbox.war")))
    (.setHandler SERVER webapp)
    (.start SERVER)))


  

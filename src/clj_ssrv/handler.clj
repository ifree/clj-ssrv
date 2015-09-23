(ns clj-ssrv.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.tools.logging :as log]
            [clj-ssrv.config :as cfg]
            [clj-ssrv.jetty-wrapper :as rvs-server]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :refer [response]]
            ring.adapter.jetty)

  (:require [clj-ssrv.verification-service-google :as Google-verify-service]
            [clj-ssrv.verification-service-amazon :as Amazon-verify-service]))

;;; reload config first
(cfg/update-props)

(defn start-rvs-sandbox []
  (log/info "starting rvs sandbox")
  (when (not (rvs-server/running?))
          (rvs-server/start-server)))

(defroutes app-routes
  (GET "/" [] "Hello World! <br>RVS server is working.<br>
If you want to start amazon sandbox verification server,
<a href=\"/init-amazon\">click here</a>")
  
  (GET "/init-amazon" []
       (when (read-string (cfg/get-prop "amazon.rvs-use-sandbox"))
         (start-rvs-sandbox)
         "amazon sandbox verification service started.<a href=\"/\">back</a>"))
  
  (POST "/getpayload" request
        (let [{:strs [userid platformid bid product-id]} (:body request)]
          (response
           (Google-verify-service/get-payload
            platformid userid product-id bid))))
  
  (POST "/verifyReceipt" request
        (let [{:strs [startDate
                      platformid
                      userid
                      purchaseToken
                      quantity
                      client_dp
                      bid
                      product-id
                      receiptID]}
              (:body request)
              func (if (= platformid "google")
                     #'Google-verify-service/verify
                     #'Amazon-verify-service/verify)]
          (log/debug "start verifing purchase " request)
          (response (func platformid
                          userid
                          receiptID
                          product-id
                          bid
                          quantity
                          (.getTime (java.util.Date.))
                          client_dp
                          purchaseToken))))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-json-body)
      (wrap-json-response)))

(defonce ^:private server
  (delay (ring.adapter.jetty/run-jetty #'app {:port 3000 :join? false})))

(defn run
  []
  @server)

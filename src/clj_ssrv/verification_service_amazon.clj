(ns clj-ssrv.verification-service-amazon
  (:require [clj-http.client :as http]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [clj-ssrv.config :as cfg]
            ))


(defn verify [platformid
              userid
              receipt-id
              product-id
              appid
              quantity
              start-date
              client-dp
               purchase-token]
  ;; <Protocol>//<Server>/<[RVSSandbox]>/version/<Operation_version_number>/verifyReceiptId/developer/<Shared_Secret>/user/<UserId>/receiptId/<ReceiptId>
  (let [server (if (read-string (cfg/get-prop "amazon.rvs-use-sandbox"))
                 (str "http://localhost:"
                      (cfg/get-prop "amazon.rvs-sandbox-port"))
                 (cfg/get-prop "amazon.rvs-server"))
        url (str server
                 "/version/"
                 (cfg/get-prop "amazon.rvs-version")
                 "/verifyReceiptId/developer/"
                 (cfg/get-prop "amazon.developer-secret")
                 "/user/"
                 userid
                 "/receiptId/"
                 receipt-id)
        resp (http/get url {:throw-exceptions false})
        status (:status resp)
        success? (= status 200)
        resp-obj (json/read-str (:body resp))
        ]
    (log/debug "amazon verification reponse: " resp)
    
    {:status (if success? 0 status)
     :clientproduct_id product-id
     :serverproduct_id (if success? (:productId resp-obj) nil)
     :userhashcode userid
     :transaction_id nil}
    ))

(ns clj-ssrv.verification-service-google

  (:require [clj-http.client :as http]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [clojure.data.codec.base64 :as base64]
            [clj-ssrv.config :as cfg]
            [clj-ssrv.google-api-client :as api-client]
            ))


(defonce rsa-key-factory (delay (java.security.KeyFactory/getInstance "RSA")))

(defn- generate-public-key [encode-key]
  "Generate public key instance from base public key"
  (let [^bytes key-bytes (base64/decode encode-key)]
    (.generatePublic @rsa-key-factory
                     (java.security.spec.X509EncodedKeySpec. key-bytes))))

(defn- key-verify-internal [public-key signed-data signature]
  "Verify signed data with public key and signature."
  (let [sig (doto (java.security.Signature/getInstance "SHA1withRSA")
              (.initVerify public-key)
              (.update (.getBytes signed-data)))]
    (.verify sig (base64/decode (.getBytes signature)))))

(defn- verify-via-google-pubsher-api [package-name sku-id purchase-token]
  "Verify via google pubsher api"
  (let [url (format "https://www.googleapis.com/androidpublisher/v2/applications/%s/purchases/products/%s/tokens/%s"
                    package-name sku-id purchase-token)]
    (http/get
     url
     {:throw-exceptions false
      :query-params {"access_token" (api-client/get-access-token)}})))

(defn get-payload [platformid userid sku appid]
  "Get developer payload."
  (log/info "get developer layload " platformid userid sku appid)
  
  (let [content (str platformid userid sku appid "*magic tailing*")
        hash-bytes (doto (java.security.MessageDigest/getInstance "MD5")
                (.reset)
                (.update (.getBytes content)))]
    {:dp (.toString (java.math.BigInteger. 1 (.digest hash-bytes)) 16)}))

(defn verify [platformid
              userid
              receipt-id
              product-id
              appid
              quantity
              start-date
              client-dp
              purchase-token]
  "Purchase verification."
  (log/info "Start verify via pubsher api" platformid
            userid
            receipt-id
            product-id
            appid
            quantity
            start-date
            client-dp
            purchase-token)
  (let [server-dp (:dp (get-payload platformid userid product-id appid))]
    (log/debug "clientdp " client-dp "serverdp " server-dp)
    (when (= client-dp server-dp)
      (let [resp (verify-via-google-pubsher-api appid product-id purchase-token)
            success? (= 200 (:status resp))
            body (:body resp)]
        
        (log/info "verification reponse: " resp)
        {:status (if success? 0 (:status resp))
         :clientproduct_id product-id
         :serverproduct_id (if success? (:sku (json/read-str body)) nil)
         :userhashcode userid
         :transaction_id nil}))))



;;; test

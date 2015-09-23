(ns clj-ssrv.google-api-client
  (:import 
   com.google.api.client.auth.oauth2.Credential
   com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
   com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
   com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
   com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
   com.google.api.client.googleapis.auth.oauth2.GoogleCredential
   com.google.api.client.googleapis.auth.oauth2.GoogleCredential$Builder
   com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
   com.google.api.client.http.HttpTransport
   com.google.api.client.json.JsonFactory
   com.google.api.client.json.jackson2.JacksonFactory
   com.google.api.client.repackaged.com.google.common.base.Preconditions
   com.google.api.client.repackaged.com.google.common.base.Strings
   com.google.api.client.util.store.DataStoreFactory
   com.google.api.client.util.store.FileDataStoreFactory
   com.google.api.services.androidpublisher.AndroidPublisher
   com.google.api.services.androidpublisher.AndroidPublisherScopes
   java.util.Collections
   (java.io File IOException InputStreamReader))
  (:require [clj-ssrv.config :as cfg]
            [clojure.java.io :as io])
  )

(def ^JsonFactory JSON_FACTORY (JacksonFactory/getDefaultInstance))

(def HTTP_TRANSPORT (GoogleNetHttpTransport/newTrustedTransport))

(defn get-credential []
  (let [builder (doto (GoogleCredential$Builder.)
      (.setTransport HTTP_TRANSPORT)
      (.setJsonFactory JSON_FACTORY)
      (.setServiceAccountId (cfg/get-prop "google.publisher.email"))
      (.setServiceAccountScopes
       (Collections/singleton (AndroidPublisherScopes/ANDROIDPUBLISHER)))
      (.setServiceAccountPrivateKeyFromP12File
       (let [file (cfg/get-prop "google.pubsher.p12.file")]
         (if (empty? file)
           (io/file (io/resource (cfg/get-prop "google.pubsher.p12.resource")))
           (io/file file)))))]
    (.build builder)))

(defn get-access-token []
  (let [credential (get-credential)
        token (.getAccessToken credential)]
    (if (nil? token) 
      (do
        (.refreshToken credential)
        (.getAccessToken credential))
      token)))

(defn refresh-access-token []
  (let [credential (get-credential)]
    (when credential
      (.refreshToken credential))))

(ns clj-ssrv.config
  (:require [clojure.java.io :as io]))

(def prop-store (ref {}))

(defn update-props
  ([file]
   (with-open [^java.io.Reader
               reader
               (io/reader file)] 
     (let [props (java.util.Properties.)]
       (.load props reader)
       (dosync
        (ref-set
         prop-store
         (into {}
               (for [[k v] props] [(keyword k) v])))))))
  ([]
   (if-let [file-name (System/getenv "ssrv.config")]
     (update-props (java.io.File. file-name))
     (update-props (.getFile (io/resource "config.properties"))))))

(defn get-prop [key]
  (get @prop-store (keyword key) nil))


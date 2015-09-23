(defproject clj-ssrv "0.1.0-SNAPSHOT"
  :description "Clojure InApp purchase validation service"
  :url "http://google.com"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.1"]
                 [ring "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [org.eclipse.jetty/jetty-webapp "9.2.10.v20150310"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/data.codec "0.1.0"]
                 [clj-http "2.0.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [com.google.oauth-client/google-oauth-client-java6 "1.19.0"]
                 [com.google.oauth-client/google-oauth-client-jetty "1.19.0"]
                 [com.google.apis/google-api-services-androidpublisher "v2-rev20-1.20.0"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                  javax.jms/jms
                                                  com.sun.jdmk/jmxtools
                                                  com.sun.jmx/jmxri]]]
  :plugins [[lein-ring "0.8.13"]]
;  :aot :all
  :ring {:handler clj-ssrv.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})

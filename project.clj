(defproject yuhta/gcal2org "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [yuhta/googleauth "0.1.0-SNAPSHOT"]
                 [com.google.apis/google-api-services-calendar "v3-rev168-1.21.0"]]
  :main yuhta.gcal2org
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

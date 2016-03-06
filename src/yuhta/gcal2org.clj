(ns yuhta.gcal2org
  (:require [yuhta.googleauth :refer :all]
            [clojure.instant :refer [read-instant-date]])
  (:import com.google.api.client.googleapis.util.Utils
           com.google.api.client.util.DateTime
           (com.google.api.services.calendar Calendar Calendar$Builder
                                             CalendarScopes)
           (java.text DateFormat SimpleDateFormat))
  (:gen-class))

(defn- ^Calendar service []
  (-> (Calendar$Builder. *http-transport*
                         (Utils/getDefaultJsonFactory)
                         (authorize))
      (.setApplicationName (str (:ns (meta #'service))))
      .build))

(defn- time-range []
  (let [t-min (java.util.Calendar/getInstance)
        ^java.util.Calendar t-max (.clone t-min)]
    (.add t-min java.util.Calendar/DATE -7)
    (.add t-max java.util.Calendar/DATE 7)
    (map #(DateTime. (.getTime ^java.util.Calendar %)) [t-min t-max])))

(def ^:private ^DateFormat date-format (SimpleDateFormat. "yyyy-MM-dd"))
(def ^:private ^DateFormat time-format (SimpleDateFormat. "HH:mm"))
(def ^:private ^DateFormat datetime-format (SimpleDateFormat. "yyyy-MM-dd HH:mm"))

(defn- org-timestamp [start end]
  (if-let [^DateTime s (get start "date")]
    (let [e (doto (java.util.Calendar/getInstance)
              (.setTime (.parse date-format (str (get end "date"))))
              (.add java.util.Calendar/DATE -1))]
      (if (= (.getValue s) (.getTimeInMillis e))
        (str "<" s ">")
        (str "<" s ">--<" (.format date-format (.getTime e)) ">")))
    (let [[s e] (map #(read-instant-date (str (get % "dateTime"))) [start end])]
      (if (= (.format date-format s) (.format date-format e))
        (str "<" (.format datetime-format s) "-" (.format time-format e) ">")
        (str "<" (.format datetime-format s) ">--<" (.format datetime-format e) ">")))))

(defn- print-events [^Calendar svc cal-id t-min t-max]
  (println "*" cal-id)
  (doseq [item (-> svc .events (.list cal-id)
                   (.setSingleEvents true) (.setTimeMin t-min) (.setTimeMax t-max)
                   .execute
                   (get "items"))]
    (println "**" (get item "summary"))
    (println "  " (org-timestamp (get item "start") (get item "end")))
    (some-> (get item "description") println)))

(defn -main [& cal-ids]
  (let [svc (service)]
    (if cal-ids
      (let [[t-min t-max] (time-range)]
        (doseq [id cal-ids] (print-events svc id t-min t-max)))
      (doseq [item (-> svc .calendarList .list .execute (get "items"))]
        (prn (select-keys item ["id" "summary"]))))))

(ns state-datascript.core
  (:require [clojure.walk :as walk]
            [datascript.core :as ds]
            [replicant.alias :as alias]
            [replicant.dom :as r]
            [state-datascript.router :as router]
            [state-datascript.ui :as ui]))

(defn routing-anchor [attrs children]
  (let [routes (-> attrs :replicant/alias-data :routes)]
    (into [:a (cond-> attrs
                (:ui/location attrs)
                (assoc :href (router/location->url routes
                                                   (:ui/location attrs))))]
          children)))

(alias/register! :ui/a routing-anchor)

(defn find-target-href [e]
  (some-> e .-target
          (.closest "a")
          (.getAttribute "href")))

(defn get-current-location []
  (->> js/location.href
       (router/url->location router/routes)))

(defn interpolate-actions [event actions]
  (walk/postwalk
   (fn [x]
     (case x
       :event/target.value (.. event -target -value)
       ;; Add more cases as needed
       x))
   actions))

(defn execute-actions [conn actions]
  (doseq [[action & args] actions]
    (case action
      :db/transact (apply ds/transact! conn args)
      (println "Unknown action" action "with arguments" args))))

(defn get-location-entity [location]
  (into {:db/ident :ui/location
         :location/query-params {}
         :location/hash-params {}
         :location/params {}}
        location))

(defn route-click [e conn routes]
  (let [href (find-target-href e)]
    (when-let [location (router/url->location routes href)]
      (.preventDefault e)
      (if (router/essentially-same? location (ds/entity (ds/db conn) :ui/location))
        (.replaceState js/history nil "" href)
        (.pushState js/history nil "" href))
      (ds/transact! conn [(-> (router/url->location router/routes href)
                              get-location-entity)]))))

(defn main [conn el]
  (add-watch
   conn ::render
   (fn [_ _ _ _]
     (r/render el (ui/render-page (ds/db conn)) {:alias-data {:routes router/routes}})))

  (r/set-dispatch!
   (fn [event-data actions]
     (->> actions
          (interpolate-actions
           (:replicant/dom-event event-data))
          (execute-actions conn))))

  (js/document.body.addEventListener
   "click"
   #(route-click % conn router/routes))

  (js/window.addEventListener
   "popstate"
   (fn [_] (ds/transact! conn [(-> (get-current-location)
                                   get-location-entity)])))

  ;; Trigger the initial render
  (ds/transact! conn [{:db/ident :system/app
                       :app/started-at (js/Date.)}
                      (get-location-entity (get-current-location))]))

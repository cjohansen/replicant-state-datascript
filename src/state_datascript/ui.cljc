(ns state-datascript.ui
  (:require [datascript.core :as ds]))

(defn render-frontpage [db _]
  (let [app (ds/entity db :system/app)
        clicks (:clicks app)]
    [:div
     [:h1 "Hello world"]
     [:p "Started at " (:app/started-at app)]
     [:button
      {:on {:click [[:db/transact [[:db/add (:db/id app) :clicks (inc clicks)]]]]}}
      "Click me"]
     (when (< 0 clicks)
       [:p
        "Button was clicked "
        clicks
        (if (= 1 clicks) " time" " times")])
     [:p [:ui/a {:ui/location {:location/page-id :pages/episode
                               :location/params {:episode/id "s2e1"}}}
          "Episode 1"]]]))

(defn render-episode [_ location]
  [:main
   [:h1 "Episode " (-> location :location/params :episode/id)]
   (if (-> location :location/hash-params :description)
     (list
      [:p "It's an episode of Parens of the dead"]
      [:ui/a {:ui/location (update location :location/hash-params dissoc :description)}
       "Hide description"])
     [:ui/a {:ui/location (assoc-in location [:location/hash-params :description] "1")}
      "Show description"])
   [:p
    [:ui/a {:ui/location {:location/page-id :pages/frontpage}}
     "Back to frontpage"]]])

(defn render-not-found [_ _]
  [:h1 "Not found"])

(defn render-page [db]
  (let [;; Turning the location into a map allows us to assoc on it
        location (into {} (ds/entity db :ui/location))
        f (case (:location/page-id location)
            :pages/frontpage render-frontpage
            :pages/episode render-episode
            render-not-found)]
    (f db location)))

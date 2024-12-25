(ns state-datascript.core
  (:require [clojure.walk :as walk]
            [datascript.core :as ds]
            [replicant.dom :as r]
            [state-datascript.ui :as ui]))

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

(defn main [conn el]
  (add-watch
   conn ::render
   (fn [_ _ _ _]
     (r/render el (ui/render-page (ds/db conn)))))

  (r/set-dispatch!
   (fn [event-data actions]
     (->> actions
          (interpolate-actions
           (:replicant/dom-event event-data))
          (execute-actions conn))))

  ;; Trigger the initial render
  (ds/transact! conn [{:db/ident :system/app
                       :app/started-at (js/Date.)}]))

(ns state-datascript.dev
  (:require [datascript.core :as ds]
            [state-datascript.core :as app]
            [state-datascript.schema :as schema]))

(defonce conn (ds/create-conn schema/schema))
(defonce el (js/document.getElementById "app"))

(defn main []
  ;; Add additional dev-time tooling here
  (app/main conn el))

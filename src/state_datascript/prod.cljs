(ns state-datascript.prod
  (:require [datascript.core :as ds]
            [state-datascript.core :as app]
            [state-datascript.schema :as schema]))

(defn main []
  ;; Make production adjustments here
  (app/main (ds/create-conn schema/schema) (js/document.getElementById "app")))

(ns kepler16.clam.router
  (:require [kepler16.clam.util :as clam.util]
            ["react-router-dom" :as rr]))

(defn router [props app]
  [:> (if (clam.util/browser?) rr/BrowserRouter rr/StaticRouter) props
    app])

(defn switch [props & routes]
  (into
   [:> rr/Switch props]
   routes))

(defn route [props & children]
  (into
   [:> rr/Route props]
   children))

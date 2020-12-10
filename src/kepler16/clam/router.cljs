(ns kepler16.clam.router
  (:require [kepler16.clam.util :as clam.util]
            ["react-router-dom" :as rr]))


(defn router [props app]
  [:> (if (clam.util/browser?) rr/BrowserRouter rr/StaticRouter) props
    app])

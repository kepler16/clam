(ns kepler16.clam.router
  (:require [kepler16.clam.util :as clam.util]
            [cljs-bean.core :as bean]
            ["react-router" :as rr]
            ["react-router-dom" :as rr-dom]))

(defn router [props app]
  [:> (if (clam.util/browser?) rr-dom/BrowserRouter rr-dom/StaticRouter) props
    app])

(defn redirect [props]
  (into
   [:> rr-dom/Redirect props]))

(defn switch [props & routes]
  (into
   [:> rr-dom/Switch props]
   routes))

(defn route [props & children]
  (into
   [:> rr-dom/Route props]
   children))

(defn use-location []
  (bean/bean (rr/useLocation)))

(defn use-params []
  (bean/bean (rr/useParams)))

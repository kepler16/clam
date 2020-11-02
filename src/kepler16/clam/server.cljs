(ns kepler16.clam.server
  (:require [uix.dom.alpha :as dom]
            [uix.core.alpha :as uix]
            ["react-dom/server" :as dom-server]
            [kepler16.clam.core :as clam]
            [kepler16.principaltower.website.api.ring :as ring-compat]
            [reitit.ring :as reitit.ring]))

(defn ssr [req doc app]
  (let [location (:uri req)]
    {:status 200
     :headers {"Cache-Control" "max-age=0, s-maxage=86400"}
     :body
     (->> (uix/as-element
           [clam/router {:location location :context {}}
            [doc
             [app]]])
          (.renderToString dom-server))}))

(def api
  (reitit.ring/ring-handler
   (reitit.ring/router
    {"/api/*" (constantly {:body "Welcome to the Clam API"})})
   (reitit.ring/routes
    ssr
    (reitit.ring/create-default-handler))))

(defn handler [^js req ^js res]
  (let [handler (ring-compat/proxy-handler api)]
    (handler req res)))


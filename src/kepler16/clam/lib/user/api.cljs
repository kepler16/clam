(ns kepler16.clam.lib.user.api
  (:require [kepler16.clam.lib.api.core :as clam.api]
            [kepler16.clam.lib.api.ring.node :as clam.ring]
            [reitit.ring :as reitit.ring]
            [site.core :as site]))

(defn api-404 [_]
  {:body "This is not the route you're looking for"})

(def api-router
  (reitit.ring/router
   {"/api/*" api-404}))

(def ssr-handler
  (-> (clam.api/ssr-handler
       [site/root])
      (clam.api/vercel-cache)))

(def handler
  (clam.ring/ring->node-handler
   (reitit.ring/ring-handler
    api-router
    (reitit.ring/routes
     ssr-handler
     (reitit.ring/create-default-handler)))))

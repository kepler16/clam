(ns kepler16.clam.server
  (:require [uix.core.alpha :as uix]
            ["react-dom/server" :as dom-server]
            [kepler16.clam.core :as clam]))

(defn ssr-handler [app]
  (fn [req]
    (let [location (:uri req)]
      {:status 200
       :body
       (->> (uix/as-element
             [clam/router {:location location
                           :context {}}
              app])
            (.renderToString dom-server))})))

(defn vercel-cache [handler]
  (fn [req]
    (-> req
        (handler)
        (assoc-in [:headers "Cache-Control"] "max-age=0, s-maxage=86400"))))

(ns kepler16.clam.client.core
  (:require [uix.core.alpha :as uix]
            [uix.dom.alpha :as uix.dom]
            [kepler16.clam.router :as clam.router]
            ["react-router-dom" :as rr]))

(defn render [node app]
  (let [render-fn  (if (.hasChildNodes node)
                     uix.dom/hydrate
                     uix.dom/render)]
    (render-fn [clam.router/router {} app] node)))

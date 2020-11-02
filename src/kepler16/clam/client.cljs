(ns kepler16.clam.client
  (:require [uix.core.alpha :as uix]
            [uix.dom.alpha :as uix.dom]
            [kepler16.clam.core :as clam]
            ["react-router-dom" :as rr]))

(defn render [node app]
  (let [render-fn  (if (.hasChildNodes node)
                     uix.dom/hydrate
                     uix.dom/render)]
    (render-fn [clam/router {} [app]] node)))

(defn browser-init [app]
  (render (js/document.getElementById "app") app))

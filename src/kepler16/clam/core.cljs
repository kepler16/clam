(ns kepler16.clam.core
  (:require [uix.dom.alpha :as uix.dom]
            [kepler16.clam.router :as router]))

(defn render [node app]
  (let [render-fn  (if (.hasChildNodes node)
                     uix.dom/hydrate
                     uix.dom/render)]
    (render-fn [router/router {} app] node)))

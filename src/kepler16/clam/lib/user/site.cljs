(ns kepler16.clam.lib.user.site
  (:require ["react-router-dom" :as rr]
            [kepler16.clam.core :as clam]
            [site.core :as site]
            ["react-helmet" :as helmet]))

(defn root []
  [:<>
   [site/root]])

(defn render! []
  (clam/render
   (js/document.getElementById "app")
   [root]))

(defn ^:dev/before-load before-load! []
  (js/console.log "reloading..."))

(defn ^:dev/after-load after-load! []
  (render!)
  (js/console.log "reloaded..."))

(defn browser-init! []
  (render!))

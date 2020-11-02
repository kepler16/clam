(ns kepler16.clam.example.client.core
  (:require [kepler16.clam.core :as clam]
            [uix.core.alpha :as uix]
            [uix.dom.alpha :as uix.dom]
            ["react-router-dom" :as rr]
            [kepler16.clam.client.app :as app]
            ;; [kepler16.principaltower.client.auth :as auth]
            [clojure.string :as str]))

(defn app []
   [:> rr/Switch
    [:> rr/Route {:path "/" :exact true}
     [app/component]]])

(defn browser? []
  (exists? ^js js/window))

(defn router [props app]
  [:> (if (browser?) rr/BrowserRouter rr/StaticRouter) props
   app])

(defn render [node]
  (let [render-fn  (if (.hasChildNodes node)
                     uix.dom/hydrate
                     uix.dom/render)]
    (render-fn [router {} [app]] node)))

(defn browser-init []
  (render (js/document.getElementById "app")))
  ;; (analytics/init))

(defn ^:dev/before-load stop []
  (js/console.log "stop "))

(defn ^:dev/after-load start []
  (render (js/document.getElementById "app")))

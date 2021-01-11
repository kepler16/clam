(ns kepler16.clam.lib.user.site
  {:dev/always true}
  (:require ["react-router-dom" :as rr]
            [kepler16.clam.core :as clam]
            [kepler16.clam.router :as router]
            [kepler16.clam.util :as clam.util]
            [kepler16.clam.lib.build.routes :as routes]
            [kepler16.clam.head :as head]
            [kepler16.clam.lib.user.pages :as pages]
            [kepler16.clam.lib.user.dev]))

(def site-data pages/site-data)

(defn not-found []
  [:div "Page not found"])

(defn head []
  [:<>
   [head/Head
    [:title "Clam"]]

   (when-not (clam.util/browser?)
     [head/Head
      [:script {:ssronly "true"
                :defer true
                :src "/static/dist/cljs/main.js"}]])])

(defn default-site-root [page]
  [:<>
   page])

(def site-root (or (some-> site-data :config :site :root) default-site-root))

(defn page []
  (into
   [router/switch {}]
   (concat
    (map
     (fn [{:keys [path component]}]
       [:> rr/Route {:path path :exact true}
        [component]])
     (:pages site-data))
    [[not-found]])))

(defn root []
  [:<>
   [head]
   [site-root
    [page]]])

(defn render! []
  (js/console.log "rendering")
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

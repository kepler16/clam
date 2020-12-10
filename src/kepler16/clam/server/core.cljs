(ns kepler16.clam.server.core
  (:require [uix.core.alpha :as uix]
            ["react-dom/server" :as dom-server]
            [kepler16.clam.router :as clam.router]
            ["react-helmet" :as helmet]
            [clojure.string :as str]
            [cljs-bean.core :as bean]))

(defn- squash-ssronly-data-tags [component-fragment]
  (->> component-fragment
       bean/->clj
       (map (fn [el]
              (if (-> el :props :ssronly)
                (update el :props #(dissoc % :ssronly :data-react-helmet))
                el)))
       bean/->js))


(defn- helmet-document [^js h child]
  [:html (-> h .-htmlAttributes .toComponent js->clj)
   [:head
    [:> #(-> h .-title .toComponent squash-ssronly-data-tags)]
    [:> #(-> h .-meta .toComponent squash-ssronly-data-tags)]
    [:> #(-> h .-script .toComponent squash-ssronly-data-tags)]
    [:> #(-> h .-noscript .toComponent squash-ssronly-data-tags)]
    [:> #(-> h .-link .toComponent squash-ssronly-data-tags)]
    [:> #(-> h .-style .toComponent squash-ssronly-data-tags)]]
   [:body (-> h .-bodyAttributes .toComponent js->clj)
    child]])

(defn ssr-handler [app]
  (fn [req]
    (let [location (:uri req)
          app (->> (uix/as-element
                    [clam.router/router {:location location
                                         :context {}}
                     app])
                   (.renderToString dom-server))
          h (helmet/Helmet.renderStatic)
          document-container (->> (uix/as-element
                                   [helmet-document h [:clam]])
                                  (.renderToString dom-server))
          document (str/replace document-container #"<clam></clam>" app)]
      {:status 200
       :body document})))

(defn vercel-cache [handler]
  (fn [req]
    (-> req
        (handler)
        (assoc-in [:headers "Cache-Control"] "max-age=0, s-maxage=86400"))))

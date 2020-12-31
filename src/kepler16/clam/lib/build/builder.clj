(ns kepler16.clam.lib.build.builder
  (:require
   [shadow.cljs.devtools.server :as server]
   [shadow.cljs.devtools.api :as cljs]))

(defn build-hook
  {:shadow.build/stage :flush}
  [build-state & args]
  (def build-state build-state)
  (def args args)
  build-state)

(def build-site
  {:build-id :clam/site
   :target :browser
   :output-dir "public/static/dist/cljs"
   :asset-path "/static/dist/cljs"
   :build-hooks ['(kepler16.clam.lib.build.builder/build-hook)]
   :modules {:main {:entries ['kepler16.clam.lib.user.site]
                    :init-fn 'kepler16.clam.lib.user.site/browser-init!}}})

(def build-api
  {:build-id :clam/api
   :target :node-library
   :output-to "api/dist/handler.js"
   :exports-var 'kepler16.clam.lib.user.api/handler})

;; (load-pages! "pages")

;; (cljs/compile* build-site {})

(defn release [{}]
  (server/start!)
  (cljs/release* build-api {})
  (cljs/release* build-site {})
  (System/exit 0))

(defn watch [{}]
  (server/start!)

  (cljs/watch* build-api {})
  (cljs/watch* build-site {})
  ::watching)

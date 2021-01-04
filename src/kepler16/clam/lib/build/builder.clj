(ns kepler16.clam.lib.build.builder
  (:require
   [shadow.cljs.devtools.server :as server]
   [shadow.cljs.devtools.api :as cljs]
   [integrant.core :as ig]
   [juxt.dirwatch :as dirwatch]
   [clojure.java.io :as io]
   [clojure.core.async :as a]
   [kepler16.clam.lib.build.routes :as routes]
   [clojure.string :as str]))

(defn build-hook
  {:shadow.build/stage :flush}
  [build-state & args]
  (def build-state build-state)
  (def args args)
  build-state)

(def build-site
  `{:build-id :clam/site
    :target :browser
    :output-dir "public/static/dist/cljs"
    :asset-path "/static/dist/cljs"
    :build-hooks [(kepler16.clam.lib.build.builder/build-hook)]
    :modules {:main {:entries [kepler16.clam.lib.user.site]
                     :init-fn kepler16.clam.lib.user.site/browser-init!}}})

(def build-api
  `{:build-id :clam/api
    :target :node-library
    :output-to "api/dist/handler.js"
    :exports-var kepler16.clam.lib.user.api/handler})

(def build-api-esm

  `{:build-id :clam/esm
    :target :esm
    :output-dir "dist/esm"
    :devtools {:loader-mode :eval}
    :modules {:handler {:exports {default kepler16.clam.lib.user.api/handler}}}})

(def system-config-defaults
  `{:deps true
    :cache-blockers #{kepler16.clam.lib.user.site
                      kepler16.clam.lib.user.pages}})

(defn load-shadow-config []
  (merge
   system-config-defaults
   (server/load-config)))


(defmethod ig/init-key :clam/file-watcher [_ {:keys [dir]}]
  (let [c (a/chan (a/dropping-buffer 100))]
    {:watcher
     (dirwatch/watch-dir #(a/put! c %) (io/file dir))
     :<events c}))

(defmethod ig/halt-key! :clam/file-watcher [_ {:keys [watcher <events]}]
  (a/close! <events)
  (dirwatch/close-watcher watcher))

(defn generate-pages-template-override []
  (let [pages-template (slurp (io/resource "kepler16/clam/lib/user/pages.cljsx"))
        pages (routes/load-pages! "pages")]
    (str/replace
     pages-template
     #"\[replace\.deps\]"
     (->> pages
          (map :entry)
          (distinct)
          (map #(str "[" % "]\n"))
          (apply str)
          (str ";; GENERATED - " (rand-int 1000000) "\n")))))

;; (generate-pages-template-override)

(defmethod ig/init-key :clam/dev-modules-file [_ {:keys [file-watcher]}]
  (a/go-loop []
    (try
      (let [pages-file ".clam/cp/kepler16/clam/lib/user/pages.cljs"
            pages-override (generate-pages-template-override)]

        (io/make-parents pages-file)
        (spit pages-file pages-override))
      (catch Exception e
        (println "Issue generating pages")
        (println e)))
    (when (a/<! (:<events file-watcher))
      (recur))))

(defmethod ig/init-key :clam/build-watcher [_ {:keys [config options]}]
  (cljs/watch* config options))

(defmethod ig/halt-key! :clam/build-watcher [_ {:keys [build-id]}]
  (cljs/stop-worker build-id))

(defmethod ig/init-key :clam/shadow-server [_ {}]
  (server/start! (load-shadow-config)))

(defn watch-system []
  {:clam/shadow-server {}
   :clam/file-watcher {:dir "pages"}
   [:clam/build-watcher :clam/site] {:config build-site
                                     :options {}}
   [:clam/build-watcher :clam/api] {:config build-api
                                    :options {}}

   :clam/dev-modules-file {:file-watcher (ig/ref :clam/file-watcher)}})

(defn release [{}]
  (let [system (ig/init {:clam/shadow-server {}})]
    (cljs/release* build-api {})
    (cljs/release* build-site {})
    (ig/halt! system)
    (System/exit 0)))

(defonce system* (atom nil))

(defn stop []
  (when @system*
    (ig/halt! @system*)))

(defn watch [{}]
  (stop)

  (let [system (ig/init (watch-system))]
    (reset! system* system))

  ::watching)

(comment
  (stop)
  (watch {}))

(ns kepler16.clam.lib.build.builder
  (:require
   [shadow.cljs.devtools.server :as server]
   [shadow.cljs.devtools.api :as cljs]
   [shadow.cljs.devtools.server.fs-watch :as fs-watch]
   [integrant.core :as ig]
   [clojure.java.io :as io]
   [clojure.core.async :as a]
   [kepler16.clam.lib.build.analyze :as clam-analyze]
   [clojure.pprint :as pprint]
   [me.raynes.fs :as fs]
   [jsonista.core :as json]))

(defn clean-site-data [site-data]
  (-> site-data
      (update
       :pages
       (fn [pages]
         (->> pages
              (map #(dissoc % :var-def))
              (into []))))))

(defn pages->requires []
  (let [site-data (clam-analyze/analyze-site-data!)]
    (str "(ns kepler16.clam.lib.user.pages (:require "
              (->> (:pages site-data)
                   (map :entry)
                   (concat (clam-analyze/discover-entries (io/file "pages")))
                   (concat [(some-> site-data :config :site :root namespace)])
                   (filter identity)
                   (distinct)
                   (map #(str "[" % "]\n"))
                   (apply str))
         "))\n"
         "(def site-data " (clean-site-data site-data) ")")))

(defn write-pages! []
  (try
    (let [dev-file ".clam/cp/kepler16/clam/lib/user/pages.cljs"
          dev-override (pages->requires)]
      (io/make-parents dev-file)
      (spit dev-file dev-override))
    (catch Exception e
      (println "Issue generating pages")
      (println e))))

(defn development-requires []
  (let [namespaces (clam-analyze/discover-entries (io/file "pages"))]
    (with-out-str
      (pprint/pprint
       `(~'ns kepler16.clam.lib.user.dev
         (:require ~@(map vector namespaces)))))))

(defn write-dev-modules! []
  (try
    (let [dev-file ".clam/cp/kepler16/clam/lib/user/dev.cljs"
          dev-override (development-requires)]
      (io/make-parents dev-file)
      (spit dev-file dev-override))
    (catch Exception e
      (println "Issue generating pages")
      (println e))))

(def build-site
  `{:build-id :clam/site
    :target :browser
    :output-dir "public/static/dist/cljs"
    :asset-path "/static/dist/cljs"
    :modules {:main {:entries [kepler16.clam.lib.user.site]
                     :init-fn kepler16.clam.lib.user.site/browser-init!}}})

(def build-api
  `{:build-id :clam/api
    :target :node-library
    :js-options {:js-provider :shadow
                 :keep-native-requires true}
    :output-to ".clam/builds/api/dist/handler.js"
    :exports-var kepler16.clam.lib.user.api/handler})

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
    {:watcher (fs-watch/start {} [(io/file dir)] ["cljs"] #(a/put! c %))
     :<events c}))

(defmethod ig/halt-key! :clam/file-watcher [_ {:keys [watcher <events]}]
  (a/close! <events)
  (fs-watch/stop watcher))


(defmethod ig/init-key :clam/dev-modules-file [_ {:keys [file-watcher]}]
  (a/go-loop []
    (write-dev-modules!)
    (write-pages!)
    (when-let [e (a/<! (:<events file-watcher))]
      (println "Pages directory changed: analyzing")
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
   :clam/dev-modules-file {:server (ig/ref :clam/shadow-server)
                           :file-watcher (ig/ref :clam/file-watcher)}})

(defn vercel-build! []
  ;; prep
  (fs/delete-dir ".vercel_build_output")
  (fs/mkdirs ".vercel_build_output")

  ;; static
  (fs/copy-dir (io/file "public") (io/file ".vercel_build_output/static"))

  ;; serverless
  (fs/mkdirs ".vercel_build_output/functions/node/renderer")
  (fs/copy (io/file ".clam/builds/api/dist/handler.js") (io/file ".vercel_build_output/functions/node/renderer/index.js"))
  ;; (fs/copy-dir (io/file "node_modules") (io/file ".vercel_build_output/functions/node/renderer/node_modules"))

  ;; routes
  (let [routes
        [{:handle "filesystem"}
         {:src "/(.*)"
          :dest "/.vercel/functions/renderer/index"}]]

    (fs/mkdirs ".vercel_build_output/config")
    (->> routes
         (json/write-value-as-string)
         (spit (io/file ".vercel_build_output/config/routes.json")))))

(defn release [{}]
  (let [system (ig/init {:clam/shadow-server {}})]
    (write-dev-modules!)
    (write-pages!)
    (cljs/release* build-api {})
    (cljs/release* build-site {})
    (vercel-build!)
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

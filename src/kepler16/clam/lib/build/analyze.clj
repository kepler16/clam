(ns kepler16.clam.lib.build.analyze
  (:require [kepler16.clam.lib.build.page :as page]
            [shadow.cljs.devtools.api :as shadow]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defn build-state->all-vars [state]
  (for [[ns ns-info] (get-in state [:compiler-env :cljs.analyzer/namespaces])
        ns-def (-> ns-info :defs vals)]
    ns-def))

(defn build-state->page-vars [build-state]
  (for [v (build-state->all-vars build-state)
         :when (get-in v [:meta :clam/page])]
    v))

(defn var->page [v]
  (let [path (or (some-> v :meta :clam/path)
                 (page/file-path->url-path (:file v)))]
    {:var-def v
     :component (:name v)
     :path path}))

(defn read-config! []
  (try
    (read-string (slurp "./clam.edn"))
    (catch Exception _ nil)))


(defn throw-build-state!
  {:shadow.build/stage :compile-finish}
  [state]
  (throw (ex-info "<build-state>" state)))

(defn build-state* [build-config options]
  (try
    (shadow/compile*
     (merge
      build-config
      {:build-hooks [`(kepler16.clam.lib.build.analyze/throw-build-state!)]})
     options)
    (catch Exception e
      (if (= "<build-state>" (ex-message (ex-cause e)))
        (ex-data (ex-cause e))
        (throw e)))))

(defn analyse-entries [entries]
  (build-state*
   {:build-id :clam/analyze-pages
    :target :browser
    :modules {:main {:entries entries}}}
   {}))

(defn discover-entries [root]
  (->> (file-seq root)
       (map (fn [file]
              (-> (.toPath (io/file root))
                  (.relativize (.toPath file))
                  (str))))
       (filter #(str/ends-with? % ".cljs"))
       (filter #(not (str/starts-with? % ".#")))
       (map #(str/replace % #".cljs$" ""))
       (map #(str/replace % #"/" "."))
       (map symbol)
       (into [])))

(defn discover-pages [root]
  (->> (discover-entries root)
       analyse-entries
       build-state->page-vars
       (map var->page)))

(defn analyze-site-data! []
  {:config (read-config!)
   :pages (discover-pages (io/file "pages"))})

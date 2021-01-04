(ns kepler16.clam.lib.build.routes
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn drop-trailing-edn [path]
  (str/replace path #".edn$" ""))

(defn drop-trailing-index [path]
  (str/replace path #"index$" ""))

(defn drop-trailing-slash [path]
  (str/replace path #"(.+)(/$)" "$1"))

(defn file-path->url-path [file-path]
  (->> (str "/" file-path)
       drop-trailing-edn
       drop-trailing-index
       drop-trailing-slash))

(defn load-page! [root file]
  (try
    (let [path (-> (.toPath (io/file root))
                   (.relativize (.toPath file))
                   (str))
          page-data (read-string (slurp file))]
      {:file file
       :file-path path
       :url-path (file-path->url-path path)
       :page-data page-data
       :entry (namespace (:component page-data))})
    (catch Exception e nil)))

(defn load-config! []
  (read-string (slurp "./clam.edn")))

(defn load-pages! [root]
  (->> (io/file root)
       (file-seq)
       (filter #(.isFile %))
       (map #(load-page! root %))
       (filter map?)
       (map #(dissoc % :file))
       (into [])))

(defmacro load-pages!* [root]
  (load-pages! root))

(defmacro inline-clam-site-data! [root]
  {:pages (load-pages! root)
   :config (load-config!)})

(defn user-require [[]]
  ['kepler16.clam.lib.build.routes])

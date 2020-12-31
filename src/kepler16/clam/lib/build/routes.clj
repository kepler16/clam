(ns kepler16.clam.lib.build.routes
	(:require [clojure.java.io :as io]))

(defn file-path->url-path [file-path]
  file-path)

(defn load-page! [root file]
  (let [path (-> (.toPath (io/file root))
                 (.relativize (.toPath file))
                 (str))
        page-data (read-string (slurp file))]
    {:file file
     :file-path path
     :url-path (file-path->url-path path)
     :page-data page-data
     :entry (symbol (namespace (:component page-data)))}))

(defn load-pages! [root]
  (->> (io/file root)
       (file-seq)
       (filter #(.isFile %))
       (map #(load-page! root %))))

(defmacro load-pages-client! [root]
  (load-pages! root))

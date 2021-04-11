(ns kepler16.clam.lib.build.page
  (:require [clojure.string :as str]))


(defn drop-trailing-cljs [path]
  (-> path
      (str/replace #".cljs$" "")
      (str/replace #".cljc$" "")))

(defn drop-trailing-index [path]
  (str/replace path #"index$" ""))

(defn drop-trailing-slash [path]
  (str/replace path #"(.+)(/$)" "$1"))

(defn file-path->url-path [file-path]
  (->> (str "/" file-path)
       drop-trailing-cljs
       drop-trailing-index
       drop-trailing-slash))

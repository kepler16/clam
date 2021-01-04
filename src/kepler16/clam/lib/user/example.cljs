(ns kepler16.clam.lib.user.example
  (:require [kepler16.clam.head :as head]))

(defn head []
  [head/Head
   [:html {:lang "en"}]
   [:meta {:charset "UTF-8"}]
   [:meta {:content "width=device-width, initial-scale=1", :name "viewport"}]

   [:title "Clam Template"]
   [:meta {:name "Description" :content "Clam Template"}]
   [:meta {:name "robots" :content "index, follow"}]])

(defn root [page]
  [:<>
   [head]
   page])

(defn index []
  [:div "INDEX"])

(defn a []
  [:div "Page A"])

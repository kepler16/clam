(ns kepler16.clam.head
  (:require ["react-helmet" :as helmet]))

(defn Head [& children]
  (into [:> helmet/Helmet] children))

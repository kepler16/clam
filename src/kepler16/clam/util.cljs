(ns kepler16.clam.util)

(defn browser? []
  (exists? ^js js/window))

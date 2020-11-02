(ns kepler16.clam.core
  (:require [uix.core.alpha :as uix]
            [uix.dom.alpha :as uix.dom]
            ["react-router-dom" :as rr]))

(defn browser? []
  (exists? ^js js/window))

(defn router [props app]
	[:> (if (browser?) rr/BrowserRouter rr/StaticRouter) props
	 app])

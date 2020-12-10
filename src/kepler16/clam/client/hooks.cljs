(ns kepler16.clam.client.hooks
  (:require [uix.core.alpha :as uix]))

(defn use-first-render []
  (let [first-render? (uix/state true)]
    (uix/with-effect []
      (reset! first-render? false))
    @first-render?))

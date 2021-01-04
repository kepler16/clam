(ns kepler16.clam.lib.cli.death
  (:require ["process" :as process]))

(defn on-death! [callback]
  (process/on "SIGINT" callback)
  (process/on "SIGTERM" callback)
  (process/on "SIGQUIT" callback))

(defn kill-process-on-death! [^js process]
  (on-death! (fn [] (.kill process))))

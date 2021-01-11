(ns dev
  (:require [kepler16.clam.lib.build.builder]))

(defn reset []
  (kepler16.clam.lib.build.builder/watch {}))

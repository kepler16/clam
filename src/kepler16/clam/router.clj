(ns kepler16.clam.router)

(defmacro switch* [rr-switch props-or-route & routes]
  (into
   [:> rr-switch props-or-route]
   routes))

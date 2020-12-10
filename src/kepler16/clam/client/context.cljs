(ns kepler16.clam.client.context)

(defn context-provider [[ctx value] & children]
  (into [:> (.-Provider ^js ctx) {:value value}]
        children))

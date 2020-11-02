(ns kepler16.clam.ring.node
  (:require
   [clojure.string :as str]
   ["http" :as http]
   ["url" :as url]))

(defn- build-request-map
  "Create the request map from the ServerRequest object."
  [request]
  (let [parsed-url (.parse url (.-url request))
        body (.read request)]
    {:server-port        3000
     :server-name        nil
     :remote-addr        nil
     :uri                (.-pathname parsed-url)
     :query-string       (.-search parsed-url)
     :scheme             (keyword "http")
     :request-method     (keyword (str/lower-case (.-method request)))
     :headers            (js->clj (.-headers request))
     :ssl-client-cert    nil
     :body               body}))

(defn- set-status
  "Update a ServerResponse with a status code."
  [response status]
  (.writeHead ^js response status))

(defn- set-headers
  "Update a ServerResponse with a map of headers."
  [response headers]
  (doseq [[key val-or-vals] headers]
    (if (string? val-or-vals)
      (.setHeader ^js response key val-or-vals)
      (doseq [val val-or-vals]
        (.addHeader ^js response key val)))))

(defn- set-body
  "Update a ServerResponse body with a String or ISeq."
  [response body]
  (.end response (if (seq? body) (str/join body) body)))

(defn update-response
  "Update the ServerResponse using a response map."
  {:arglists '([response response-map])}
  [response {:keys [status headers body]}]
  (doto response
    (set-headers headers)
    (set-status (or status 200))
    (set-body body)))

(defn proxy-handler
  "Returns a NodeJS Server handler for the given ring handler."
  [handler]
  (fn [request response]
    (let [request-map (build-request-map request)
          response-map (handler request-map)]
      (when response-map
        (update-response response response-map)))))

(defn run-server [handler {:keys [port]}]
  (let [server (.createServer http (proxy-handler handler))]
    (.listen server port)))

(defn handler [req]
  (js/console.log req)
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (js/JSON.stringify (clj->js {:foo "bar!"}))})

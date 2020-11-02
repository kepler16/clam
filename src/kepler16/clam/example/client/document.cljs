(ns kepler16.principaltower.website.document)

(defn document [child]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta
     {:content "width=device-width, initial-scale=1", :name "viewport"}]
    [:title "Principal Tower"]
    [:meta
     {:content
      "",
      :name "Description"}]
    [:link {:href "/static/assets/css/normalize.css", :rel "stylesheet"}]
    [:style
     "@font-face {
				font-family: D-DIN Condensed;
				font-weight: 300;
				src: url(/static/assets/fonts/D-DINCondensed.otf);
      }
			@font-face {
				font-family: D-DIN Condensed;
				font-weight: 700;
				src: url(/static/assets/fonts/D-DINCondensed-Bold.otf);
      }
			body {
				font-family: D-DIN Condensed;
			}"]]
   [:body {:lang "en"}
    [:div#app
     child]]
   [:script {:src "/static/assets/app/js/main.js", :type "text/javascript"}]])

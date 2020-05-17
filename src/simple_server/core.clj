(ns simple-server.core
  (:require [clojure.pprint]
            [ring.adapter.jetty :refer [run-jetty]]

            [ring.util.response :refer [response created redirect not-found status content-type set-cookie]]
            [compojure.core :refer [GET POST ANY defroutes context]]
            [compojure.coercions :refer [as-int]]
            [ring.middleware.defaults :as middleware]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [hiccup.core :refer [html]]

            [ring.mock.request :as mock]
            [simple-server.simple-game :as game]))

;;;;;;;;;;;;;;;;;;;;;;;;;;
;; refer lesson 8 solution

(defn login-handler
  []
  (response
   (html
    [:body
     [:form {:method :post}
      [:h1 "Please enter your name to play the game"]
      [:input {:type :text :name :username}]]])))

(defn successful-login-handle 
  [username]
  (-> (redirect "/new-game.html")
      (set-cookie "auth" username)))

(defn new-game-handler
  [username]
  (when (game/new-game! username)
    (response
     (html
      [:body
       [:h1 "Welcome to the game!"]
       [:h3 "Ok! Start guessing "
        [:a {:href "/guess.html"} "here"]]]))))

(defn guess-handler
  [guess username]
  (let [form [:div
              [:h1 "Enter your guess"]
              [:form {:method :post}
               [:input {:type :text :name :guess}]]]
        guess (as-int guess)]
    (response
     (html
      [:body
       (condp = (game/guess-answer guess username)
         nil           form
         :correct      [:h2 "You win!"]
         :game-over    [:h2 "You lose!"]
         :too-low      [:div [:h2 "Too low"] form]
         :too-high     [:div [:h2 "Too high"] form]
         :unauthorized [:div
                        [:h1 "You must login first"]
                        [:a {:href "login.html"} "Login here"]])]))))

(defn ensure-auth-cookie [handler]
  (fn [request]
    (if-let [cookie (get-in request [:cookies "auth"])]
      (handler (-> request
                   (assoc :username (:value cookie))))
      {:status 403
       :header {"Content-Type" "text/html"}
       :body (html
              [:body
               [:h1 "You must login first"]
               [:a {:href "login.html"} "Login here"]])})))

(defroutes game-routes
  (GET "/new-game.html" {username :username} (new-game-handler username))
  (GET "/guess.html"    {username :username} (guess-handler nil username))
  (POST "/guess.html"   
        [guess :as req] 
        (guess-handler guess (:username req))))

(defroutes html-routes
  (GET "/login.html"  []         (login-handler))
  (POST "/login.html" [username] (successful-login-handle username))
  (-> game-routes
      (ensure-auth-cookie)))

(def localhost-defaults (dissoc middleware/site-defaults :security))

(defroutes handler
  (-> html-routes
      (middleware/wrap-defaults localhost-defaults)
      (wrap-content-type))
  (ANY "*" [] (not-found "Sorry, No such URI on this server!")))

(defonce server
  (run-jetty #'handler {:port 3001 :join? false}))

:core



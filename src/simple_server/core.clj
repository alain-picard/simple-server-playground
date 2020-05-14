(ns simple-server.core
  (:require [clojure.pprint]
            [ring.adapter.jetty :refer [run-jetty]]

            [ring.util.response :refer [response created redirect not-found status content-type]]
            [compojure.core :refer [GET POST ANY defroutes]]
            [compojure.coercions :refer [as-int]]
            [ring.middleware.defaults :as middleware]

            [ring.mock.request :as mock]
            [simple-server.simple-game :as game]))

;;; Finally, let us truly separate concerns between our "application code"
;;; and our "http code".  Our game now lives in its own namespace, and
;;; is fully testable independent of our "presentation layer".

(defn new-game-handler []
  (when (game/new-game!)
    (response "Ok! start guessing at /new-game/guess")))

(defn guess-handler [guess]
  (condp = (game/guess-answer guess)
    nil        (-> (response  "You need to supply a guess with /guess?guess=N")
                   (status 400))
    :correct   (response "Congratulations! You win!")
    :game-over (response "Game over! You lose")
    :too-low   (response "Too low.")
    :too-high  (response "Too high.")))

(defn authorization-handler
  ([id]
   (condp = (game/authorize-user id)
     nil          (-> (response "Please provide your name to enter the game")
                      (status 401))
     :user-exists (response "Username already exists! Please choose another name")
     :authorized  (response "Ok! Start guessing at /auth-game/<yourname>/guess?guess=<number>")))
  
  ;; handle a guess from an authorized player
  ([id guess]
   (condp = (game/user-guess id guess)
     nil           (-> (response  "You need to provide your name and a guess at /auth-game/<yourname>/guess?guess=<yourguess>")
                       (status 400))
     :unauthorized (-> (response  "Unauthorized! Please register your name to start the game at /auth-game/user/<yourname>")
                       (status 401))
     :correct      (response "Congratulations! You win!")
     :game-over    (response "Game over! You lose!")
     :too-low      (response "Too low!")
     :too-high     (response "Too high!"))))

(defroutes game-routes
  #_
  (GET "/new-game"       
       []
       (new-game-handler))
  #_
  (GET "/new-game/guess" 
       [guess :<< as-int] 
       (guess-handler guess))
  
  (GET "/"
       []
       (response "Please register your name to start the game at /auth-game/user/<yourname>"))
  (GET "/auth-game/user/:id" 
       [id] 
       (authorization-handler id))
  (GET "/auth-game/:id/guess" 
       [guess :<< as-int id] 
       (authorization-handler id guess))
  (ANY "*" 
       []                 
       (not-found "Sorry, No such URI on this server!")))

(def handler
  (-> game-routes
      (middleware/wrap-defaults middleware/api-defaults)))

#_
(comment
  (handler (mock/request :get "/auth-game/user/pat"))
  (handler (mock/request :get "/auth-game/pat/guess?guess=8"))
  (handler (mock/request :get "/auth-game/user/peter"))
  (handler (mock/request :get "/auth-game/peter/guess?guess=5"))
  ; if a user try to hijack the game?
  (handler (mock/request :get "/auth-game/batman/guess?guess=5")))

(defonce server
  (run-jetty #'handler {:port 3001 :join? false}))

:core



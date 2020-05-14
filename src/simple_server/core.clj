(ns simple-server.core
  (:require [clojure.pprint]
            [ring.adapter.jetty :refer [run-jetty]]

            [ring.util.response :refer [response created redirect not-found status]]
            [compojure.core :refer [GET POST ANY defroutes]]
            [compojure.coercions :refer [as-int]]
            [ring.middleware.defaults :as middleware]

            [ring.mock.request :as mock]
            [simple-server.simple-game :as game]))

;;; Finally, let us truly separate concerns between our "application code"
;;; and our "http code".  Our game now lives in its own namespace, and
;;; is fully testable independent of our "presentation layer".

;; An abstraction for accounts in the database
(def users (atom #{"ray"}))

;; An abstraction for currently logged in accounts
(def current-users (atom #{}))

(defn landing-handler []
  ;; The landing page of the game, with instructions to tell user to either sign up or log in
  (response (str "Welcome to Guessing the Number! "
                 "Head to /sign-up?account=\"your account\" to sign up if this is your first time visiting. "
                 "Or go to /log-in?account=\"your account\" to log in to start playing the game!")))

(defn sign-up-handler [account]
  ;; Check if the user already exists, if not, create it, otherwise, tell the user to log in
  (if (nil? (some #{account} @users))
    (do
      (swap! users conj account)
      (response (str "Thank you for joining us! " account ". You may now log in to your page at /login?account=" account)))
    (response (str  "You are already with us " account ", log in to your page at /login?account=" account))))

(defn log-in-handler [account]
  ;; Check if the user is in our database, if not, tell the user to sign up, otherwise, log the user in
  (if (nil? (some #{account} @users))
    (response "You are not a member of us yet, pleaes sign up at /sign-up?account=\"your account\".")
    (do
      (swap! current-users conj account)
      (response (str "Welcome back " account ", head to your page at /" account)))))

(defn account-handler [account]
  ;; Check if the user is logged in, if not, tell the user to log in, otherwise, welcome the user
  (if (nil? (some #{account} @current-users))
    (response "Please first log in via /log-in?account=\"your account\"")
    (response (str "Hello " account ". Start your game at /" account "/new-game"))))

(defn new-game-handler [account]
  ;; Check if the user is logged in, if not, tell the user to log in, otherwise, instruct the user to start the game
  (if (nil? (some #{account} @current-users))
    (response "You must be logged-in to access your account. Please log-in at /log-in?account=" account)
    (do
      (when (game/new-game!)
        (response (str "OK- start guessing at /" account "/guess?guess=x."))))))

(defn guess-handler [guess account]
  ;; Players must be a logged in user in order to play the game
  (if (nil? (some #{account} @current-users))
    (response "You must be logged-in before playing the game. Please head to /log-in?account=\"your account\".")
    (do
      (condp = (game/guess-answer guess)
        nil       (-> (response  "You need to supply a guess with /guess?guess=N")
                      (status 400))
        :you-win   (response (str "Congratulations! You win " account "! Play again at /" account "/new-game"))
        :you-lose  (response (str "Sorry, you ran out of guesses " account ". It's GAME OVER! Play again at /" account "/new-game"))
        :too-low   (response (str "Too low " account ", " (get-in @game/game-in-progress [:guesses]) " guesses left."))
        :too-high  (response (str "Too high " account ", " (get-in @game/game-in-progress [:guesses]) " guesses left."))))))

(defroutes game-routes
  (GET "/landing"  [account]          (assoc-in (landing-handler) [:headers "Content-type"] "text/html"))  
  (GET "/sign-up"  [account]          (assoc-in (sign-up-handler account) [:headers "Content-type"] "text/html"))
  (GET "/log-in"   [account]          (assoc-in (log-in-handler account) [:headers "Content-type"] "text/html"))
  (GET "/:account" [account]          (assoc-in (account-handler account) [:headers "Content-type"] "text/html"))
  (GET "/:account/new-game" [account] (assoc-in (new-game-handler account) [:headers "Content-type"] "text/html"))
  (GET "/:account/guess"    [guess :<< as-int account] (assoc-in (guess-handler guess account) [:headers "Content-type"] "text/html"))
  (ANY "*"         []                 (not-found "Sorry, No such URI on this server!")))

(def handler
  (-> game-routes
      (middleware/wrap-defaults middleware/api-defaults)))

(comment
  (handler (mock/request :get "/new-game"))
  (handler (mock/request :get "/guess?guess=3"))
  (handler (mock/request :get "/dunno")))

(defonce server
  (run-jetty #'handler {:port 3001 :join? false}))

:core


(ns simple-server.core
  (:require [clojure.pprint]
            [ring.adapter.jetty :refer [run-jetty]]

            [ring.util.response :refer [response created redirect not-found status]]
            [compojure.core :refer [GET POST ANY defroutes]]
            [compojure.coercions :refer [as-int]]
            [ring.middleware.defaults :as middleware]

            [ring.mock.request :as mock]
            [simple-server.simple-game :as game]
            [simple-server.database :as game-db]))

;;; Finally, let us truly separate concerns between our "application code"
;;; and our "http code".  Our game now lives in its own namespace, and
;;; is fully testable independent of our "presentation layer".

;; An abstraction for accounts in the database
;;(def users (atom #{"ray"}))

;; An abstraction for currently logged in accounts
;;(def current-users (atom #{}))

;; An abstraction for current game in progress for logged in accounts
;;(def current-games (atom {}))

;; listing tables in the database
(game-db/list-tables)

(game-db/is-user? "Ray")

(defn logged-in? [account]
  (contains? @game/games-in-progress account))

(logged-in? "Ray")
(logged-in? "Fdfsafsa")

(defn landing-handler []
  ;; The landing page of the game, with instructions to tell user to either sign up or log in
  (response (str "Welcome to Guessing the Number! "
                 "Head to /sign-up?account=\"your account\" to sign up if this is your first time visiting. "
                 "Or go to /log-in?account=\"your account\" to log in to start playing the game!")))

(defn sign-up-handler [account]
  ;; Check if the user is in database, tell the user to log in, otherwise, sign the user up
  (if (game-db/is-user? account)
    (response (str  "You are already with us " account ", log in to your page at /login?account=" account))
    (do
      ;;adding the user to our database
      (game-db/record-a-game account 0 0)
      (response (str "Thank you for joining us! " account ". You may now log in to your page at /login?account=" account)))))

(defn log-in-handler [account]
  ;; Check if the user is in our database, log the user, the random number and guesses remaining  in current-user, otherwise, tell the user to sign up
  (if (game-db/is-user? account)
    (do
      (let [user-game (game-db/get-num-guesses account)
            user-num (get-in user-game [:item :random-number])
            user-guesses (get-in user-game [:item :guesses-remain])]
        (swap! game/games-in-progress assoc account [user-num user-guesses]))
      (response (str "Hello " account "! Head to your page at /" account)))
    (response "You are not a member of us yet, pleaes sign up at /sign-up?account=\"your account\".")))

(defn account-handler [account]
  ;; Check if the user is logged in, then return depends on the user's game status, otherwise, tell the user to log in
  (if (logged-in? account)
    (do
      (let [user-num (get-in @game/games-in-progress [account 0])
            user-guesses (get-in @game/games-in-progress [account 1])]
        (cond
          (= 0 (or user-num user-guesses)) (response (str "Welcome to this game " account ". Start a new game at /" account "/new-game"))
          (<= user-guesses 0) (response (str "Welcome back " account ". You finished your last game, start again at /" account "/new-game" user-guesses))
          (not= user-guesses 0) (response (str "Welcome back " account ". Continue your last game and make your next guess at /" account "/guess?guess=x." user-guesses)))))
    (response "Please first log in via /log-in?account=\"your account\"")))

(defn new-game-handler [account]
  ;; Check if the user is logged in, then update the random number and guesses in current-games, otherwise, tell the user to log in
  (if (logged-in? account)
    (do
      (when (game/new-game! account)
        (let [user-game (get @game/games-in-progress account)
              user-num (get user-game 0)
              user-guesses (get user-game 1)]
          (swap! game/games-in-progress assoc account [user-num user-guesses])
          (response (str "OK- start guessing at /" account "/guess?guess=x.")))))
    (response (str "You must be logged-in to access your account. Please log-in at /log-in?account=" account))))

(defn guess-handler [account guess]
  ;; Players must be a logged in user in order to play the game
  (if (logged-in? account)
    (do
      (condp = (game/guess-answer account guess)
        nil       (-> (response  "You need to supply a guess with /guess?guess=N")
                      (status 400))
        :you-win   (response (str "Congratulations! You win " account "! Play again at /" account "/new-game"))
        :you-lose  (response (str "Sorry, you ran out of guesses " account ". It's GAME OVER! Play again at /" account "/new-game"))
        :too-low   (response (str "Too low " account ", " (get-in @game/games-in-progress [account 1]) " guesses left."))
        :too-high  (response (str "Too high " account ", " (get-in @game/games-in-progress [account 1]) " guesses left."))))
    (response "You must be logged-in before playing the game. Please head to /log-in?account=\"your account\".")))

(defn log-out-handler [account]
  ;; Now we records account's random number and guesses left to the database, and remove account from games-in-progress
  (if (logged-in? account)
    (do
      (let [user-game (get @game/games-in-progress account)
            user-num (get user-game 0)
            user-guesses (get user-game 1)]
        (game-db/record-a-game account user-num user-guesses)
        (swap! game/games-in-progress dissoc account))
      (response (str "See you soon " account "!")))
    (response "You must be logged-in in order to log out...?")))


(defroutes game-routes
  (GET "/landing"  [account]          (assoc-in (landing-handler) [:headers "Content-type"] "text/html"))  
  (GET "/sign-up"  [account]          (assoc-in (sign-up-handler account) [:headers "Content-type"] "text/html"))
  (GET "/log-in"   [account]          (assoc-in (log-in-handler account) [:headers "Content-type"] "text/html"))
  (GET "/:account" [account]          (assoc-in (account-handler account) [:headers "Content-type"] "text/html"))
  (GET "/:account/new-game" [account] (assoc-in (new-game-handler account) [:headers "Content-type"] "text/html"))
  (GET "/:account/guess"    [guess :<< as-int account] (assoc-in (guess-handler guess account) [:headers "Content-type"] "text/html"))
  (GET "/logout" [account] (assoc-in (log-out-handler account) [:headers "Content-type"] "text/html"))
  (ANY "*"         []                 (not-found "Sorry, No such URI on this server!")))

(def handler
  (-> game-routes
      (middleware/wrap-defaults middleware/api-defaults)))

(defonce server
  (run-jetty #'handler {:port 3001 :join? false}))

:core


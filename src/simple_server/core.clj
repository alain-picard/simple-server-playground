(ns simple-server.core
  (:require [clojure.pprint]
            [ring.adapter.jetty :refer [run-jetty]]

            [ring.util.response :refer [response created redirect not-found status content-type]]
            [compojure.core :refer [GET POST PUT ANY defroutes]]
            [compojure.coercions :refer [as-int]]
            [ring.middleware.defaults :as middleware]
            [ring.middleware.cookies :refer [wrap-cookies]]

            [ring.mock.request :as mock]
            [simple-server.simple-game :as game]
            [clojure.edn :as edn]))

;;; Finally, let us truly separate concerns between our "application code"
;;; and our "http code".  Our game now lives in its own namespace, and
;;; is fully testable independent of our "presentation layer".

(def users-info
  "Store all users' login information"
  (atom {}))

(defn login?
  "Check if a user has logged in"
  [user-collection user]
  (@user-collection user))

(defn login
  [user-collection user]
  (swap! user-collection assoc user true))

(defn login-handler
  [username]
  (login users-info username)
  (-> (response (format "Welcome to the guessing game! %s" username))
      (assoc :cookies {"session_id" {:value username}})))

(defn new-game-handler [username]
  (when (game/new-game! username)
    (response (format "OK- start guessing at /guess. Current user: %s" username))))

(defn guess-handler [username guess]
  (condp = (game/guess-answer username guess)
    nil               (-> (response  "You need to supply a guess with /guess?guess=N")
                          (status 400))
    :game-not-started (-> (response "You need to start the game with /new-game first.")
                          (status 400))
    :game-over        (response (format "Sorry, %s, you have guessed 5 times. Game over." username))
    :win              (response (format "Congratulations %s! You win!" username))
    :too-low          (response (format "Too low and you have %s chances left." (- 5 (game/get-times game/game-in-progress username))))
    :too-high         (response (format "Too high and you have %s chances left." (- 5 (game/get-times game/game-in-progress username))))))

(defroutes game-routes
  (POST "/login"    [username]                                                           (login-handler username))
  (POST "/new-game" {{{username :value} "session_id"} :cookies}                          (if (login? users-info username) (new-game-handler username) (response "Please login first.")))
  (PUT "/guess"     {{{username :value} "session_id"} :cookies {guess :guess} :params}   (if (login? users-info username) (guess-handler username (edn/read-string guess)) (response "Please login first.")))
  (ANY "*"         []                                                                    (not-found "Sorry, No such URI on this server!")))

(defn content-type-middleware
  [handler]
  (fn [request]
    (-> request
        (handler)
        (content-type "text/plain"))))

(def handler
  (-> game-routes
      (middleware/wrap-defaults middleware/api-defaults)
      (content-type-middleware)
      (wrap-cookies)))

(comment
  (handler (mock/request :get "/new-game"))
  (handler (mock/request :get "/guess?guess=3"))
  (handler (mock/request :get "/dunno")))

(defonce server
  (run-jetty #'handler {:port 3001 :join? false}))

:core



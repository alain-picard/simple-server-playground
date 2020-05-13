(ns simple-server.simple-game)

(defn update-count
  [game]
  (update-in game [:counter] inc))

(def game-in-progress (atom {:number nil 
                             :counter 0}))

(defn new-game! 
  []
  (reset! game-in-progress {:number (+1 (rand-int 10)) :counter 0 }))

(defn guess-answer [guess]
  (cond
    (nil? guess) nil

    (= guess (:number @game-in-progress))
    (do (new-game!)
        :correct)

    (< guess (:number @game-in-progress))
    (do (swap! game-in-progress update-count)
        (if (<= 5 (:counter @game-in-progress))
          (do (new-game!)
              :game-over)
          :too-low))

    (> guess (:number @game-in-progress))
    (do (swap! game-in-progress update-count) 
        (if (<= 5 (:counter @game-in-progress))
          (do (new-game!)
              :game-over)
          :too-high))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; game with authorized user
(def auth-game (atom {}))

#_
(defn reset-game! []
  (reset! auth-game {}))

(defn- new-user!
  "Adds a new user to the game
  If a user already exists, restart the game with a new number"
  [user]
  (swap! auth-game conj {user {:counter 0 :number (+ 1 (rand-int 10))}}))

;@auth-game
;=> ("pat" {:counter 0 :number (+1 (rand-int 10}})

(defn authorize-user
  "Adds a user to the game state to track the progress"
  [user]
  (if (nil? user)
    nil
    (if (empty? @auth-game)
      ;; add the first user and start the game
      (do (new-user! user)
          :authorized)
      (if (some #(= user %) (keys @auth-game))
        :user-exists
        (do (new-user! user)     ; add nth user
            :authorized)))))

(defn user-guess
  "Returns the result of a guess from an authorized user"
  [user guess]
  (if (or (nil? user) (nil? guess))
    nil
    (if (some #(= user %) (keys @auth-game))
      (cond
 
        (= guess (get-in @auth-game [user :number])) 
        (do (new-user! user) ;restart the game with a different number
            :correct)

        (< guess (get-in @auth-game [user :number]))
        (do (swap! auth-game update-in [user :counter] inc)
            (if (<= 5 (get-in @auth-game [user :counter]))
              (do (new-user! user)
                  :game-over)
              :too-low))

        (> guess (get-in @auth-game [user :number]))
        (do (swap! auth-game update-in [user :counter] inc)
            (if (<= 5 (get-in @auth-game [user :counter]))
              (do (new-user! user)
                  :game-over)
              :too-high)))

      :unauthorized)))

#_
(do
  (reset-game!)
  @auth-game
  (new-user! "pat")
  (authorize-user "pat")
  (authorize-user "peter")
  (user-guess "pat" 1)
  (user-guess "pat" 2)
  (user-guess "peter" 6)
  (user-guess "pat" 3)
  (user-guess "peter" 7)
  (user-guess "pat" 4)
  (user-guess "peter" 3)
  (user-guess "pat" 5))


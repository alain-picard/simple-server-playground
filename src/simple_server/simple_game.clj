(ns simple-server.simple-game
  (:require [simple-server.database :as db]))

(defn new-game!
  "Creates a new game for a user in the database"
  [user]
  (db/add-user {:username user :counter 0 :target (+ 1 (rand-int 10))}))

(defn update-counter
  "Updates counter after a user makes a guess"
  [user]
  (let [counter (get-in (db/get-user user) [:item :counter])
        target (get-in (db/get-user user) [:item :target])]
    (db/add-user {:username user :counter (inc counter) :target target})))

(defn guess-answer
  "Returns the result of a guess from an authorized user"
  [guess user]
  (if (nil? guess)
    nil
    (if (= user (get-in (db/get-user user) [:item :username]))
      (cond
        
        (= guess (get-in (db/get-user user) [:item :target])) 
        (do (new-game! user) ; restart the game with a new target number
            :correct)

        (< guess (get-in (db/get-user user) [:item :target]))
        (do (update-counter user)
            (if (<= 5 (get-in (db/get-user user) [:item :counter]))
              (do (new-game! user)
                  :game-over)
              :too-low))

        (> guess (get-in (db/get-user user) [:item :target]))
        (do (update-counter user)
            (if (<= 5 (get-in (db/get-user user) [:item :counter]))
              (do (new-game! user)
                  :game-over)
              :too-high)))
      :unauthorized)))



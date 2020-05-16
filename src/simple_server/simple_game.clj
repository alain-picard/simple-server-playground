(ns simple-server.simple-game)

;; Creating a map for a game, consisting of the number to guess and the number of guesses remains for a given user
(def games-in-progress (atom {}))

(defn new-game! [account]
  ;; Make our new game: Generate a random number and resetting the number of guesses to 5
  (do
    (swap! games-in-progress assoc account [(+ 1 (rand-int 10)) 5])
    :ok))

(defn guess-answer [account guess]
  (do
    (swap! games-in-progress update-in [account 1] dec)
    (let [user-num (get-in @games-in-progress [account 0])
          user-guesses (get-in @games-in-progress [account 1])]
      (cond
        (nil? guess) nil

        (= guess user-num)
        :you-win
        
        (= 0 user-guesses)
        :you-lose

        (< guess user-num)
        :too-low

        (> guess user-num)
        :too-high))))


(ns simple-server.simple-game)

;; Creating a map for a game, consisting of the number to guess and the number of guesses remain
(def game-in-progress (atom {:number nil
                             :guesses nil}))

(defn new-game! []
  ;; Make our new game: Generate a random number and resetting the number of guesses to 5
  (do
    (swap! game-in-progress assoc-in [:number] (+ 1 (rand-int 10)))
    (swap! game-in-progress assoc-in [:guesses] 5)
    :ok))

(defn guess-answer [guess]
  (do
    (swap! game-in-progress update-in [:guesses] dec)
    (cond
      (nil? guess) nil
      
      (= 0 (:guesses @game-in-progress))
      :you-lose

      (= guess (:number @game-in-progress))
      :you-win

      (< guess (:number @game-in-progress))
      :too-low

      (> guess (:number @game-in-progress))
      :too-high)))

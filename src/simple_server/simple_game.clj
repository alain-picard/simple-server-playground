(ns simple-server.simple-game)


(def game-in-progress (atom {}))

(defn reset-game
  "Reset the game status for a user."
  [game-data username]
  (swap! game-data assoc username {:value (+ 1 (rand-int 10)) :times 0}))

(defn get-value
  "Return the value for this round for a user."
  [game-data username]
  (get-in @game-data [username :value]))

(defn get-times
  "Return the number of guessing that a user has done."
  [game-data username]
  (get-in @game-data [username :times]))

(defn inc-times
  [game-data username]
  (swap! game-data update-in [username :times] #(inc %)))

(defn new-game! [name]
  ;; Make our new game:
  (reset-game game-in-progress name)
  :ok)

(defn guess-answer
  [name guess]
  (let [value (get-value game-in-progress name) times (get-times game-in-progress name)]
    (cond
      (or (nil? guess) (not= java.lang.Long (type guess))) nil
      ;;you have to start a new game before guessing.
      (nil? value) :game-not-started

      (or (> times 5) (and (= times 5) (not= guess value)))
      (do (reset-game game-in-progress name)
          :game-over)

      (= guess value)
      (do (reset-game game-in-progress name)
          :win)

      (< guess value)
      (do (inc-times game-in-progress name)
          :too-low)

      (> guess value)
      (do (inc-times game-in-progress name)
          :too-high))))


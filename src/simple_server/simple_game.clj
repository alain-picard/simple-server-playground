(ns simple-server.simple-game
  (:require [amazonica.aws.dynamodbv2 :as dynamo]))

(def cred {:access-key "5qklzr"
           :secret-key "16ibz"
           :endpoint "http://localhost:4566"})

(defn reset-game
  "Reset the game status for a user."
  [username]
  (dynamo/put-item cred
                   :table-name "guess-game"
                   :item {:pk (str "GAME#" username "1")
                          :sk (str "USER#" username)
                          :value (+ 1 (rand-int 10))
                          :times 0}))

(defn get-value
  "Return the value for this round for a user."
  [username]
  (let [{user-info :item} (dynamo/get-item cred
                                           :table-name "guess-game"
                                           :key {:pk {:s (str "GAME#" username "1")}
                                                 :sk {:s (str "USER#" username)}})]
    (and user-info (:value user-info))))

(defn get-times
  "Return the number of guessing that a user has done."
  [username]
  (let [{user-info :item} (dynamo/get-item cred
                                           :table-name "guess-game"
                                           :key {:pk {:s (str "GAME#" username "1")}
                                                 :sk {:s (str "USER#" username)}})]
    (and user-info (:times user-info))))

(defn inc-times
  [username]
  (dynamo/update-item cred
                      :table-name "guess-game"
                      :key {:pk {:s (str "GAME#" username "1")}
                            :sk {:s (str "USER#" username)}}
                      :update-expression "ADD #times :val"
                      :expression-attribute-names {"#times" "times"}
                      :expression-attribute-values {":val" 1}))

(defn new-game! [name]
  ;; Make our new game:
  (reset-game name)
  :ok)

(defn guess-answer
  [name guess]
  (let [value (get-value name) times (get-times name)]
    (cond
      (or (nil? guess) (not= java.lang.Long (type guess))) nil
      ;;you have to start a new game before guessing.
      (nil? value) :game-not-started

      (or (> times 4) (and (= times 4) (not= guess value)))
      (do (reset-game name)
          :game-over)

      (= guess value)
      (do (reset-game name)
          :win)

      (< guess value)
      (do (inc-times name)
          :too-low)

      (> guess value)
      (do (inc-times name)
          :too-high))))


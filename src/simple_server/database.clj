(ns simple-server.database
  (:require [cheshire.core]
            [clojure.java.io :as io]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [amazonica.aws.dynamodbv2 :as dynamo]))

(def cred {:endpoint "http://localhost:4566"})

(defn list-tables []
  (dynamo/list-tables cred))

(list-tables)

(dynamo/delete-table cred "games-table")

(dynamo/create-table
 cred
 :table-name "games-table"
 :attribute-definitions [{:attribute-name "account" :attribute-type "S"}]
 :key-schema            [{:attribute-name "account" :key-type "HASH"}]
 :provisioned-throughput {:read-capacity-units 10 :write-capacity-units 10})

;;(dynamo/describe-table cred "games-table")

;; Check if the account exists in our database
(defn is-user? [account]
  (let [user (dynamo/get-item cred
                              :table-name "games-table"
                              :key {:account {:s account}})]
    (not= {} user)))

;; Recording a game in the database, use when logging out a user & signing up a user
(defn record-a-game [account num guesses]
  (dynamo/put-item cred
                   :table-name "games-table"
                   :item {:account account, :random-number num, :guesses-remain guesses}))

;; Retrieving a given user's game
(defn get-num-guesses [account]
  (dynamo/get-item cred
                   :table-name "games-table"
                   :key {:account {:s account}}))





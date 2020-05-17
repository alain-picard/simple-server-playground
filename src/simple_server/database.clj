(ns simple-server.database
  (:require [amazonica.aws.dynamodbv2 :as dynamo]
            [cheshire.core]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [clojure.java.io :as io]))

;; store credentials in the same file for convenience
(def cred {:access-key "mysecretkey"
           :secret-key "mysecretkey"
           :endpoint   "http://localhost:8000"})

;;;;;;;;;;;;;;;;;;;;;;;;
;; initialize a database

#_
(dynamo/create-table
 cred
 :table-name "GuessingGame"
 :key-schema [{:attribute-name "username" :key-type "HASH"}]
 :attribute-definitions [{:attribute-name "username" :attribute-type "S"}]
 :provisioned-throughput {:read-capacity-units 5 :write-capacity-units 5})

#_(dynamo/list-tables cred)
#_(dynamo/describe-table cred "GuessingGame")

;;;;;;;;;;;;;;;;;;;;;;;;
;; delete a table
;; (dynamo/delete-table cred "GuessingGame")

(defn add-user
  "Stores user info and game info in the database"
  [item]
  (dynamo/put-item
   cred
   :table-name "GuessingGame"
   :returned-consumed-capacity "TOTAL"
   :return-item-collection-metrics "SIZE"
   :item item))

(defn get-user
  "Returns a username if exists in the database"
  [username]
  (dynamo/get-item 
   cred
   :table-name "GuessingGame"
   :key {:username {:s username}}))

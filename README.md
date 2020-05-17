## About this project
This project (guessing game) is used to demonstrate important concepts in building an HTTP api service in Clojure.

## Installation

### 1. Clone this project 
Clone this project to your computer, then you need to set up a DynamoDB database to cooperate with this server.

### 2. Setup a database
This project deploys DynamoDB locally on your computer so make sure you have installed [NoSQL Workbench for Amazon Dynamo](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/workbench.settingup.html) and [DynamoDB Local](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.DownloadingAndRunning.html)

After installing these softwares successfully, Open NoSQL workbench and create a DynamoDB Local connection. Credentials for database connection should be automatically generated for you with `Access key ID` and `Secret access key`. Copy these keys and paste them into the following code block of this file `src/simple-server/database.clj`

```clojure
(def cred {:access-key "your-access-key-id"  ; replace your-access-key-id with your access key id 
           :secret-key "your-secret-access-key" ; replace your-secret-access-key with your secret access key
           :endpoint   "http://localhost:8000"})
```
Next, open a command window and navigate to the directory where you extracted DynamoDBLocal.jar, and enter the following command

```sh
java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb
```

The database should start on port 8000

In lein repl, run the following code block from `src/simple-server/database.clj` to create a table in DynamoDB database

```clojure
;;;;;;;;;;;;;;;;;;;;;;;;
;; initialize a database
...
;;;;;;;;;;;;;;;;;;;;;;;;
```

### 3. Start the server
Navigate to the directory where you cloned this project and enter the following command into a new command window to start the server

```sh
lein repl  
```

A server should start listening on [http://localhost:3001](http://localhost:3001)

## How to play this game

This guessing game has some simple rules:
- You need to provide your name to enter the game
- Once registered, the game will provide a random number between 1-10
- You are allowed to make 5 guesses to find the correct number
- If you lose, the game will automatically restart with a new number

1. After starting the server, open [http://localhost:3001/new-game.html](http://localhost:3001/new-game.html) in your browser. A message should display to ask you login before playing the game

2. Click login and enter your name, then you should be able to make a guess at [http://localhost:3001/guess.html](http://localhost:3001/guess.html)

3. A message will display to inform your guess is too low, too high or correct. If you make 5 incorrect predictions, you lose and the game will automatically restart with a new number. Just repeat step 2 to continue playing the game.

## Tags

By moving from git tag to git tag, you should see the evolution
of the code.

 - starting-point
 - request-methods
 - manual-dispatching
 - compojure-routing
 - http-ring-responses
 - destructuring-routes
 - understanding-middlewares
 - using-middleware-and-destructured-routes
 - separation-of-concerns

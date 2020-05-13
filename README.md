## Simple Server

A toy repository to demonstrate important concepts in building
an HTTP api service in clojure.

## Usage
Clone this project and run the following command to start the application

```sh
lein repl  
```

Should start a server listening on [http://localhost:3001](http://localhost:3001)

## How to play this game
This guessing game has some simple rules:
- You need to provide your name to enter the game
- Once registered, the game will provide a random number between 1-10
- You are allowed to make 5 guesses to find the correct number
- If you lose, the game will automatically restart with a new number

To play the game, make sure you have installed [curl](https://curl.haxx.se/download.html) in your machine

After successfully installed the software, open terminal (command in Windows) and follow these steps to play:

1. Enter the following command in a terminal and you should see a message displayed: 

```sh
curl localhost:3001/
Please register your name to start the game at /auth-game/user/<yourname>
```

2. Register player by entering this command and replace `<yourname>` by your username 

```sh
curl localhost:3001/auth-game/user/<yourname>
```

For example: 

```sh
curl localhost:3001/auth-game/user/batman
```

Then a message will display and prompt you to proceed the next step.

3. Make a guess to play the game
To make a guess, replace `<yourname>` and `<yourguess>` by your username and a number in the following command 

```sh
curl localhost:3001/auth-game/<yourname>/guess?guess=<yourguess>
```

A message will display to inform your guess is too low, too high or correct. If you make 5 incorrect predictions, you lose and the game will automatically restart with a new number. Just repeat step 3 to continue playing the game.

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

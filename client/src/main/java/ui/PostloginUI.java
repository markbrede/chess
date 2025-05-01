package ui;

import model.GameData;
import response.CreateGameResponse;
import response.ListGamesResponse;
import facade.ServerFacade;
import ui.ChessBoardUI;

import java.util.ArrayList;
import java.util.List;

public class PostloginUI extends UI {
    private final ServerFacade facade;
    private final String authToken;
    private final String username;
    private boolean running;
    private List<GameData> gamesList;

    public PostloginUI(ServerFacade facade, String authToken, String username) {
        super();
        this.facade = facade;
        this.authToken = authToken;
        this.username = username;
        this.running = true;
        this.gamesList = new ArrayList<>();
    }

    public void run() {
        clearScreen();
        System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY);
        displayWelcomeMessage();  // replace custom welcome message
        displayHelp();

        while (running) {
            String command = promptUser("\nEnter command: ").toLowerCase();
            processCommand(command);
        }
    }


    private void displayWelcomeMessage() {
        displayMessage("\nLOGGED IN AS: " + username);
    }

    private void displayHelp() {
        displayMessage("\nAvailable commands:");
        displayMessage("  help - to display available commands");
        displayMessage("  logout - to log out of the current account");
        displayMessage("  create - to create a new game");
        displayMessage("  list - to list all available games");
        displayMessage("  join - to join a game as a player");
        displayMessage("  observe - to join a game as an observer");
    }

    private void processCommand(String command) {
        switch (command) {
            case "help":
                displayHelp();
                break;
            case "logout":
                logout();
                break;
            case "create":
                createGame();
                break;
            case "list":
                listGames();
                break;
            case "join":
                playGame();
                break;
            case "observe":
                observeGame();
                break;
            default:
                displayErrorMessage("Hmm, that was an unknown command. Please type 'help' for a list of valid commands.");
                break;
        }
    }

    private void logout() {
        try {
            facade.logout(authToken);
            displayMessage("You have logged out successfully.");
            running = false;
        } catch (Exception e) {
            displayErrorMessage("Logout failed due to a " + e.getMessage());
        }
    }

    private void createGame() {
        String gameName = promptUser("Please enter a name for your chess game: ");

        if (gameName == null || gameName.trim().isEmpty()) {
            displayErrorMessage("Game name can't be empty.");
            return;
        }

        //prevent duplicate games through case differences
        String adjGameName = gameName.trim().toLowerCase(); //trim whitespace and convert to lowercase

        try {
            CreateGameResponse response = facade.createGame(adjGameName, authToken);
            displayMessage("Game created successfully with game ID number: " + response.gameID());
        } catch (Exception e) {
            displayErrorMessage("Failed to create game: " + e.getMessage());
        }
    }

    private void listGames() {
        try {
            ListGamesResponse response = facade.listGames(authToken);
            gamesList = response.games();

            if (gamesList.isEmpty()) {
                displayMessage("No games available.");
            } else {
                displayMessage("\nAvailable games:");
                int index = 1;
                for (GameData game : gamesList) {
                    String whitePlayer = game.whiteUsername() != null ? game.whiteUsername() : "EMPTY";
                    String blackPlayer = game.blackUsername() != null ? game.blackUsername() : "EMPTY";
                    displayMessage(index + ". " + game.gameName() + " (White: " + whitePlayer + ", Black: " + blackPlayer + ")");
                    index++;
                }
            }
        } catch (Exception e) {
            displayErrorMessage("Failed to list games: " + e.getMessage());
        }
    }

    private void playGame() {
        if (gamesList.isEmpty()) {
            displayMessage("No games available. Use 'list' command to refresh the games list.");
            return;
        }

        try {
            String gameNumberStr = promptUser("Please enter the game ID number: ");
            int gameNumber = Integer.parseInt(gameNumberStr);

            if (gameNumber < 1 || gameNumber > gamesList.size()) {
                displayErrorMessage("invalid game ID number.");
                return;
            }

            GameData selectedGame = gamesList.get(gameNumber - 1);

            if (selectedGame.game() == null) {
                displayErrorMessage("The board could not be generated due to no available game data.");
                return;
            }

            String color = promptUser("Color (WHITE/BLACK): ").toUpperCase().trim(); //added trim
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                displayErrorMessage("Invalid color. You must choose WHITE or BLACK.");
                return;
            }

            int gameID = selectedGame.gameID();

            if ((color.equals("WHITE") && selectedGame.whiteUsername() != null) ||
                (color.equals("BLACK") && selectedGame.blackUsername() != null)) {
                displayErrorMessage("That side is already taken.");
                return;
            }

            facade.joinGame(color, gameID, authToken);

            displayMessage("You successfully joined the game as the " + color + " player.");

            //draws board
            ChessBoardUI chessboardUI = new ChessBoardUI();
            chessboardUI.drawBoard(color.equals("WHITE"), selectedGame.game());

            promptUser("\nPress Enter to go back to the menu..."); //user must press enter to continue

        } catch (NumberFormatException e) {
            displayErrorMessage("Invalid format for game number");
        } catch (Exception e) {
            displayErrorMessage("Could not join game. There was a " + e.getMessage());
        }
    }

    private void observeGame() {
        if (gamesList.isEmpty()) {
            displayMessage("No games are available. Enter 'list' to refresh the games list.");
            return;
        }

        try {
            String gameNumberStr = promptUser("Please enter the game ID number: ");
            int gameNumber = Integer.parseInt(gameNumberStr);

            if (gameNumber < 1 || gameNumber > gamesList.size()) {
                displayErrorMessage("Invalid game ID number.");
                return;
            }

            GameData selectedGame = gamesList.get(gameNumber - 1);
            int gameID = selectedGame.gameID();

            if (selectedGame.game() == null) {
                displayErrorMessage("The board could not be generated due to no available game data.");
                return;
            }

            //joining a game as an observer
            facade.joinGame(null, gameID, authToken);
            GameData updatedGame = facade.getGame(gameID, authToken);  // Fixed variable name

            displayMessage("You've joined the game as an observer.");

            ChessBoardUI chessboardUI = new ChessBoardUI();
            chessboardUI.drawBoard(true, updatedGame.game());  // Now using fresh data


            promptUser("\nPress Enter to go back to the menu..."); //user must press enter to continue

        } catch (NumberFormatException e) {
            displayErrorMessage("Invalid format for game ID number");
        } catch (Exception e) {
            displayErrorMessage("Could not observe game. There was a " + e.getMessage());
        }
    }
}

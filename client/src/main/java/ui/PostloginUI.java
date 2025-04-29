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
        displayMessage("\n♛ Welcome, " + username + "!");
        displayHelp();

        while (running) {
            String command = promptUser("\nEnter command: ").toLowerCase();
            processCommand(command);
        }
    }

    private void displayWelcomeMessage() {
        displayMessage("\nLogged in as: " + username);
    }

    private void displayHelp() {
        displayMessage("\nAvailable commands:");
        displayMessage("  help - Display available commands");
        displayMessage("  logout - Log out of the current account");
        displayMessage("  create - Create a new game");
        displayMessage("  list - List all available games");
        displayMessage("  join - Join a game as a player");
        displayMessage("  observe - Join a game as an observer");
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
                displayErrorMessage("Unknown command. Type 'help' for a list of commands.");
                break;
        }
    }

    private void logout() {
        try {
            facade.logout(authToken);
            displayMessage("Logout successful.");
            running = false;
        } catch (Exception e) {
            displayErrorMessage("Logout failed: " + e.getMessage());
        }
    }

    private void createGame() {
        String gameName = promptUser("Game name: ");

        if (gameName == null || gameName.trim().isEmpty()) {
            displayErrorMessage("Game name can't be empty.");
            return;
        }

        try {
            CreateGameResponse response = facade.createGame(gameName, authToken);
            displayMessage("Game created successfully with ID: " + response.gameID());
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
            String gameNumberStr = promptUser("Game number: ");
            int gameNumber = Integer.parseInt(gameNumberStr);

            if (gameNumber < 1 || gameNumber > gamesList.size()) {
                displayErrorMessage("Invalid game number.");
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
            displayErrorMessage("Could not join game: " + e.getMessage());
        }
    }

    private void observeGame() {
        if (gamesList.isEmpty()) {
            displayMessage("No games are available. Use the 'list' command to refresh the games list.");
            return;
        }

        try {
            String gameNumberStr = promptUser("Game number: ");
            int gameNumber = Integer.parseInt(gameNumberStr);

            if (gameNumber < 1 || gameNumber > gamesList.size()) {
                displayErrorMessage("Invalid game number.");
                return;
            }

            GameData selectedGame = gamesList.get(gameNumber - 1);

            if (selectedGame.game() == null) {
                displayErrorMessage("The board could not be generated due to no available game data.");
                return;
            }

            int gameID = selectedGame.gameID();

            //joining a game as an observer
            facade.joinGame(null, gameID, authToken);

            displayMessage("You've joined the game as an observer.");

            //draw board from white sides view
            ChessBoardUI chessboardUI = new ChessBoardUI();
            chessboardUI.drawBoard(true, selectedGame.game());

            promptUser("\nPress Enter to go back to the menu..."); //user must press enter to continue

        } catch (NumberFormatException e) {
            displayErrorMessage("Invalid format for game number");
        } catch (Exception e) {
            displayErrorMessage("Could not observe game: " + e.getMessage());
        }
    }
}

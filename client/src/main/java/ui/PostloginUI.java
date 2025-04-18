package ui;

import model.GameData;
import response.CreateGameResponse;
import response.ListGamesResponse;
import facade.ServerFacade;

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
        displayWelcomeMessage();
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

    private void playGame() {}

    private void observeGame() {}
}

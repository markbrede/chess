package ui;

import response.LoginResponse;
import response.RegisterResponse;
import facade.ServerFacade;

public class PreloginUI extends UI {
    private final ServerFacade facade;
    private boolean running;

    public PreloginUI(ServerFacade facade) {
        super();
        this.facade = facade;
        this.running = true;
    }

    public void run() {
        clearScreen();
        displayWelcomeMessage();
        displayHelp();

        while (running) {
            String command = promptUser("\nPlease enter a command: ").toLowerCase();
            processCommand(command);
        }
    }

    private void displayWelcomeMessage() {
        System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY);
        displayMessage("\n♕ HELLO, AND WELCOME TO CHESS! ♕\nReady to make your next brilliant move?");
    }

    private void displayHelp() {
        displayMessage("\nYOUR AVAILABLE COMMANDS ARE...");
        displayMessage("  help - to display available commands");
        displayMessage("  quit - to exit the chess program");
        displayMessage("  login - to log in to an existing account");
        displayMessage("  register - to create a new account");
    }

    private void processCommand(String command) {
        switch (command) {
            case "help":
                displayHelp();
                break;
            case "quit":
                quit();
                break;
            case "login":
                login();
                break;
            case "register":
                register();
                break;
            default:
                displayErrorMessage("Hmm, that was an unknown command. Please type 'help' for a list of valid commands.");
                break;
        }
    }

    private void quit() {
        displayMessage("Thanks for stopping by. Good day to you!");
        running = false;
    }

    private void login() {
        String username = promptUser("Username: ");
        String password = promptUser("Password: ");

        if (username.isEmpty() || password.isEmpty()) {
            displayErrorMessage("Username and password cannot be empty.");
            return;
        }

        try {
            LoginResponse response = facade.login(username, password);
            displayMessage("Login successful! Welcome, " + response.username() + "!");

            // will transition to PostloginUI. I haven't implemented it yet
            PostloginUI postloginUI = new PostloginUI(facade, response.authToken(), response.username());
            clearScreen();
            postloginUI.run();
            clearScreen();

            // check if continue running prelogin after return
            if (!running) {
                return;
            }

        } catch (Exception e) {
            displayErrorMessage("Login failed. Please ensure credentials were entered correctly.");
        }
    }

    private void register() {
        String username = promptUser("Username: ");
        String password = promptUser("Password: ");
        String email = promptUser("Email: ");

        if (username.isEmpty() || password.isEmpty()) {
            displayErrorMessage("Username and password cannot be empty.");
            return;
        }

        try {
            RegisterResponse response = facade.register(username, password, email);
            displayMessage("Registration successful! Welcome, " + response.username() + "!");

            PostloginUI postloginUI = new PostloginUI(facade, response.authToken(), response.username());
            postloginUI.run();

            //check if continue running prelogin after return
            if (!running) {
                return;
            }

        } catch (Exception e) {
            displayErrorMessage("Registration failed: " + e.getMessage());
        }
    }
}

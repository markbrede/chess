package ui;

import java.util.Scanner;

//UI is for common functionality needed in prelog and postlog
public abstract class UI {
    protected Scanner scanner;

    public UI() {
        this.scanner = new Scanner(System.in);
    }

    protected String promptUser(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    protected void displayMessage(String message) {
        System.out.println(message);
    }

    protected void displayErrorMessage(String message) {
        System.out.println("ERROR: " + message);
    }

    protected void clearScreen() {
        System.out.print(EscapeSequences.ERASE_SCREEN);
    }
}

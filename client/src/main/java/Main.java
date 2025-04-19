import facade.ServerFacade;
import ui.PreloginUI;

public class Main {
    public static void main(String[] args) {

        String serverUrl = "http://localhost:8080"; //8080 should work if I wrote it down correctly

        //make server facade
        ServerFacade facade = new ServerFacade(serverUrl);

        //make and run pre login ui
        PreloginUI preloginUI = new PreloginUI(facade);
        preloginUI.run();
    }
}
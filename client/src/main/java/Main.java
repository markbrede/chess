import facade.ServerFacade;
import ui.PreloginUI;

public class Main {
    public static void main(String[] args) {
        int port = 8080;
        ServerFacade facade = new ServerFacade(port);
        PreloginUI preloginUI = new PreloginUI(facade);
        preloginUI.run();
    }
}

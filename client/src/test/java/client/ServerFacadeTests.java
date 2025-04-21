package client;

import facade.ServerFacade;
import server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {
    private static Server server;
    static ServerFacade facade;
    private static final String TestUser = "Marcus Aurelius";
    private static final String TestPassword = "asdf";
    private static final String TestEmail = "marcus@chessmail.testerton";

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabase() throws Exception {
        facade.clearDatabase();
    }

    @Test
    public void passRegister() throws Exception {
        var authData = facade.register(TestUser, TestPassword, TestEmail);
        assertNotNull(authData.authToken());
        assertEquals(TestUser, authData.username());
    }

    @Test
    public void failRegister() throws Exception {
        facade.register(TestUser, TestPassword, TestEmail);
        Exception ex = assertThrows(Exception.class, () ->
                facade.register(TestUser, TestPassword, TestEmail));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    public void passLogin() throws Exception {
        facade.register(TestUser, TestPassword, TestEmail);
        var response = facade.login("marcus aurelius", TestPassword);
        assertNotNull(response.authToken());
        assertEquals(TestUser.toLowerCase(), response.username());
    }

    @Test
    public void failLogin() throws Exception {
        facade.register(TestUser, TestPassword, TestEmail);
        Exception ex = assertThrows(Exception.class, () ->
                facade.login(TestUser, "wrongpassword"));
        assertTrue(ex.getMessage().toLowerCase().contains("unauthorized"));
    }

    @Test
    public void passLogout() throws Exception {
        var auth = facade.register(TestUser, TestPassword, TestEmail);
        assertDoesNotThrow(() -> facade.logout(auth.authToken()));

        Exception ex = assertThrows(Exception.class, () ->
                facade.createGame("Meditations", auth.authToken()));
        assertTrue(ex.getMessage().contains("unauthorized"));
    }

    @Test
    public void failLogout() {
        Exception ex = assertThrows(Exception.class, () ->
                facade.logout("fake authtoken"));
        assertTrue(ex.getMessage().toLowerCase().contains("unauthorized"));
    }

    @Test
    public void passCreateGame() throws Exception {
        var auth = facade.register(TestUser, TestPassword, TestEmail);
        var response = facade.createGame("Stoic Chess Match", auth.authToken());
        assertTrue(response.gameID() > 0);
    }

    @Test
    public void failCreateGame() throws Exception {
        facade.register(TestUser, TestPassword, TestEmail);
        Exception ex = assertThrows(Exception.class, () ->
                facade.createGame("Invalid Game", "fake authtoken"));
        assertTrue(ex.getMessage().toLowerCase().contains("unauthorized"));
    }

    @Test
    public void passListGames() throws Exception {
        var auth = facade.register(TestUser, TestPassword, TestEmail);
        facade.createGame("Roman Millet", auth.authToken());
        facade.createGame("Dueler the FHE kid", auth.authToken());

        var response = facade.listGames(auth.authToken());
        assertEquals(2, response.games().size());
    }

    @Test
    public void failListGames() throws Exception {
        facade.register(TestUser, TestPassword, TestEmail);
        Exception ex = assertThrows(Exception.class, () ->
                facade.listGames("invalid session Yeeee"));
        assertTrue(ex.getMessage().toLowerCase().contains("unauthorized"));
    }

    @Test
    public void passJoinGame() throws Exception {
        var auth1 = facade.register(TestUser, TestPassword, TestEmail);
        var auth2 = facade.register("Zues", "123", "zues@gmail.com");

        var game = facade.createGame("Stoic Showdown", auth1.authToken());
        assertDoesNotThrow(() -> facade.joinGame("WHITE", game.gameID(), auth1.authToken()));
        assertDoesNotThrow(() -> facade.joinGame("BLACK", game.gameID(), auth2.authToken()));
    }

    @Test
    public void failJoinGame() throws Exception {
        var auth = facade.register(TestUser, TestPassword, TestEmail);
        var game = facade.createGame("Marcus vs Himself", auth.authToken());

        Exception colorEx = assertThrows(Exception.class, () ->
                facade.joinGame("GOLD", game.gameID(), auth.authToken()));
        assertEquals("bad request", colorEx.getMessage().toLowerCase());

        Exception gameEx = assertThrows(Exception.class, () ->
                facade.joinGame("WHITE", 9999, auth.authToken()));
        assertTrue(gameEx.getMessage().toLowerCase().contains("couldn't find game"));
    }


    @Test
    public void passObserveGame() throws Exception {
        var auth = facade.register(TestUser, TestPassword, TestEmail);
        var game = facade.createGame("Observable Meditations", auth.authToken());

        //pass null to join as observer
        assertDoesNotThrow(() -> facade.joinGame(null, game.gameID(), auth.authToken()));
    }

}

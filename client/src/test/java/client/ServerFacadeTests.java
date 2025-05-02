package client;

import facade.ServerFacade;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    private static final String USERNAME = "marcus";
    private static final String PASSWORD = "asdf";
    private static final String EMAIL = "marcus@rome.empire";

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    public static void tearDown() {
        server.stop();
    }

    @BeforeEach
    public void clear() throws Exception {
        facade.clearDatabase();
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        var response = facade.register(USERNAME, PASSWORD, EMAIL);
        assertNotNull(response);
        assertNotNull(response.authToken());
        assertTrue(response.authToken().length() > 10);
    }

    @Test
    public void testRegisterFailure() throws Exception {
        facade.register(USERNAME, PASSWORD, EMAIL);

        assertThrows(Exception.class, () -> {
            facade.register(USERNAME, PASSWORD, "different@email.com");
        });
    }

    @Test
    public void testEmptyRegistration() {
        assertThrows(Exception.class, () -> facade.register("", PASSWORD, EMAIL));
        assertThrows(Exception.class, () -> facade.register(USERNAME, "", EMAIL));
        assertThrows(Exception.class, () -> facade.register(USERNAME, PASSWORD, ""));
    }

    @Test
    public void testLoginSuccess() throws Exception {
        facade.register(USERNAME, PASSWORD, EMAIL);

        var response = facade.login(USERNAME, PASSWORD);
        assertNotNull(response);
        assertNotNull(response.authToken());
        assertEquals(USERNAME, response.username());
    }

    @Test
    public void testLoginFailure() throws Exception {
        facade.register(USERNAME, PASSWORD, EMAIL);

        assertThrows(Exception.class, () -> {
            facade.login(USERNAME, "wrongpassword");
        });
    }

    @Test
    public void testLoginNonExistentUser() {
        assertThrows(Exception.class, () -> facade.login("ghostuser", PASSWORD));
    }

    @Test
    public void testLogoutSuccess() throws Exception {
        facade.register(USERNAME, PASSWORD, EMAIL);
        var loginResponse = facade.login(USERNAME, PASSWORD);

        assertDoesNotThrow(() -> {
            facade.logout(loginResponse.authToken());
        });
    }

    @Test
    public void testLogoutFailure() throws Exception {
        assertThrows(Exception.class, () -> {
            facade.logout("invalidauthtoken");
        });
    }

    @Test
    public void testListGamesSuccess() throws Exception {
        facade.register(USERNAME, PASSWORD, EMAIL);
        var loginResponse = facade.login(USERNAME, PASSWORD);

        var response = facade.listGames(loginResponse.authToken());
        assertNotNull(response);
        assertNotNull(response.games());
    }

    @Test
    public void testListGamesFailure() throws Exception {
        assertThrows(Exception.class, () -> {
            facade.listGames("invalidauthtoken");
        });
    }

    @Test
    public void testCreateGameSuccess() throws Exception {
        facade.register(USERNAME, PASSWORD, EMAIL);
        var loginResponse = facade.login(USERNAME, PASSWORD);

        var response = facade.createGame("testgame", loginResponse.authToken());
        assertNotNull(response);
        assertTrue(response.gameID() > 0);
    }

    @Test
    public void testCreateGameFailure() throws Exception {
        assertThrows(Exception.class, () -> {
            facade.createGame("testgame", "invalidauthtoken");
        });
    }

    @Test
    public void testCreateEmptyGameName() throws Exception {
        facade.register(USERNAME, PASSWORD, EMAIL);
        var loginRes = facade.login(USERNAME, PASSWORD);
        assertThrows(Exception.class, () -> facade.createGame("", loginRes.authToken()));
    }

    @Test
    public void testJoinGameSuccess() throws Exception {
        facade.register(USERNAME, PASSWORD, EMAIL);
        var loginResponse = facade.login(USERNAME, PASSWORD);

        var createResponse = facade.createGame("testgame", loginResponse.authToken());

        assertDoesNotThrow(() -> {
            facade.joinGame("WHITE", createResponse.gameID(), loginResponse.authToken());
        });
    }

    @Test
    public void testJoinGameFailure() throws Exception {
        facade.register(USERNAME, PASSWORD, EMAIL);
        var loginResponse = facade.login(USERNAME, PASSWORD);

        var createResponse = facade.createGame("testgame", loginResponse.authToken());

        assertThrows(Exception.class, () -> {
            facade.joinGame("WHITE", 9999, loginResponse.authToken());
        });
    }

    @Test
    public void testJoinOccupiedColor() throws Exception {
        facade.register(USERNAME, PASSWORD, EMAIL);
        var loginRes = facade.login(USERNAME, PASSWORD);
        var game = facade.createGame("test", loginRes.authToken());

        facade.joinGame("WHITE", game.gameID(), loginRes.authToken());

        assertThrows(Exception.class, () ->
                facade.joinGame("WHITE", game.gameID(), loginRes.authToken()));
    }

    @Test
    public void testObserveGame() throws Exception {
        facade.register(USERNAME, PASSWORD, EMAIL);
        var loginRes = facade.login(USERNAME, PASSWORD);
        var game = facade.createGame("test", loginRes.authToken());

        assertDoesNotThrow(() ->
                facade.joinGame(null, game.gameID(), loginRes.authToken()));
    }

    //attempt to make a move as an observer
    @Test
    public void testObserverCannotMakeMove() throws Exception {
        facade.register(USERNAME, PASSWORD, EMAIL);
        var loginRes = facade.login(USERNAME, PASSWORD);
        var game = facade.createGame("testGame", loginRes.authToken());

        facade.joinGame(null, game.gameID(), loginRes.authToken());


        var from = new chess.ChessPosition(2, 1); // b2
        var to = new chess.ChessPosition(3, 1);   // b3
        var move = new chess.ChessMove(from, to, null);

        assertThrows(Exception.class, () ->
                facade.makeMove(game.gameID(), move, loginRes.authToken()));
    }

    @Test
    public void testMultipleObserversCanJoin() throws Exception {
        var user1 = facade.register("observer1", "pass", "o1@mail.com");
        var user2 = facade.register("observer2", "pass", "o2@mail.com");

        var game = facade.createGame("watchable", user1.authToken());

        assertDoesNotThrow(() ->
                facade.joinGame(null, game.gameID(), user1.authToken()));

        assertDoesNotThrow(() ->
                facade.joinGame(null, game.gameID(), user2.authToken()));
    }

    @Test
    public void testObserverCannotJoinInvalidGame() throws Exception {
        var user = facade.register(USERNAME, PASSWORD, EMAIL);

        assertThrows(Exception.class, () ->
                facade.joinGame(null, 99999, user.authToken())); // invalid game ID
    }

    @Test
    public void testExpiredToken() throws Exception {
        var regRes = facade.register(USERNAME, PASSWORD, EMAIL);
        facade.logout(regRes.authToken());

        assertThrows(Exception.class, () ->
                facade.listGames(regRes.authToken()));
    }

    @Test
    public void testClearDatabaseEffect() throws Exception {
        facade.register(USERNAME, PASSWORD, EMAIL);
        facade.clearDatabase();

        assertThrows(Exception.class, () ->
                facade.login(USERNAME, PASSWORD));
    }

    @Test
    public void testClearDatabaseSuccess() throws Exception {
        assertDoesNotThrow(() -> {
            facade.clearDatabase();
        });
    }
}

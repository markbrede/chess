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
    public void testRegisterDuplicateFails() throws Exception {
        facade.register(USERNAME, PASSWORD, EMAIL);
        Exception ex = assertThrows(Exception.class, () -> {
            facade.register(USERNAME, PASSWORD, EMAIL);
        });
        assertTrue(ex.getMessage().toLowerCase().contains("taken"));
    }
}

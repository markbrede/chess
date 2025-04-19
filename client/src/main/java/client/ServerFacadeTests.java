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

        @BeforeAll
        public static void init() {
            server = new Server();
            var port = server.run(0);
            System.out.println("Started test HTTP server on " + port);
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
        public void register_success() throws Exception {
            var authData = facade.register("user1", "password", "email@example.com");
            assertNotNull(authData.authToken());
            assertTrue(authData.authToken().length() > 10);
            assertEquals("user1", authData.username());
        }

        @Test
        public void register_duplicate() {
            // Register once successfully
            assertDoesNotThrow(() -> facade.register("user1", "password", "email@example.com"));

            // Try to register same username again - should throw exception
            Exception exception = assertThrows(Exception.class, () ->
                    facade.register("user1", "password", "email@example.com"));
            assertTrue(exception.getMessage().contains("already taken"));
        }

        //tests I still need to make
        // login
        // loguot
        // createGame
        // listGames
        // joinGame
    }

}

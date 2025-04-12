package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DBAuthDAOTest {
    private DBAuthDAO authDAO;
    private DBUserDAO userDAO; //for creating test users

    @BeforeEach
    public void setUp() throws DataAccessException {
        DatabaseManager.createTables();//database tables
        authDAO = new DBAuthDAO(); //new instance
        userDAO = new DBUserDAO();
        authDAO.clear();
        userDAO.clear();

        //test user
        userDAO.createUser(new UserData("goodBoy", "asdf", "test@gmail.com"));
    }

    @Test
    public void passMakeAuth() throws DataAccessException {
        String authToken = authDAO.makeAuth("goodBoy");

        assertNotNull(authToken, "Auth token should not be null");
        assertFalse(authToken.isEmpty(), "Auth token should not be empty");

        AuthData retrievedAuth = authDAO.getAuth(authToken);
        assertNotNull(retrievedAuth, "Should be able to retrieve the auth data");
        assertEquals("goodBoy", retrievedAuth.username(), "Username should match");
        assertEquals(authToken, retrievedAuth.authToken(), "Auth token should match");
    }

    @Test
    public void failMakeAuthNullUsername() {

        assertThrows(DataAccessException.class, () -> {
            authDAO.makeAuth(null);
        }, "Making auth with null username should throw DataAccessException");
    }

    @Test
    public void failMakeAuthEmptyUsername() {
        assertThrows(DataAccessException.class, () -> {
            authDAO.makeAuth("");
        }, "Making auth with empty username should throw DataAccessException");
    }

    @Test
    public void passGetAuth() throws DataAccessException {
        String authToken = authDAO.makeAuth("goodBoy");

        AuthData retrievedAuth = authDAO.getAuth(authToken);

        assertNotNull(retrievedAuth, "Auth data should not be null");
        assertEquals("goodBoy", retrievedAuth.username(), "Username should match");
        assertEquals(authToken, retrievedAuth.authToken(), "Auth token should match");
    }

    @Test
    public void failGetAuthNonexistent() {

        assertThrows(UnauthorizedException.class, () -> {
            authDAO.getAuth("nonexistentToken");
        }, "Getting non-existent auth token should throw UnauthorizedException");
    }

    @Test
    public void passDeleteAuth() throws DataAccessException {
        String authToken = authDAO.makeAuth("goodBoy");

        authDAO.deleteAuth(authToken);

        assertThrows(UnauthorizedException.class, () -> {
            authDAO.getAuth(authToken);
        }, "Token should be deleted and not retrievable");
    }

    @Test
    public void failDeleteAuthNonexistent() {
        assertThrows(UnauthorizedException.class, () -> {
            authDAO.deleteAuth("nonexistentToken");
        }, "Deleting non-existent auth token should throw UnauthorizedException");
    }

    @Test
    public void passClear() throws DataAccessException {
        String authToken1 = authDAO.makeAuth("goodBoy");

        userDAO.createUser(new UserData("anotherUser", "asdf", "another@example.com"));
        String authToken2 = authDAO.makeAuth("anotherUser");

        authDAO.clear();//clear all tok

        assertThrows(UnauthorizedException.class, () -> {
            authDAO.getAuth(authToken1);
        }, "First token should be cleared");

        assertThrows(UnauthorizedException.class, () -> {
            authDAO.getAuth(authToken2);
        }, "Second token should be cleared");
    }

}
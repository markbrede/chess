package service; // This is the package the test class belongs to

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserService userService;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    @BeforeEach
    public void setUp() {
        // in memory version of daos for the tests
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    public void verifyUserValidCredentials() throws DataAccessException {
        // I want to add a user
        UserData testUser = new UserData("testuser", "password", "test@example.com");
        userService.makeUser(testUser);
        // I want to return true if username and password match
        assertTrue(userService.verifyUser("testuser", "password"));
    }

    @Test
    public void verifyUserInvalidUsername() throws DataAccessException {
        // test for known user
        UserData testUser = new UserData("testuser", "password", "test@example.com");
        userService.makeUser(testUser);
        // verifying with a wrong username should throw except
        assertThrows(DataAccessException.class, () ->
                userService.verifyUser("wronguser", "password"));
    }

    @Test
    public void verifyUserInvalidPassword() throws DataAccessException {
        // add with valid creds
        UserData testUser = new UserData("testuser", "password", "test@example.com");
        userService.makeUser(testUser);
        // testing if a wrong PW throws an except
        assertFalse(userService.verifyUser("testuser", "wrongpassword"));
    }

    @Test
    public void makeUserValid() throws DataAccessException {
        // if I create a new user is valid auth data returned?
        UserData testUser = new UserData("testuser", "password", "test@example.com");
        AuthData result = userService.makeUser(testUser);

        assertNotNull(result);
        assertEquals("testuser", result.username()); // needs to be valid data
        assertNotNull(result.authToken()); // tok cant be null
    }

    @Test
    public void makeUserDuplicateUsername() {
        // duplicate users
        UserData testUser = new UserData("testuser", "password", "test@example.com");

        assertDoesNotThrow(() -> userService.makeUser(testUser));

        assertThrows(DataAccessException.class, () -> userService.makeUser(testUser));
    }

    @Test
    public void loginUserValidCredentials() throws DataAccessException {
        // register user first
        UserData testUser = new UserData("testuser", "password", "test@example.com");
        userService.makeUser(testUser);
        // should return same auth tok
        AuthData result = userService.loginUser(testUser);

        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void loginUserInvalidCredentials() throws DataAccessException {
        UserData testUser = new UserData("testuser", "password", "test@example.com");
        userService.makeUser(testUser);
        // take the registered user and try to login with wrong password (expecting unauthorized)
        UserData wrongUser = new UserData("testuser", "wrongpassword", "test@example.com");
        assertThrows(UnauthorizedException.class, () -> userService.loginUser(wrongUser));
    }

    @Test
    public void logoutUserValidToken() throws DataAccessException {
        UserData testUser = new UserData("testuser", "password", "test@example.com");
        AuthData authData = userService.makeUser(testUser);

        // I want to take valid user and log them out. Should not throw
        assertDoesNotThrow(() -> userService.logoutUser(authData.authToken()));
    }

    @Test
    public void logoutUserInvalidToken() {
        // no user is added so this token is definitely invalid
        assertThrows(DataAccessException.class, () -> userService.logoutUser("invalid-token"));
    }

    @Test
    public void clearRemovesAllUsers() throws DataAccessException {
        UserData testUser = new UserData("testuser", "password", "test@example.com");
        userService.makeUser(testUser);
        // wipe the database of the new user
        userService.clear();
        // login should fail cause they no longer exisit.
        assertThrows(DataAccessException.class, () -> userService.loginUser(testUser));
    }
}

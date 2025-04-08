package service; // This is the package the test class belongs to

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.LoginRequest;
import request.RegisterRequest;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserService userService;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    RegisterRequest req = new RegisterRequest("Mark", "123", "mark@gmail.com");

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
        userService.makeUser(req);
        // I want to return true if username and password match
        assertTrue(userService.verifyUser(req.username(), req.password()));
    }

    @Test
    public void verifyUserInvalidUsername() throws DataAccessException {
        // test for known user
        userService.makeUser(req);
        // verifying with a wrong username should throw except
        assertThrows(DataAccessException.class, () ->
                userService.verifyUser("wronguser", req.password()));
    }

    @Test
    public void verifyUserInvalidPassword() throws DataAccessException {
        // add with valid creds
        userService.makeUser(req);
        // testing if a wrong PW throws an except
        assertFalse(userService.verifyUser(req.username(), "wrongpassword"));
    }

    @Test
    public void makeUserValid() throws DataAccessException {
        // if I create a new user is valid auth data returned?
        AuthData result = userService.makeUser(req);

        assertNotNull(result);
        assertEquals(req.username(), result.username()); // needs to be valid data
        assertNotNull(result.authToken()); // tok cant be null
    }

    @Test
    public void makeUserDuplicateUsername() {
        // duplicate users
        assertDoesNotThrow(() -> userService.makeUser(req));

        assertThrows(DataAccessException.class, () -> userService.makeUser(req));
    }

    @Test
    public void loginUserValidCredentials() throws DataAccessException {
        // register user first
        userService.makeUser(req);
        LoginRequest loginReq = new LoginRequest(req.username(), req.password());
        // should return same auth tok
        AuthData result = userService.loginUser(loginReq);

        assertNotNull(result);
        assertEquals(req.username(), result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void loginUserInvalidCredentials() throws DataAccessException {
        userService.makeUser(req);
        // take the registered user and try to login with wrong password (expecting unauthorized)
        LoginRequest invalid = new LoginRequest("asdf", "asdfadf");
        assertThrows(UnauthorizedException.class, () -> userService.loginUser(invalid));
    }

    @Test
    public void logoutUserValidToken() throws DataAccessException {
        AuthData authData = userService.makeUser(req);

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
        userService.makeUser(req);
        // wipe the database of the new user
        userService.clear();
        // login should fail cause they no longer exisit.
        LoginRequest loginReq = new LoginRequest(req.username(), req.password());
        assertThrows(DataAccessException.class, () -> userService.loginUser(loginReq));
    }
}

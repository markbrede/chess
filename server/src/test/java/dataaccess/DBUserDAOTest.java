package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mindrot.jbcrypt.BCrypt;

public class DBUserDAOTest {
    private DBUserDAO userDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        DatabaseManager.createTables();//database tables
        userDAO = new DBUserDAO(); //new instance
        userDAO.clear();
    }


    //CREATE USER
    @Test
    public void passCreatingUser() throws DataAccessException {
        UserData testUser = new UserData("testgoodboy", "asdf", "test@goodboy.com");

        userDAO.createUser(testUser);
        //retrieve the good boy (verify uesr)
        UserData retrievedUser = userDAO.getUser("testgoodboy");
        assertNotNull(retrievedUser, "User should be retrieved");
        assertEquals("testgoodboy", retrievedUser.username(), "Username should match");
        // Note: We don't check password directly as it's hashed
        assertEquals("test@goodboy.com", retrievedUser.email(), "email should match");
    }

    @Test
    public void failDuplicateUser() throws DataAccessException {
        UserData testUser = new UserData("duplicatedoggo", "asdf", "test@naughtydoggo.com");

        userDAO.createUser(testUser);
        assertThrows(BadRequestException.class, () -> {
            userDAO.createUser(testUser);
        }, "creating duplicate user should throw badrequest");
    }


    //GET USER
    @Test
    public void passGetUser() throws DataAccessException {
        UserData testUser = new UserData("getgoodboy", "asdf", "getgoodboy@test.com");
        userDAO.createUser(testUser);

        UserData retrievedUser = userDAO.getUser("getgoodboy");

        assertNotNull(retrievedUser, "User should not be null");
        assertEquals("getgoodboy", retrievedUser.username(), "Username should match");
        assertEquals("getgoodboy@test.com", retrievedUser.email(), "Email should match");
    }

    @Test
    public void failGetNonexistent() {
        assertThrows(UnauthorizedException.class, () -> {
            userDAO.getUser("nonexistentboy");
        }, "Getting nonexistent user should throw Unauthorized");
    }

    //CLEAR
    @Test
    public void passClear() throws DataAccessException {
        //a couple of users
        userDAO.createUser(new UserData("goodboy", "asdf", "goodboy@gmail.com"));
        userDAO.createUser(new UserData("naughtydoggo", "fdsa", "naughtydoggo@gmail.com"));

        userDAO.clear();

        //Did they clear? Check if getUser throws exception
        assertThrows(UnauthorizedException.class, () -> {
            userDAO.getUser("goodboy");
        }, "User should not exist after clear");

        assertThrows(UnauthorizedException.class, () -> {
            userDAO.getUser("naughtydoggo");
        }, "User should not exist after clear");
    }

    //Password Verification
    @Test
    public void passHashPassword() throws DataAccessException {
        String username = "passworduser";
        String password = "securepassword";
        UserData testUser = new UserData(username, password, "password@example.com");

        userDAO.createUser(testUser);
        UserData retrievedUser = userDAO.getUser(username);
        //bcrypt to verify the PW
        boolean passwordMatches = BCrypt.checkpw(password, retrievedUser.password());
        assertTrue(passwordMatches, "Password verification should work with correct password");

        boolean wrongPasswordFails = BCrypt.checkpw("wrongpassword", retrievedUser.password());
        assertFalse(wrongPasswordFails, "Password verification should not work with incorrect password");
    }


    //Null fields
    @Test
    public void failNullFieldUser() {
        //null username
        assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(new UserData(null, "password", "email"));
        });
        //null password
        assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(new UserData("username", null, "email"));
        });
        //Null email
        assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(new UserData("username", "password", null));
        });
    }

    @Test
    public void failEmptyFieldUser() {
        // Test empty username
        assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(new UserData("", "password", "email"));
        });

        // Test empty password
        assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(new UserData("username", "", "email"));
        });
    }

}

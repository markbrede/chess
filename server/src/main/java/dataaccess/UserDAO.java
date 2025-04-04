package dataaccess;
//DAOs know how to interact with the database (or in your case, in-memory storage).
//They should perform operations like creating, reading, updating, and deleting users and authentication tokens
import model.UserData;

public interface UserDAO {
    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void clear();
}
package dataaccess;

import model.AuthData;
import model.UserData;

public interface UserDAO {
    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void createAuth(String username, String authToken) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    void clear();
}

package dataaccess;
//DAOs know how to interact with the database (in your case, in memory storage).
//They should perform operations like creating, reading, updating, and deleting users and authentication tokens
import model.AuthData;

//Authorizer
public interface AuthDAO {
    String makeAuth(String username) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    void clear();
}
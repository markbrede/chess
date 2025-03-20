package dataaccess;

import model.AuthData;
import model.UserData;
import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO {
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> auths = new HashMap<>();

    //creates a new user
    public void createUser(UserData user) throws DataAccessException {
        //if a new user contains existing UserData (username) in hashmap, throw error message
        if (users.containsKey(user.username())) {
            throw new DataAccessException("Error: Username already taken");
        }
        //otherwise, it will add the new user to the users hashmap with their username as the key
        users.put(user.username(), user);
    }

    //get the user by their username
    public UserData getUser(String username) throws DataAccessException {
        //look for user from hashmap using their username
        UserData user = users.get(username);
        //if username is not found, throw err message
        if (user == null) {
            throw new DataAccessException("Error: User not found");
        }
        //otherwise, return the found user!👍
        return user;
    }

    //makes users new authentication token
    public void createAuth(String username, String authToken) throws DataAccessException {
        //dose user exists in the hashmap?
        if (!users.containsKey(username)) {
            throw new DataAccessException("Error: User dose not exist");
        }
        //does the token already exists in hashmap?
        if (auths.containsKey(authToken)) {
            throw new DataAccessException("Error: Auth token already exists");
        }
        //if none of the DataAccExcps are hit, this method will finish by making a new
        // AuthData object and add it to the auths hashmap
        auths.put(authToken, new AuthData(authToken, username));
    }

    //gets authentication data through auth token
    public AuthData getAuth(String authToken) throws DataAccessException {
        //see if authentication data from the auths hashmap can be accessed using the auth token
        AuthData auth = auths.get(authToken);
        //if the data is not found, err mess
        if (auth == null) {
            throw new DataAccessException("Error: Auth token not found");
        }
        //otherwise, return that authentication data
        return auth;
    }

    //for deleting authentication tokens
    public void deleteAuth(String authToken) throws DataAccessException {
        //does the authentication token exists in the auths hashmap?
        if (!auths.containsKey(authToken)) {
            throw new DataAccessException("Error: Auth token does not exist");
        }
        //if it so, .remove that thang
        auths.remove(authToken);
    }

    //clear data
    public void clear() {
        users.clear();
        auths.clear();
    }
}

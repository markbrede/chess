package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    private final Map<String, AuthData> authTokens = new HashMap<>();

    @Override
    public String makeAuth(String username) throws DataAccessException {
        if (username == null || username.isEmpty()) {
            throw new DataAccessException("Your username cannot be empty");
        }
        String authToken = UUID.randomUUID().toString(); //ex from the class md for getting a unique token
        AuthData auth = new AuthData(authToken, username);
        authTokens.put(authToken, auth); //slap new authToken into that hashmap
        return authToken;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        AuthData auth = authTokens.get(authToken);
        if (auth == null) {
            throw new DataAccessException("The authorize token was not found: " + authToken);
        }
        return auth;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        //modifies map. including in if statement
        if (authTokens.remove(authToken) == null) {
            throw new DataAccessException("The authorize token was not found and could not be deleted: " + authToken);
        }
    }

    @Override
    public void clear() {
        authTokens.clear();
    }
}

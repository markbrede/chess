package dataaccess;

import model.AuthData;

public class DBAuthDAO implements AuthDAO{
    @Override
    public String makeAuth(String username) throws DataAccessException{

        return username;
    }
    @Override
    public AuthData getAuth(String authToken) throws DataAccessException{

        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException{

    }
    @Override
    public void clear() {

    }
}

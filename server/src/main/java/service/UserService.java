package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;

public class UserService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }
    //named method makeUser so there is no confusion with the lower level method
    public AuthData makeUser(UserData userData) throws DataAccessException {
        userDAO.createUser(userData);
        String authToken = authDAO.makeAuth(userData.username()); //makeAuth generates token
        return new AuthData(authToken, userData.username());
    }

    public AuthData loginUser(UserData userData) throws DataAccessException {
        if (userDAO.verifyUser(userData.username(), userData.password())) {
            String authToken = authDAO.makeAuth(userData.username());
            return new AuthData(authToken, userData.username());
        } else {
            throw new DataAccessException("The username and/or password you entered are incorrect");
        }
    }

    public void logoutUser(String authToken) throws DataAccessException {
        authDAO.deleteAuth(authToken);
    }

    public void clear() {
        userDAO.clear();
        authDAO.clear();
    }
}

package service;

import dataaccess.*;
import model.UserData;

public class UserService {
    //I want these fields private
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    //new users are made with a username, password, and email
    public String registerUser(String username, String password, String email) throws DataAccessException {
        if (username == null || username.isEmpty() ||
                password == null || password.isEmpty() ||
                email == null || email.isEmpty()) {
            throw new DataAccessException("Username, password, and email cannot be empty.");
        }

        UserData newUser = new UserData(username, password, email);
        userDAO.createUser(newUser);

        return authDAO.makeAuth(username); //make a token when a user registers
    }

    //authenticates past users and returns auth token
    public String authenticateUser(String username, String password) throws DataAccessException {
        UserData user = userDAO.getUser(username);

        if (!user.password().equals(password)) {
            throw new DataAccessException("Invalid username or password.");
        }

        return authDAO.makeAuth(username); //new session, new token
    }

    //delete the auth token when logged out
    public void logoutUser(String authToken) throws DataAccessException {
        authDAO.deleteAuth(authToken);
    }
}

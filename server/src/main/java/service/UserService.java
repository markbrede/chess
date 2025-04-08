package service;
// Service are the workers of your application. They contain the business logic.
// We talked about how your UserService handles the actual registration process, checking if the username is taken,
// creating a new user in the database, and generating an authentication token.
import dataaccess.*;
import model.AuthData;
import model.UserData;
import request.LoginRequest;
import request.RegisterRequest;

public class UserService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }
    //named method makeUser so there is no confusion with the lower level method
    public AuthData makeUser(RegisterRequest req) throws DataAccessException {

        UserData userData = new UserData(req.username(), req.password(), req.email());
        userDAO.createUser(userData);
        String authToken = authDAO.makeAuth(userData.username()); //makeAuth generates token
        return new AuthData(authToken, userData.username());
    }

    public AuthData loginUser(LoginRequest req) throws DataAccessException {
        //switching method to use the service's verifyUser method instead of DAO
        if (verifyUser(req.username(), req.password())) {
            String authToken = authDAO.makeAuth(req.username());
            return new AuthData(authToken, req.username());
        } else {
            throw new UnauthorizedException("Error: invalid username or password");
        }
    }

    //verifyUser cause authUser looks wrong in the userDAO interface
    public boolean verifyUser(String username, String password) throws DataAccessException {
        //call userDAO.getUser since I moved this method from MemoryUserDAO to UserService
        UserData user = userDAO.getUser(username);
        return user.password().equals(password);
    }

    public void logoutUser(String authToken) throws DataAccessException {
        authDAO.deleteAuth(authToken);
    }

    public void clear() {
        userDAO.clear();
        authDAO.clear();
    }
}

package service;

import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class UserService {

    private final MemoryUserDAO userDAO;

    public UserService(MemoryUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public RegisterResult register(RegisterRequest request) throws Exception {
        try {
            // Check if the username already exists
            if (userDAO.getUser(request.getUsername()) != null) {
                throw new Exception("Username already taken");
            }

            // Create a new user
            UserData userData = new UserData(request.getUsername(), request.getPassword(), request.getEmail());
            userDAO.createUser(userData);

            // Generate an authentication token
            String authToken = UUID.randomUUID().toString();
            userDAO.createAuth(request.getUsername(), authToken);

            // Return the result with the username and auth token
            return new RegisterResult(request.getUsername(), authToken);
        } catch (DataAccessException e) {
            throw new Exception(e.getMessage());
        }
    }
}

package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryUserDAO;
import model.UserDataRecord;
import java.util.UUID;

public class UserService {
    private final MemoryUserDAO userDAO;

    public UserService(MemoryUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public String register(String username, String password, String email) throws DataAccessException {
        if (username == null || username.isEmpty() || password == null || password.isEmpty() || email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Error: bad request");
        }
        UserDataRecord newUser = new UserDataRecord(username, password, email);
        userDAO.createUser(newUser);
        return generateAuthToken();
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}

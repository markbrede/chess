package dataaccess;

import model.UserData;
import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    private final HashMap<String, UserData> users = new HashMap<>();

    public void createUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new DataAccessException("User already exists");
        }
        users.put(user.username(), user);
    }


    public UserData getUser(String username) throws DataAccessException {
        UserData user = users.get(username);
        //if null is returned, then exception thrown. Prob rm exception after I test
        if (user == null) {
            throw new DataAccessException("User not found: " + username);
        }
        return user;
    }
    //verifyUser cause authUser looks wrong in the userDAO interface
    public boolean verifyUser(String username, String password) throws DataAccessException {
        UserData user = getUser(username); //reuse getUser exception if the user is not found
        return user.password().equals(password);
    }

    public void clear() {
        users.clear();
    }
}

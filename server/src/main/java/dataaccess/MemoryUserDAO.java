package dataaccess;

import model.UserData;
import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    private final HashMap<String, UserData> users = new HashMap<>();

    public void createUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new BadRequestException("Error: username already exists");
        }
        users.put(user.username(), user);
    }

    public UserData getUser(String username) throws DataAccessException {
        UserData user = users.get(username);
        //if null is returned, then exception thrown. Prob rm exception after I test
        if (user == null) {
            throw new UnauthorizedException("Error: the following user was not found... " + username);
        }
        return user;
    }

    public void clear() {
        users.clear();
    }
}

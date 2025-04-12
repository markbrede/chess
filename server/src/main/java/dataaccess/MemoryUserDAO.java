package dataaccess;

import model.UserData;
import java.util.HashMap;
import org.mindrot.jbcrypt.BCrypt;

public class MemoryUserDAO implements UserDAO {
    private final HashMap<String, UserData> users = new HashMap<>();

    public void createUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new BadRequestException("Error: username already exists");
        }
        //BCrypt hashing
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        //new user data
        UserData userWithHashedPassword = new UserData(user.username(), hashedPassword, user.email());
        users.put(user.username(), userWithHashedPassword);
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

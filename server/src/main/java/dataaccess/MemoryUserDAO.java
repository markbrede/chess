package dataaccess;

import model.UserDataRecord;
import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO {
    private final Map<String, UserDataRecord> users = new HashMap<>();

    public void createUser(UserDataRecord user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new DataAccessException("Error: already taken");
        }
        users.put(user.username(), user);
    }

    public UserDataRecord getUser(String username) {
        return users.get(username);
    }

    public void clear() {
        users.clear();
    }
}

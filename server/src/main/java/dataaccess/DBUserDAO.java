package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUserDAO implements UserDAO {

    @Override
    public void createUser(UserData user) throws DataAccessException {
        //hash password with bcrypt gensalt before storing in db
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        //db equivalent to my hashmap in mem dao
        String sql = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.username());
            stmt.setString(2, hashedPassword);//Hashed! cannot use UserData's str pw
            stmt.setString(3, user.email());
            //add to storage. like users.put in mem dao
            stmt.executeUpdate();
        }
        //like me memory contains.key except for the db level
        catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                throw new BadRequestException("Error: username already exists"); //reuse mem dao exceptions
            }
            throw new DataAccessException("Error: " + e.getMessage()); //for general db errs
        }
    }
    
    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password, email FROM user WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedUsername = rs.getString("username");
                    String storedPassword = rs.getString("password");
                    String storedEmail = rs.getString("email");

                    return new UserData(storedUsername, storedPassword, storedEmail); //if they are all equal, return them
                } else {
                    throw new UnauthorizedException("Error: the following user was not found... " + username); //my mem err
                }
            }
        }
        catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
    }

    @Override
    public void clear() {
        String sql = "DELETE FROM user";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.executeUpdate();
        }
        catch (SQLException | DataAccessException e) {
            System.err.println("Error clearing user table: " + e.getMessage());//no longer simply clearing memory
        }
    }

}

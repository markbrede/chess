package dataaccess;

import model.AuthData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DBAuthDAO implements AuthDAO {
    //Reusing MySQL structure from my DBuserDAO
    @Override
    public String makeAuth(String username) throws DataAccessException {
        if (username == null || username.isEmpty()) {
            throw new DataAccessException("Error: username cannot be empty");
        }

        String authToken = UUID.randomUUID().toString();
        //db equivalent to my hashmap in mem dao
        String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, authToken);
            stmt.setString(2, username);
            stmt.executeUpdate(); //add to storage. like users.put in mem dao

            return authToken;
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage()); //for general DB errs
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = "SELECT authToken, username FROM auth WHERE authToken = ?";
        //Reusing getUser structure
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, authToken);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedAuthToken = rs.getString("authToken");
                    String storedUsername = rs.getString("username");

                    return new AuthData(storedAuthToken, storedUsername); //if they are all equal, return them
                } else {
                    throw new UnauthorizedException("Error: auth token not found: " + authToken); //my mem err
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String sql = "DELETE FROM auth WHERE authToken = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, authToken);

            int updatedRowCount = stmt.executeUpdate(); //0 woudl indicate token not found

            if (updatedRowCount == 0) {
                throw new UnauthorizedException("Error: auth token not found"); //adjusted to correct exception type
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
    }

    @Override
    public void clear() {
        String sql = "DELETE FROM auth";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.executeUpdate();
        }
        catch (SQLException | DataAccessException e) {
            System.err.println("Error clearing auth table: " + e.getMessage());//no longer simply clearing memory
        }
    }
}

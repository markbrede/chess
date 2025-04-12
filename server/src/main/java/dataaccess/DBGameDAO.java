package dataaccess;

import model.GameData;
import chess.ChessGame;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBGameDAO implements GameDAO {
    private final Gson gson;

    public DBGameDAO() {
        gson = new Gson();
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        if (gameName == null || gameName.trim().isEmpty()) {
            throw new BadRequestException("Game name can't be empty!");
        }

        ChessGame newGame = new ChessGame();
        String gameJson = gson.toJson(newGame); //ChessGame converted to json
        int newGameId = getNextGameId(); //new game ID
        //to DB
        String sql = "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newGameId);
            stmt.setString(2, null);//these two are null cause no W/B team starting out
            stmt.setString(3, null);
            stmt.setString(4, gameName);
            stmt.setString(5, gameJson);
            stmt.executeUpdate();

            return newGameId;

        } catch (SQLException e) {
            throw new DataAccessException("Error creating game: " + e.getMessage());
        }
    }

    //No longer working with memory... I need to create a way to make multiple game request possible
    private int getNextGameId() throws DataAccessException {
        String sql = "SELECT MAX(gameID) FROM game";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) { //set result

            if (rs.next()) { //max game ID in the table
                int maxID = rs.getInt(1); //get the first cols val.
                return maxID + 1; //adding a +1 iteration to easily make a unique ID
            }
            //1 for no ID situations
            return 1;
        } catch (SQLException e) {
            throw new DataAccessException("Error: Couldnt get next game ID: " + e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game WHERE gameID = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int ID = rs.getInt("gameID");
                    String whiteUsername = rs.getString("whiteUsername");
                    String blackUsername = rs.getString("blackUsername");
                    String gameName = rs.getString("gameName");
                    String gameJson = rs.getString("game");

                    // Deserialize the game JSON to a ChessGame object
                    ChessGame game = gson.fromJson(gameJson, ChessGame.class);

                    return new GameData(ID, whiteUsername, blackUsername, gameName, game);
                } else {
                    throw new DataAccessException("Couldn't find game with ID: " + gameID);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: retrieving game... " + e.getMessage());
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        String sql = "SELECT gameId, whiteUsername, blackUsername, gameName, game FROM game";
        List<GameData> games = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int ID = rs.getInt("gameID");
                String whiteUsername = rs.getString("whiteUsername");
                String blackUsername = rs.getString("blackUsername");
                String gameName = rs.getString("gameName");
                String gameJson = rs.getString("game");

                ChessGame game = gson.fromJson(gameJson, ChessGame.class); //deserialize gameJson
                games.add(new GameData(ID, whiteUsername, blackUsername, gameName, game));
            }
            return games;

        } catch (SQLException e) {
            throw new DataAccessException("Error: Could not list game... " + e.getMessage());
        }
    }

    @Override
    public void updateGame(GameData updatedGame) throws DataAccessException {
        String sql = "UPDATE game SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String gameJson = gson.toJson(updatedGame.game());//serialize the updated game

            stmt.setString(1, updatedGame.whiteUsername());
            stmt.setString(2, updatedGame.blackUsername());
            stmt.setString(3, updatedGame.gameName());
            stmt.setString(4, gameJson);
            stmt.setInt(5, updatedGame.gameID());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("Erorr: No game found with ID..." + updatedGame.gameID());
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: Could not update game..." + e.getMessage());
        }
    }

    @Override
    public void clear() {
        String sql = "DELETE FROM game";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            System.err.println("Error clearing game table: " + e.getMessage());//no longer simply clearing memory
        }
    }

}
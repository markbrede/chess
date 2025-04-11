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

    public DBGameDAO(){
        gson = new Gson();
    }
    @Override
    public int createGame(String gameName) throws DataAccessException {
        if (gameName == null || gameName.trim().isEmpty()) {
            throw new BadRequestException("Game name can't be empty!");
        }

        ChessGame newGame = new ChessGame();
        String gameJson = gson.toJson(newGame); //ChessGame converted to json
        int newGameID = getNextGameId(); //new game ID
        //to DB
        String sql = "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newGameID);
            stmt.setString(2, null);//these two are null cause no W/B team starting out
            stmt.setString(3, null);
            stmt.setString(4, gameName);
            stmt.setString(5, gameJson);
            stmt.executeUpdate();

            return newGameID;

        } catch (SQLException e) {
            throw new DataAccessException("Error creating game: " + e.getMessage());
        }
    }

    //No longer working with memory... I need to create a way to make multiple game request possible
    private int getNextGameId() throws DataAccessException {

        return 0;
    }


    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public void updateGame(GameData updatedGame) throws DataAccessException {

    }

    @Override
    public void clear() {

    }
}

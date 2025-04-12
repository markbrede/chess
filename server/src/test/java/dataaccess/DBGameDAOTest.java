package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
// import dataaccess.DataAccessException;
// import dataaccess.BadRequestException;

public class DBGameDAOTest {
    private DBGameDAO gameDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        DatabaseManager.createTables();
        gameDAO = new DBGameDAO();
        gameDAO.clear();
    }

    @Test
    public void passCreateGameValid() throws DataAccessException {
        String gameName = "test game";
        int gameId = gameDAO.createGame(gameName);

        assertEquals(1, gameId, "First game should have a 1 id");
        GameData game = gameDAO.getGame(gameId);
        assertEquals(gameName, game.gameName(), "Game name should match");
        assertNull(game.whiteUsername(), "To start, white username should be null");
        assertNull(game.blackUsername(), "To start, Black username should be null");
    }

    @Test
    public void failCreateGameEmptyName() {
        assertThrows(BadRequestException.class, () -> {
            gameDAO.createGame("");
        }, "Empty game name should throw BadRequestException");
    }

   // @Test
   // public void passGetGameValid() throws DataAccessException {
   //     int gameId = gameDAO.createGame("Retrieval Test");
   //     GameData game = gameDAO.getGame(gameId);

   //     assertNotNull(game, "Game should be retrievable");
   //     assertEquals(gameId, game.gameId(), "Game id should match");
   //}

    @Test
    public void failGetGameInvaliddD() {
        assertThrows(DataAccessException.class, () -> {
            gameDAO.getGame(999);
        }, "Invalid game id needs to throw DataAccessException");
    }

    //listing games
    @Test
    public void passListGamesMultiple() throws DataAccessException {
        gameDAO.createGame("Charlie");
        gameDAO.createGame("Bravo");

        assertEquals(2, gameDAO.listGames().size(), "should be listing the two game Charlie and Bravo");
    }

    @Test
    public void passListGamesEmpty() throws DataAccessException {
        assertTrue(gameDAO.listGames().isEmpty(), "empty list when no games exisit");
    }

    @Test //updates to game scenarios
    public void passUpdateGameValid() throws DataAccessException {
        int gameId = gameDAO.createGame("Update Test");
        GameData original = gameDAO.getGame(gameId);
        GameData updated = new GameData(
                gameId,
                "whiteUser",
                original.blackUsername(),
                original.gameName(),
                original.game()
        );

        gameDAO.updateGame(updated);
        GameData result = gameDAO.getGame(gameId);

        assertEquals("whiteUser", result.whiteUsername(), "White username should update");
    }

    @Test
    public void failUpdateGameInvalidId() {
        GameData fakeGame = new GameData(999, null, null, "Fake", new ChessGame());

        assertThrows(DataAccessException.class, () -> {
            gameDAO.updateGame(fakeGame);
        }, "updates to nonexistent game should throw exception");
    }

    @Test
    public void passClear() throws DataAccessException {
        gameDAO.createGame("Clear Test 1");
        gameDAO.createGame("Clear Test 2");

        gameDAO.clear();

        assertTrue(gameDAO.listGames().isEmpty(), "ALL games need to be cleared");
    }
}

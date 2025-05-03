package service;

import dataaccess.*;
import model.UserData;
import model.GameData;
import chess.ChessGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.CreateGameRequest;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    private GameService gameService;
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    CreateGameRequest req = new CreateGameRequest("test");

    @BeforeEach
    public void setUp() {
        //in memory version of dao for the tests
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        gameService = new GameService(gameDAO, authDAO);
    }

    @Test
    public void getValidIDReturnGame() throws DataAccessException {
        //game and return ID
        String authToken = authDAO.makeAuth("testuser");
        int gameID = gameService.createGame(req, authToken);
        //ID should be able to get the game
        GameData result = gameService.getGame(gameID);

        assertNotNull(result); //make must exist
        assertEquals(gameID, result.gameID());//game should match ID
    }

    @Test
    public void getInvalidGameID() {
        // Throw except fetching id that doesn't exist.
        assertThrows(DataAccessException.class, () ->
                gameService.getGame(999));
    }

    @Test
    public void updateGame() throws DataAccessException {
        String authToken = authDAO.makeAuth("testuser");
        int gameID = gameService.createGame(req, authToken);
        GameData original = gameService.getGame(gameID);//create/fetch game

        //updated fields same id
        GameData updated = new GameData(
                gameID,
                "newWhite",
                "newBlack",
                "Updated Game",
                original.game(),
                original.gameOver() // likely false
        );

        //update game method
        gameService.updateGame(authToken, updated);

        //check if it worked
        GameData result = gameService.getGame(gameID);
        assertEquals("newWhite", result.whiteUsername());
    }

    @Test
    public void invalidGameUpdate() throws DataAccessException {
        String authToken = authDAO.makeAuth("testuser");
        GameData invalidGame = new GameData(
                999,  // fake ID
                null,
                null,
                "Invalid Game",
                new ChessGame(),
                false  // not over (but irrelevant since it's invalid)
        );

        //needs to throw err cause it doesn't exist.
        assertThrows(DataAccessException.class, () ->
                gameService.updateGame(authToken, invalidGame));
    }

    @Test
    public void clearAllGames() throws DataAccessException {
        String authToken = authDAO.makeAuth("testuser");
        gameService.createGame(req, authToken);
        gameService.createGame(req, authToken);
        //clear the games just made
        gameService.clear();

        //list needs to be empty
        assertEquals(0, gameService.listGames(authToken).size());
    }

    @Test
    public void listInvalidAuth() {
        //try to list invalids
        assertThrows(DataAccessException.class, () ->
                gameService.listGames("invalid token"));
    }

    @Test
    public void validInputReturnID() throws DataAccessException {
        //user and auth tok
        UserData testUser = new UserData("testuser", "password", "test@example.com");
        String authToken = authDAO.makeAuth(testUser.username()); //get token
        int gameID = gameService.createGame(req, authToken);
        //game id valid if > 0
        assertTrue(gameID > 0);
    }

    @Test
    public void invalidAuthTokenGame() {
        //test invalid token to creat game
        assertThrows(DataAccessException.class, () -> gameService.createGame(req, "Gest Tame"));
    }

    @Test
    public void validAuthTokenList() throws DataAccessException {
        //multiple game us same val tok
        String authToken = authDAO.makeAuth("testuser");
        gameService.createGame(req, authToken);
        gameService.createGame(req, authToken);

        var games = gameService.listGames(authToken);
        //2 games
        assertEquals(2, games.size());
    }

    @Test
    public void joinGameWithValidRequest() throws DataAccessException {
        //join game as white player
        String authToken = authDAO.makeAuth("testuser");
        int gameID = gameService.createGame(req, authToken);

        //join on white side
        assertDoesNotThrow(() -> gameService.joinGame(authToken, gameID, "WHITE"));

        //white username should update
        GameData game = gameService.getGame(gameID);
        assertEquals("testuser", game.whiteUsername());
    }

    @Test
    public void useTakenPosition() throws DataAccessException {
        //2 user + 1 game
        String authToken1 = authDAO.makeAuth("user1");
        String authToken2 = authDAO.makeAuth("user2");
        int gameID = gameService.createGame(req, authToken1);
        //first user joins white
        gameService.joinGame(authToken1, gameID, "WHITE");
        //white spot should not be able to be taken
        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken2, gameID, "WHITE"));
    }

}
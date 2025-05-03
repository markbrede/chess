package service;

import chess.ChessMove;
import chess.InvalidMoveException;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import model.AuthData;
import request.CreateGameRequest;

import java.util.List;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public int createGame(CreateGameRequest req, String authToken) throws DataAccessException {
        authDAO.getAuth(authToken);
        return gameDAO.createGame(req.gameName());
    }

    public GameData getGame(int gameID) throws DataAccessException {
        return gameDAO.getGame(gameID);
    }

    public List<GameData> listGames(String authToken) throws DataAccessException {
        authDAO.getAuth(authToken);
        return gameDAO.listGames();
    }

    public void updateGame(String authToken, GameData updatedGame) throws DataAccessException {
        authDAO.getAuth(authToken);
        gameDAO.updateGame(updatedGame);
    }

    public void updateGame(GameData updatedGame) throws DataAccessException {
        gameDAO.updateGame(updatedGame);
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        return authDAO.getAuth(authToken);
    }

    public void clear() {
        gameDAO.clear();
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
        var auth = authDAO.getAuth(authToken);
        GameData game = gameDAO.getGame(gameID);

        if (playerColor == null || !List.of("WHITE", "BLACK").contains(playerColor.toUpperCase())) {
            throw new DataAccessException("Error: bad request");
        }

        boolean whiteTaken = game.whiteUsername() != null && !game.whiteUsername().equals(auth.username());
        boolean blackTaken = game.blackUsername() != null && !game.blackUsername().equals(auth.username());

        if (("WHITE".equalsIgnoreCase(playerColor) && whiteTaken) ||
            ("BLACK".equalsIgnoreCase(playerColor) && blackTaken)) {
            throw new DataAccessException("Error: already taken");
        }

        GameData updatedGame;
        if ("WHITE".equalsIgnoreCase(playerColor)) {
            updatedGame = new GameData(
                    game.gameID(),
                    auth.username(),
                    game.blackUsername(),
                    game.gameName(),
                    game.game(),
                    game.gameOver()
            );
        } else {
            updatedGame = new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    auth.username(),
                    game.gameName(),
                    game.game(),
                    game.gameOver()
            );
        }

        gameDAO.updateGame(updatedGame);
    }

    public void observeGame(String authToken, int gameID) throws DataAccessException {
        var auth = authDAO.getAuth(authToken);
        var game = gameDAO.getGame(gameID);

        if (auth == null || game == null) {
            throw new DataAccessException("Error: unauthorized or game not found");
        }
    }

    public GameData makeMove(String authToken, int gameId, ChessMove move) throws DataAccessException, InvalidMoveException {
        AuthData auth = getAuth(authToken);
        GameData game = getGame(gameId);

        if (game.gameOver()) {
            throw new DataAccessException("The game is over. No moves allowed.");
        }

        game.game().makeMove(move);

        GameData updatedGame = new GameData(
                game.gameID(),
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                game.game(),
                game.gameOver()
        );

        updateGame(authToken, updatedGame);
        return updatedGame;
    }

    public GameData resignGame(String authToken, int gameId) throws DataAccessException {
        AuthData auth = getAuth(authToken);
        GameData game = getGame(gameId);

        if (game.gameOver()) {
            throw new DataAccessException("Game is already over.");
        }

        if (!auth.username().equals(game.whiteUsername()) &&
            !auth.username().equals(game.blackUsername())) {
            throw new DataAccessException("Only players may resign.");
        }

        GameData updatedGame = new GameData(
                game.gameID(),
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                game.game(),
                true
        );

        updateGame(authToken, updatedGame);
        return updatedGame;
    }

    public String getUsernameFromAuth(String authToken) throws DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);
        return authData.username();
    }
}

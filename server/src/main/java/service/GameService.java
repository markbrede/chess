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

    //existing methods to define my business logic

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

    public AuthData getAuth(String authToken) throws DataAccessException {
        return authDAO.getAuth(authToken);
    }

    public void clear(){
        gameDAO.clear();
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
        var auth = authDAO.getAuth(authToken);  // val auth tok
        GameData game = gameDAO.getGame(gameID);  // val game exist

        //val color
        if (playerColor == null || !List.of("WHITE", "BLACK").contains(playerColor.toUpperCase())) {
            throw new DataAccessException("Error: bad request");
        }

        //val color avail
        if (("WHITE".equalsIgnoreCase(playerColor) && game.whiteUsername() != null) ||
            ("BLACK".equalsIgnoreCase(playerColor) && game.blackUsername() != null)) {
            throw new DataAccessException("Error: already taken");
        }

        //user to team color
        GameData updatedGame;
        if ("WHITE".equalsIgnoreCase(playerColor)) {
            updatedGame = new GameData(
                    game.gameID(),
                    auth.username(),
                    game.blackUsername(),
                    game.gameName(),
                    game.game()
            );
        } else {
            updatedGame = new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    auth.username(),
                    game.gameName(),
                    game.game()
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
        GameData game = getGame(gameId); //get current game

        game.game().makeMove(move); //apply the move

        //record immutable. save and make new one
        GameData updatedGame = new GameData(
                game.gameID(),
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                game.game()
        );

        updateGame(authToken, updatedGame);  //save to db

        return updatedGame;
    }

    //I will enhance with resignation logic later
    public GameData resignGame(String authToken, int gameId) throws DataAccessException {
        AuthData auth = getAuth(authToken);
        GameData game = getGame(gameId);

        if (!auth.username().equals(game.whiteUsername()) &&
            !auth.username().equals(game.blackUsername())) {
            throw new DataAccessException("You are not a player in this game.");
        }

        //mark game as updated
        GameData updatedGame = new GameData(
                game.gameID(),
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                game.game()
        );

        updateGame(authToken, updatedGame);
        return updatedGame;
    }

    public String getUsernameFromAuth(String authToken) throws DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);
        return authData.username();
    }


}

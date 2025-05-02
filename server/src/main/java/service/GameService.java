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
        var auth = authDAO.getAuth(authToken);//validate auth token

        GameData game = gameDAO.getGame(gameID);//check if it exists

        //team color availability
        if (playerColor != null) {
            if (!List.of("WHITE", "BLACK").contains(playerColor.toUpperCase())) {
                throw new DataAccessException("Error: bad request"); //invalid color
            }
            if (("WHITE".equalsIgnoreCase(playerColor) && game.whiteUsername() != null) ||
                    ("BLACK".equalsIgnoreCase(playerColor) && game.blackUsername() != null)) {
                throw new DataAccessException("Error: already taken");
            }

            //the updated data
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

        //for now just mark game as updated to trigger repull of game state
        GameData updatedGame = new GameData(
                game.gameID(),
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                game.game() //same state until I complete
        );

        updateGame(authToken, updatedGame);
        return updatedGame;
    }


}

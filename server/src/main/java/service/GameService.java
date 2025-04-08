package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
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
}

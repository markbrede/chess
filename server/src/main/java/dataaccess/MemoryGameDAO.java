package dataaccess;

import model.GameData;
import chess.ChessGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//GameDAO in memory for game
public class MemoryGameDAO implements GameDAO {

    private HashMap<Integer, GameData> games = new HashMap<>();//ID connects to game
    private int nextID = 1; //keep track

    @Override
    public int createGame(String gameName) throws DataAccessException {
        if (gameName == null || gameName.trim().isEmpty()) {
            throw new BadRequestException("Game name can't be empty!");
        }

        //new game
        int id = nextID;
        nextID++;

        GameData newGame = new GameData(id, null, null, gameName, new ChessGame()); //defaults
        games.put(id, newGame);
        return id;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        //loop through
        for (Integer id : games.keySet()) {
            if (id == gameID) {
                return games.get(id);
            }
        }
        throw new DataAccessException("Couldn't find game with ID: " + gameID);
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        //list of stored games
        List<GameData> list = new ArrayList<>();
        for (GameData g : games.values()) {
            list.add(g);
        }
        return list;
    }

    @Override
    public void updateGame(GameData updatedGame) throws DataAccessException {
        int id = updatedGame.gameID();
        if (!games.containsKey(id)) {
            throw new DataAccessException("No game found with ID: " + id);
        }

        games.remove(id);
        games.put(id, updatedGame);
    }

    @Override
    public void clear(){
        //reset map and id counter
        games.clear();
        nextID = 1;
    }
}
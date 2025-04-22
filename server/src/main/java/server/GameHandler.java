package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import model.GameData;
import request.CreateGameRequest;
import request.JoinGameRequest;
import response.CreateGameResponse;
import response.ListGamesResponse;
import service.GameService;
import spark.Request;
import spark.Response;
import java.util.List;
import java.util.Map;

public class GameHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public Object createGame(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            CreateGameRequest request = gson.fromJson(req.body(), CreateGameRequest.class);

            if (request.gameName() == null || request.gameName().isEmpty()) {
                throw new DataAccessException("Error: bad request");
            }

            int gameID = gameService.createGame(request, authToken);

            CreateGameResponse response = new CreateGameResponse(gameID);

            res.status(200);
            return gson.toJson(response);
        } catch (DataAccessException e) {
            return handleException(e, res);
        }
    }

    public Object listGames(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            if (authToken == null || authToken.isEmpty()) {
                throw new DataAccessException("Error: unauthorized");
            }

            List<GameData> games = gameService.listGames(authToken);
            ListGamesResponse response = new ListGamesResponse(games);
            res.status(200);
            return gson.toJson(response);
        } catch (DataAccessException e) {
            return handleException(e, res);
        }
    }

    public Object joinGame(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            if (authToken == null || authToken.isEmpty()) {
                throw new UnauthorizedException("Error: unauthorized");
            }

            JoinGameRequest request = gson.fromJson(req.body(), JoinGameRequest.class);

            if (request.gameID() == 0) {
                throw new DataAccessException("Error: bad request");
            }

            //validate color. Don't validate for observer
            if (request.playerColor() != null &&
                !List.of("WHITE", "BLACK").contains(request.playerColor().toUpperCase())) {
                throw new DataAccessException("Error: bad request");
            }

            //valid for observer or valid player
            gameService.joinGame(authToken, request.gameID(), request.playerColor());

            res.status(200);
            return "{}";
        } catch (DataAccessException e) {
            return handleException(e, res);
        }
    }


    //updating exception handler cause of 400 instead of 500 err
    private Object handleException(DataAccessException e, Response res) {
        //exception type check first
        if (e instanceof UnauthorizedException) {
            res.status(401);
        } else if (e.getMessage().equals("Error: bad request")) {
            res.status(400);
        } else if (e.getMessage().equals("Error: already taken")) {
            res.status(403);
        } else {
            res.status(500);
        }

        //setting content type to ensure proper json parsing
        res.type("application/json");
        return gson.toJson(Map.of("message", e.getMessage()));
    }
}
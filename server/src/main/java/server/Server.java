package server;

//**My server should be the entry point for all requests coming from the client.
//It's like the front door of my chess application.

import com.google.gson.Gson;
import dataaccess.*;
import service.GameService;
import service.UserService;
import spark.*;

import java.util.Map;

public class Server {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;

    private UserService userService;
    private GameService gameService;

    private UserHandler userHandler; //users http requests.
    private GameHandler gameHandler;

    public Server() {
        userDAO = new DBUserDAO(); // Was MemoryUserDAO
        authDAO = new DBAuthDAO();
        gameDAO = new DBGameDAO();

        //user service... dao dependencies
        userService = new UserService(userDAO, authDAO); //connects service to dataaccess
        gameService = new GameService(gameDAO, authDAO);

        //user handler... user service dependencies
        userHandler = new UserHandler(userService); //connect handler(http request processing) to service
        gameHandler = new GameHandler(gameService);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.webSocket("/ws", new WebSocketHandler(gameService));

        Spark.staticFiles.location("web");

        // HTTP routes
        Spark.post("/user", userHandler::register);
        Spark.post("/session", userHandler::login);
        Spark.delete("/session", userHandler::logout);
        Spark.get("/game", gameHandler::listGames);
        Spark.post("/game", gameHandler::createGame);
        Spark.put("/game", gameHandler::joinGame);
        Spark.put("/game/observe/:gameID", gameHandler::observeGame);
        Spark.delete("/db", this::clear);

        // Error handling
        Spark.exception(UnauthorizedException.class, (e, req, res) -> {
            res.status(401);
            res.body(new Gson().toJson(Map.of("message", e.getMessage())));
        });

        Spark.exception(BadRequestException.class, (e, req, res) -> {
            res.status(400);
            res.body(new Gson().toJson(Map.of("message", e.getMessage())));
        });

        Spark.awaitInitialization();
        return Spark.port();
    }


    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    //updating my clear method to also clear game data
    private Object clear(Request req, Response res) {
        userService.clear();
        gameService.clear(); //mem dao cant throw exception here. Adjusting this soon
        res.status(200);
        return "{}";
    }
}

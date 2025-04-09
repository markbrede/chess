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
        Spark.staticFiles.location("web");//where the HTML, CSS, JS will be served by the server


        Spark.post("/user", userHandler::register); //user routes to register
        Spark.post("/session", userHandler::login); //login (potentially buggy)
        Spark.delete("/session", userHandler::logout); //logout
        Spark.get("/game", gameHandler::listGames);
        Spark.post("/game", gameHandler::createGame);
        Spark.put("/game", gameHandler::joinGame);
        Spark.delete("/db", this::clear); //db route to clear method

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

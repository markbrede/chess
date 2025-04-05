package server;
//**My server should be the entry point for all requests coming from the client.
//It's like the front door of my chess application.
import com.google.gson.Gson;
import dataaccess.*;
import service.UserService;
import spark.*;

public class Server {

    private UserDAO userDAO;
    private AuthDAO authDAO;

    private UserService userService;

    private UserHandler userHandler; //users http requests.

    public Server() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();

        //UserService instance with DAOs as dependencies
        userService = new UserService(userDAO, authDAO); //connects service to dataaccess

        //UserHandler instance with UserService as dependency
        userHandler = new UserHandler(userService); //connect handler(http request processing) to service
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");//where the HTML, CSS, JS will be served by the server


        Spark.post("/user", userHandler::register); //user routes to register
        Spark.post("/session", userHandler::login); //login (potentially buggy)
        Spark.delete("/session", userHandler::logout); //logout
        Spark.delete("/db", this::clear); //db route to clear method

        // Exception handlers (commented out for now):
        //these would catch specific exceptions globally and return appropriate responses when I create them in dataaccess
        //Spark.exception(DataAccessException.class, this::dataAccessExceptionHandler);
        //Spark.exception(Exception.class, this::genericExceptionHandler);

        Spark.awaitInitialization();

        return Spark.port();
    }

    public void stop() {
        Spark.stop();

        Spark.awaitStop();
    }

    private Object clear(Request req, Response res) {
        //call clear on UserService to remove userdata data from database.
        userService.clear();

        res.status(200); //OK status code

        return "{}"; //empt json object
    }
}

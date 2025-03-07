package server;

import spark.*;
import com.google.gson.Gson;
import dataaccess.MemoryUserDAO;
import handler.RegisterHandler;
import service.UserService;
import service.ClearService;
import dataaccess.DataAccessException;

public class Server {
    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        MemoryUserDAO userDAO = new MemoryUserDAO();
        UserService userService = new UserService(userDAO);
        ClearService clearService = new ClearService(userDAO);

        Spark.post("/user", (req, res) -> {
            RegisterHandler registerHandler = new RegisterHandler(userService);
            return registerHandler.handle(req, res);
        });

        Spark.delete("/db", (req, res) -> {
            try {
                clearService.clearAll();
                res.type("application/json");
                res.status(200);
                return "{}";
            } catch (DataAccessException e) {
                res.status(500);
                return new Gson().toJson(new ErrorResponse(e.getMessage()));
            }
        });

        Spark.exception(Exception.class, (e, req, res) -> {
            res.status(500);
            res.type("application/json");
            res.body("{ \"message\": \"Error: " + e.getMessage() + "\" }");
        });

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private static class ErrorResponse {
        private final String message;

        ErrorResponse(String message) {
            this.message = message;
        }
    }
}

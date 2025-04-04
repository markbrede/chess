package server;
// Mark, as you finish up the register endpoint, think of the server like a receptionists.
// They receive requests from the server, figure out what the client wants to do (register a user),
// and delegate the task to the appropriate service. UserService in your case.
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;
import java.util.Map;
import dataaccess.BadRequestException;
import dataaccess.UnauthorizedException;

public class UserHandler {

    private final UserService userService;

    //UserService instance
    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public Object register(Request req, Response res) {
        Gson gson = new Gson();
        try {
            // Parse and validate input
            UserData userData = gson.fromJson(req.body(), UserData.class);
            if (userData.username() == null || userData.password() == null || userData.email() == null ||
                    userData.username().isEmpty() || userData.password().isEmpty() || userData.email().isEmpty()) {
                throw new BadRequestException("Error: bad request"); // throws here
            }

            //create user and return response
            AuthData authData = userService.makeUser(userData);
            res.status(200);
            return gson.toJson(Map.of("username", authData.username(), "authToken", authData.authToken()));

        } catch (BadRequestException e) {
            //empty fields or duplicate users (from DAO)
            res.status(400); //missing fields
            if (e.getMessage().equals("Error: username already exists")) {
                res.status(403); //when duplicate happens
            }
            return gson.toJson(Map.of("message", e.getMessage()));

        } catch (UnauthorizedException e) {
            //not used here. included for completeness
            res.status(401);
            return gson.toJson(Map.of("message", e.getMessage()));

        } catch (DataAccessException e) {
            //in case of unexpected errors
            res.status(500);
            return gson.toJson(Map.of("message", "Error: server error"));
        }
    }
}

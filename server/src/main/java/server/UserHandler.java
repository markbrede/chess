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

public class UserHandler {

    private final UserService userService;

    // Constructor takes a UserService instance
    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public Object register(Request req, Response res) {
        Gson gson = new Gson();
        try {
            // Parse the request body into a UserData object
            UserData userData = gson.fromJson(req.body(), UserData.class);

            // Validate user input
            if (userData.username() == null || userData.password() == null || userData.email() == null ||
                    userData.username().isEmpty() || userData.password().isEmpty() || userData.email().isEmpty()) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            // Call UserService to create a new user
            AuthData authData = userService.makeUser(userData);
            res.status(200);
            // Return success response with username and authToken
            return gson.toJson(Map.of("username", authData.username(), "authToken", authData.authToken()));
        } catch (DataAccessException e) {
            // Handle case where user already exists
            if (e.getMessage().equals("That user already exists")) {
                res.status(403);
                return gson.toJson(Map.of("message", "Error: already taken"));
            } else {
                // Handle other data access errors
                res.status(500);
                return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
            }
        } catch (Exception e) {
            // Handle any other unexpected errors
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}

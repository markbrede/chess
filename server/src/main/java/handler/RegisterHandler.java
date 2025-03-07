package handler;

import com.google.gson.Gson;
import service.UserService;
import spark.Request;
import spark.Response;
import dataaccess.DataAccessException;


public class RegisterHandler {
    private final UserService userService;
    private final Gson gson;

    public RegisterHandler(UserService userService) {
        this.userService = userService;
        this.gson = new Gson();
    }

    public Object handle(Request req, Response res) {
        try {
            RegisterRequest registerRequest = gson.fromJson(req.body(), RegisterRequest.class);
            String authToken = userService.register(registerRequest.username(), registerRequest.password(), registerRequest.email());
            RegisterResult result = new RegisterResult(registerRequest.username(), authToken);
            res.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            res.status(403);
            return gson.toJson(new ErrorResult("Error: already taken"));
        } catch (IllegalArgumentException e) {
            res.status(400);
            return gson.toJson(new ErrorResult("Error: bad request"));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResult("Error: " + e.getMessage()));
        }
    }


    private record RegisterRequest(String username, String password, String email) {}
    private record RegisterResult(String username, String authToken) {}
    private record ErrorResult(String message) {}
}

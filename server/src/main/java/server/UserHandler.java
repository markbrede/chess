package server;

// Mark, as you finish up the register endpoint, think of the server like a receptionists.
// They receive requests from the server, figure out what the client wants to do (register a user),
// and delegate the task to the appropriate service. UserService in your case.

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import request.LoginRequest;
import request.RegisterRequest;
import response.LoginResponse;
import response.RegisterResponse;
import service.UserService;
import spark.Request;
import spark.Response;
import java.util.Map;
import dataaccess.BadRequestException;
import dataaccess.UnauthorizedException;

public class UserHandler {

    private final UserService userService;
    private final Gson gson = new Gson(); //class level gson to fix standard api test error

    //UserService instance
    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public Object register(Request req, Response res) {
        try {
            // Parse and validate input
            RegisterRequest request = gson.fromJson(req.body(), RegisterRequest.class);
            if (request.username() == null || request.password() == null || request.email() == null ||
                    request.username().isEmpty() || request.password().isEmpty() || request.email().isEmpty()) {
                throw new BadRequestException("Error: bad request"); // throws here
            }

            //create user and return response
            AuthData authData = userService.makeUser(request);
            RegisterResponse response = new RegisterResponse(authData.username(), authData.authToken());
            res.status(200);

            return gson.toJson(response);

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

    //login. easy money
    public Object login(Request req, Response res) {
        try {
            //parse and validate input. Not for emails tho
            LoginRequest request = gson.fromJson(req.body(), LoginRequest.class);
            //if the username or password are missing,
            if (request.username() == null || request.password() == null ||
                    //OR if the username or pass is just an empty string,
                    request.username().isEmpty() || request.password().isEmpty()) {
                //then throw bad request error cause of the bad login creds
                throw new BadRequestException("Error: bad request");
            }

            //if they have good creds, call userservice to attempt login
            AuthData authData = userService.loginUser(request);
            res.status(200);
            //json response with username and password
            LoginResponse response = new LoginResponse(request.username(), authData.authToken());
            return gson.toJson(response);

        } catch (BadRequestException e) {
            res.status(400);
            return gson.toJson(Map.of("message", e.getMessage()));
        } catch (UnauthorizedException e) {
            res.status(401);
            return gson.toJson(Map.of("message", e.getMessage()));
        } catch (DataAccessException e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: server error"));
        }
    }

    //logout. clears session tokens.
    public Object logout(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");

            //valid auth toke that isn't empty
            if (authToken == null || authToken.isEmpty()) {
                throw new UnauthorizedException("Error: unauthorized");
            }

            //userservice method to logout
            userService.logoutUser(authToken);

            res.status(200);

            return "{}"; //empty json object if it goes as planned

        } catch (UnauthorizedException e) {

            res.status(401);

            return gson.toJson(Map.of("message", e.getMessage()));

        } catch (DataAccessException e) {

            res.status(500);

            return gson.toJson(Map.of("message", "Error: server error"));

        }
    }
}
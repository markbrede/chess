import com.google.gson.Gson;
import request.RegisterRequest;
import response.RegisterResponse;
import request.LoginRequest;
import response.LoginResponse;
import request.CreateGameRequest;
import request.JoinGameRequest;
import response.CreateGameResponse;
import response.ListGamesResponse;

import java.io.*;
import java.net.*;
import java.util.Map;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    //registration
    public RegisterResponse register(String username, String password, String email) throws Exception {
        RegisterRequest req = new RegisterRequest(username, password, email);
        var path = "/user";
        return this.makeRequest("POST", path, req, RegisterResponse.class, null);
    }

    //login
    public LoginResponse login(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest(username, password);
        var path = "/session";
        return this.makeRequest("POST", path, req, LoginResponse.class, null);
    }

    //logout
    public void logout(String authToken) throws Exception {
        var path = "/session";
        this.makeRequest("DELETE", path, null, null, authToken);
    }

    //list games
    public ListGamesResponse listGames(String authToken) throws Exception {
        var path = "/game";
        return this.makeRequest("GET", path, null, ListGamesResponse.class, authToken);
    }

    //create game
    public CreateGameResponse createGame(String gameName, String authToken) throws Exception {
        CreateGameRequest req = new CreateGameRequest(gameName);
        var path = "/game";
        return this.makeRequest("POST", path, req, CreateGameResponse.class, authToken);
    }

    //join game
    public void joinGame(String playerColor, int gameID, String authToken) throws Exception {
        JoinGameRequest req = new JoinGameRequest(playerColor, gameID);
        var path = "/game";
        this.makeRequest("PUT", path, req, null, authToken);
    }

    //clear database... testing
    public void clearDatabase() throws Exception {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null, null);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authtoken) throws Exception {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (authtoken != null) {
                http.setRequestProperty("Authorization", authtoken);
            }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw ex;
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, Exception {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    InputStreamReader reader = new InputStreamReader(respErr);
                    var error = new Gson().fromJson(reader, Map.class);
                    if (error != null && error.containsKey("message")) {
                        throw new Exception((String) error.get("message"));
                    }
                }
            }
            throw new Exception("Error: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}

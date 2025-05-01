package facade;

import com.google.gson.Gson;
import request.*;
import response.*;
import model.GameData;

import java.io.*;
import java.net.*;
import java.util.Map;

public class ServerFacade {
    private final String baseUrl;

    public ServerFacade(int port) {
        this.baseUrl = "http://localhost:" + port;
    }

    public RegisterResponse register(String username, String password, String email) throws Exception {
        var req = new RegisterRequest(username, password, email);
        return makeRequest("POST", "/user", req, RegisterResponse.class, null);
    }

    public LoginResponse login(String username, String password) throws Exception {
        var req = new LoginRequest(username, password);
        return makeRequest("POST", "/session", req, LoginResponse.class, null);
    }

    public void logout(String authToken) throws Exception {
        makeRequest("DELETE", "/session", null, null, authToken);
    }

    public ListGamesResponse listGames(String authToken) throws Exception {
        return makeRequest("GET", "/game", null, ListGamesResponse.class, authToken);
    }

    public CreateGameResponse createGame(String gameName, String authToken) throws Exception {
        var req = new CreateGameRequest(gameName);
        return makeRequest("POST", "/game", req, CreateGameResponse.class, authToken);
    }

    public void joinGame(String playerColor, int gameID, String authToken) throws Exception {
        var req = new JoinGameRequest(playerColor, gameID);
        makeRequest("PUT", "/game", req, null, authToken);
    }

    public void clearDatabase() throws Exception {
        makeRequest("DELETE", "/db", null, null, null);
    }

    //get latest game state
    public GameData getGame(int gameID, String authToken) throws Exception {
        return makeRequest("GET", "/game/" + gameID, null, GameData.class, authToken);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws Exception {
        URL url = (new URI(baseUrl + path)).toURL();
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod(method);
        http.setDoOutput(true);

        if (authToken != null) {
            http.setRequestProperty("Authorization", authToken);
        }

        writeBody(request, http);
        http.connect();
        throwIfNotSuccessful(http);
        return readBody(http, responseClass);
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.setRequestProperty("Content-Type", "application/json");
            String json = new Gson().toJson(request);
            try (OutputStream outputStream = http.getOutputStream()) {
                outputStream.write(json.getBytes());
            }
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        if (http.getContentLength() < 0 && responseClass != null) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                return new Gson().fromJson(reader, responseClass);
            }
        }
        return null;
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws Exception {
        int status = http.getResponseCode();
        if (status / 100 != 2) {
            String message = null;
            try (InputStream err = http.getErrorStream()) {
                if (err != null) {
                    var error = new Gson().fromJson(new InputStreamReader(err), Map.class);
                    message = (String) error.get("message");
                }
            }

            if (status == 401) throw new Exception("unauthorized");
            if (status == 400) throw new Exception("bad request");
            throw new Exception(message != null ? message : "HTTP Error " + status);
        }
    }
}

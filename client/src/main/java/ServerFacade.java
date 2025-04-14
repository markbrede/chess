import com.google.gson.Gson;
import request.RegisterRequest;
import response.RegisterResponse;
import request.LoginRequest;
import response.LoginResponse;

import java.io.*;
import java.net.*;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public static void main (String[] args) throws Exception {
        ServerFacade facade = new ServerFacade("http://localhost:8080");

        RegisterResponse res = facade.register("mark1", "password", "mark@gmail.com");
        System.out.println();
    }

//REGISTER
    public RegisterResponse register(String username, String password, String email) throws Exception {
        RegisterRequest req = new RegisterRequest(username, password, email);
        var path = "/user";
        return this.makeRequest("POST", path, req, RegisterResponse.class, null);
    }

//LOGIN
    public LoginResponse login(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest(username, password);
        var path = "/session";
        return this.makeRequest("POST", path, req, LoginResponse.class, null);
    }

//LOGOUT
    public void logout(String authToken) throws Exception {
        var path = "/session";
        this.makeRequest("DELETE", path, null, null, authToken);
    }



//CLEAR
    public void clearDatabase() throws Exception {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null, null);
    }



    public void deletePet(int id) throws Exception {
        var path = String.format("/pet/%s", id);
        this.makeRequest("DELETE", path, null, null, null);
    }

//    public Pet[] listPets() throws Exception {
//        var path = "/pet";
//        record listPetResponse(Pet[] pet) {
//        }
//        var response = this.makeRequest("GET", path, null, listPetResponse.class);
//        return response.pet();
//    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authtoken) throws Exception {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (authtoken != null) {
                // set headers
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
                    throw new Exception("error");
                }
            }

            throw new Exception();
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
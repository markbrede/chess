package model;
/**
 *Holds authentication token and username, used for session management.
 */
public record AuthData(String authToken, String username) {}

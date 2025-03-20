package model;
/**
 * Holds authentication data, specifically the authentication token and associated username, used for session management.
 */
public record AuthData(String authToken, String username) {}

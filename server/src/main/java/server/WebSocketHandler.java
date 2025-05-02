package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.GameService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles WebSocket connections and gameplay interactions for the chess application.
 */
@WebSocket
public class WebSocketHandler {

    private final GameService gameService;
    private final Gson gson;

    //track sessions, usernames, and games
    private final Map<Session, String> sessionToUser = new ConcurrentHashMap<>();
    private final Map<Integer, Set<Session>> gameToSessions = new ConcurrentHashMap<>();
    private final Map<Integer, Boolean> gameInProgress = new ConcurrentHashMap<>();

    public WebSocketHandler(GameService gameService) {
        this.gameService = gameService;
        this.gson = new Gson();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connection established");
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {

        String username = sessionToUser.remove(session); //get username before rmv map

        for (Map.Entry<Integer, Set<Session>> entry : gameToSessions.entrySet()) {
            Integer gameId = entry.getKey();
            Set<Session> gameSessions = entry.getValue();

            if (gameSessions.remove(session) && username != null) {
                //lets the other players know
                for (Session otherSession : gameSessions) {
                    sendNotification(otherSession, username + " has disconnected");
                }
            }
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            UserGameCommand.CommandType type = gson.fromJson(json.get("commandType"), UserGameCommand.CommandType.class);

            UserGameCommand command;
            switch (type) {
                case MAKE_MOVE:
                    command = gson.fromJson(message, MakeMoveCommand.class);
                    break;
                default:
                    command = gson.fromJson(message, UserGameCommand.class);
                    break;
            }

            switch (command.getCommandType()) {
                case CONNECT:
                    handleConnect(session, command);
                    break;
                case MAKE_MOVE:
                    handleMakeMove(session, command);
                    break;
                case LEAVE:
                    handleLeave(session, command);
                    break;
                case RESIGN:
                    handleResign(session, command);
                    break;
                default:
                    sendError(session, "Unknown command type");
            }
        } catch (Exception e) {
            sendError(session, "Error processing command: " + e.getMessage());
        }
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
    }

    //comm handlers
    private void handleConnect(Session session, UserGameCommand command) {
        try {
            String authToken = command.getAuthToken();
            Integer gameId = command.getGameID();

            AuthData authData = gameService.getAuth(authToken);

            GameData game = gameService.getGame(gameId);

            //store sesh info
            sessionToUser.put(session, authData.username());
            gameToSessions.computeIfAbsent(gameId, k -> new HashSet<>()).add(session);


            sendGameState(session, game); //send game state to client

            //let others know what's going on
            for (Session otherSession : gameToSessions.getOrDefault(gameId, new HashSet<>())) {
                if (!otherSession.equals(session)) {
                    sendNotification(otherSession, authData.username() + " has joined the game");
                }
            }
        } catch (DataAccessException e) {
            sendError(session, "Error connecting to game: " + e.getMessage());
        }
    }

    private void handleMakeMove(Session session, UserGameCommand command) {
        try {
            String authToken = command.getAuthToken();
            Integer gameId = command.getGameID();
            //fix moving after resign issue
            if (gameInProgress.getOrDefault(gameId, true) == false) {
                sendError(session, "The game is now over. No more moves are allowed.");
                return;
            }

            if (!(command instanceof MakeMoveCommand)) {
                sendError(session, "Invalid move command format.");
                return;
            }
            ChessMove move = ((MakeMoveCommand) command).getMove();

            String username = sessionToUser.get(session);
            if (username == null) {
                sendError(session, "User not recognized for this session.");
                return;
            }

            GameData game = gameService.makeMove(authToken, gameId, move);

            for (Session s : gameToSessions.getOrDefault(gameId, new HashSet<>())) {
                sendGameState(s, game);
                if (!s.equals(session)) {
                    sendNotification(s, username + " made a move: " + move.toString());
                }
            }

            if (isGameOver(game.game())) {
                for (Session s : gameToSessions.getOrDefault(gameId, new HashSet<>())) {
                    sendNotification(s, "Game over!");
                }
                gameInProgress.put(gameId, false);
            }
        } catch (InvalidMoveException e) {
            sendError(session, "Invalid move: " + e.getMessage());
        } catch (Exception e) {
            sendError(session, "Error making move: " + e.getMessage());
        }
    }

    private void handleLeave(Session session, UserGameCommand command) {
        try {
            Integer gameId = command.getGameID();
            String username = sessionToUser.get(session);


            Set<Session> gameSessions = gameToSessions.get(gameId);
            if (gameSessions != null) {
                gameSessions.remove(session); //rmv sesh from game

                //let others know what's going on
                for (Session otherSession : gameSessions) {
                    sendNotification(otherSession, username + " has left the game");
                }
            }
        } catch (Exception e) {
            sendError(session, "Error leaving game: " + e.getMessage());
        }
    }

    //need to add resignGame in gameservice for method to be usable
    private void handleResign(Session session, UserGameCommand command) {
        try {
            String authToken = command.getAuthToken();
            Integer gameId = command.getGameID();
            String username = sessionToUser.get(session);
            if (username == null) {
                sendError(session, "User not recognized for this session.");
                return;
            }

            gameInProgress.put(gameId, false); //game marked as over

            //reflect resignation
            GameData updatedGame = gameService.resignGame(authToken, gameId);
            notifyAllPlayers(gameId, updatedGame, session, "resigned from the game");

            Set<Session> gameSessions = gameToSessions.get(gameId);
            if (gameSessions != null) {
                for (Session playerSession : gameSessions) {
                    sendNotification(playerSession, username + " resigned from the game");
                }
            }
        } catch (Exception e) {
            sendError(session, "Error resigning from game: " + e.getMessage());
        }
    }

    //helper methods
    private boolean isGameOver(ChessGame game) {
        //checkmate or stalemate check
        return game.isInCheckmate(ChessGame.TeamColor.WHITE) ||
               game.isInCheckmate(ChessGame.TeamColor.BLACK) ||
               game.isInStalemate(ChessGame.TeamColor.WHITE) ||
               game.isInStalemate(ChessGame.TeamColor.BLACK);
    }

    private void notifyAllPlayers(Integer gameId, GameData game, Session sourceSession, String action) {
        Set<Session> gameSessions = gameToSessions.get(gameId);
        if (gameSessions != null) {
            for (Session playerSession : gameSessions) {
                //update players with game st
                sendGameState(playerSession, game);

                //let 'em know what's goin on
                if (!playerSession.equals(sourceSession)) {
                    String username = sessionToUser.get(sourceSession);
                    sendNotification(playerSession, username + " " + action);
                }
            }
        }
    }

    private void sendGameState(Session session, GameData game) {
        try {
            ServerMessage message = createLoadGameMessage(game);

            session.getRemote().sendString(gson.toJson(message));
        } catch (IOException e) {
            System.err.println("Error sending game state: " + e.getMessage());
        }
    }

    private void sendNotification(Session session, String notificationText) {
        try {
            ServerMessage message = createNotificationMessage(notificationText);

            session.getRemote().sendString(gson.toJson(message));
        } catch (IOException e) {
            System.err.println("Error sending notification: " + e.getMessage());
        }
    }

    private void sendError(Session session, String errorMessage) {
        try {
            ServerMessage message = createErrorMessage(errorMessage);

            session.getRemote().sendString(gson.toJson(message));
        } catch (IOException e) {
            System.err.println("Error sending error message: " + e.getMessage());
        }
    }

    //message creation methods
    private ServerMessage createLoadGameMessage(GameData game) {
        return new LoadGameMessage(game);
    }

    private ServerMessage createNotificationMessage(String message) {
        return new NotificationMessage(message);
    }

    private ServerMessage createErrorMessage(String errorMessage) {
        return new ErrorMessage(errorMessage);
    }

}

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
import websocket.commands.ObserveGameCommand;
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
    private final Map<Integer, Set<Session>> gameToObservers = new ConcurrentHashMap<>();

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
        String username = sessionToUser.remove(session);

        for (Map.Entry<Integer, Set<Session>> entry : gameToSessions.entrySet()) {
            Integer gameId = entry.getKey();
            Set<Session> gameSessions = entry.getValue();

            if (gameSessions.remove(session) && username != null) {
                try {
                    GameData game = gameService.getGame(gameId);
                    if (game != null) {
                        String newWhite = username.equals(game.whiteUsername()) ? null : game.whiteUsername();
                        String newBlack = username.equals(game.blackUsername()) ? null : game.blackUsername();

                        GameData updatedGame = new GameData(
                                game.gameID(),
                                newWhite,
                                newBlack,
                                game.gameName(),
                                game.game(),
                                game.gameOver()
                        );
                        gameService.updateGame(updatedGame);
                    }
                } catch (DataAccessException e) {
                    e.printStackTrace(); // Optional: replace with logging
                }

                for (Session otherSession : gameSessions) {
                    sendNotification(otherSession, username + " has disconnected");
                }
            }
        }

        for (Map.Entry<Integer, Set<Session>> entry : gameToObservers.entrySet()) {
            entry.getValue().remove(session);
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
                case OBSERVE:
                    ObserveGameCommand observeCommand = gson.fromJson(message, ObserveGameCommand.class);
                    handleObserveGame(session, observeCommand);
                    return;
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

    private void handleObserveGame(Session session, ObserveGameCommand command) {
        try {
            int gameID = command.getGameID();
            String authToken = command.getAuthToken();
            String username = gameService.getUsernameFromAuth(authToken);

            GameData game = gameService.getGame(gameID);
            if (game == null) {
                sendError(session, "Game doesn't exist.");
                return;
            }

            sessionToUser.put(session, username);
            gameToObservers.putIfAbsent(gameID, ConcurrentHashMap.newKeySet());
            gameToObservers.get(gameID).add(session);

            LoadGameMessage message = new LoadGameMessage(game.game(), null);

            session.getRemote().sendString(gson.toJson(message));

            broadcastToPlayersAndObservers(gameID, new NotificationMessage(username + " is observing the game."));
        } catch (Exception e) {
            sendError(session, "Error observing game: " + e.getMessage());
        }
    }

    private void handleMakeMove(Session session, UserGameCommand command) {
        try {
            String authToken = command.getAuthToken();
            Integer gameId = command.getGameID();

            GameData game = gameService.getGame(gameId);
            String username = sessionToUser.get(session);

            if (username == null) {
                sendError(session, "User not recognized for this session.");
                return;
            }

            if (game.gameOver()) {
                sendError(session, "The game is over. No moves allowed.");
                return;
            }

            if (!(command instanceof MakeMoveCommand)) {
                sendError(session, "Invalid move command format.");
                return;
            }

            ChessMove move = ((MakeMoveCommand) command).getMove();
            ChessGame.TeamColor movingColor = game.game().getBoard().getPiece(move.getStartPosition()).getTeamColor();

            //turn base logic enforced
            if ((movingColor == ChessGame.TeamColor.WHITE && !username.equals(game.whiteUsername())) ||
                (movingColor == ChessGame.TeamColor.BLACK && !username.equals(game.blackUsername()))) {
                sendError(session, "You can't move for your opponent!");
                return;
            }

            //make the move
            GameData updatedGame = gameService.makeMove(authToken, gameId, move);

            for (Session s : gameToSessions.getOrDefault(gameId, new HashSet<>())) {
                sendGameState(s, updatedGame);
                if (!s.equals(session)) {
                    sendNotification(s, username + " made a move: " + move.toString());
                }
            }

            for (Session observer : gameToObservers.getOrDefault(gameId, Set.of())) {
                sendGameState(observer, updatedGame);
                sendNotification(observer, username + " made a move: " + move.toString());
            }

            if (isGameOver(updatedGame.game())) {
                gameInProgress.put(gameId, false);
                GameData gameOverState = new GameData(
                        updatedGame.gameID(),
                        updatedGame.whiteUsername(),
                        updatedGame.blackUsername(),
                        updatedGame.gameName(),
                        updatedGame.game(),
                        true
                );
                gameService.updateGame(gameOverState);

                for (Session s : gameToSessions.getOrDefault(gameId, new HashSet<>())) {
                    sendNotification(s, "Game over!");
                }
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
                gameSessions.remove(session);
            }

            // Remove user from game in the DB
            GameData game = gameService.getGame(gameId);
            if (game != null) {
                String newWhite = username.equals(game.whiteUsername()) ? null : game.whiteUsername();
                String newBlack = username.equals(game.blackUsername()) ? null : game.blackUsername();

                GameData updatedGame = new GameData(
                        game.gameID(),
                        newWhite,
                        newBlack,
                        game.gameName(),
                        game.game(),
                        game.gameOver()
                );
                gameService.updateGame(updatedGame);

                for (Session otherSession : gameToSessions.getOrDefault(gameId, new HashSet<>())) {
                    sendNotification(otherSession, username + " has left the game");
                }
            }
        } catch (Exception e) {
            sendError(session, "Error leaving game: " + e.getMessage());
        }
    }

    private void handleResign(Session session, UserGameCommand command) {
        try {
            int gameID = command.getGameID();
            String authToken = command.getAuthToken();
            String username = gameService.getUsernameFromAuth(authToken);

            GameData game = gameService.getGame(gameID);

            if (!username.equals(game.whiteUsername()) && !username.equals(game.blackUsername())) {
                sendError(session, "Only players may resign.");
                return;
            }

            if (game.gameOver()) {
                sendError(session, "Game is already over.");
                return;
            }

            GameData updatedGame = new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    game.blackUsername(),
                    game.gameName(),
                    game.game(),
                    true // mark it over
            );
            gameService.updateGame(updatedGame);

            String resignMessage = username + " has resigned.";
            broadcastToPlayersAndObservers(gameID, new NotificationMessage(resignMessage));

        } catch (Exception e) {
            sendError(session, "Failed to resign: " + e.getMessage());
        }
    }

    private boolean isGameOver(ChessGame game) {
        return game.isInCheckmate(ChessGame.TeamColor.WHITE) ||
               game.isInCheckmate(ChessGame.TeamColor.BLACK) ||
               game.isInStalemate(ChessGame.TeamColor.WHITE) ||
               game.isInStalemate(ChessGame.TeamColor.BLACK);
    }

    private void notifyAllPlayers(Integer gameId, GameData game, Session sourceSession, String action) {
        Set<Session> gameSessions = gameToSessions.get(gameId);
        if (gameSessions != null) {
            for (Session playerSession : gameSessions) {
                sendGameState(playerSession, game);
                if (!playerSession.equals(sourceSession)) {
                    String username = sessionToUser.get(sourceSession);
                    sendNotification(playerSession, username + " " + action);
                }
            }
        }
    }

    private void broadcastToPlayersAndObservers(int gameID, ServerMessage message) {
        Set<Session> players = gameToSessions.getOrDefault(gameID, Set.of());
        Set<Session> observers = gameToObservers.getOrDefault(gameID, Set.of());

        for (Session session : players) {
            try {
                session.getRemote().sendString(gson.toJson(message));
            } catch (IOException e) {
                System.err.println("Failed nroadcast to player: " + e.getMessage());
            }
        }

        for (Session session : observers) {
            try {
                session.getRemote().sendString(gson.toJson(message));
            } catch (IOException e) {
                System.err.println("Failed broadcast to observer: " + e.getMessage());
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
            System.err.println("Erorr sending notification: " + e.getMessage());
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

    private ServerMessage createLoadGameMessage(GameData game) {
        return new LoadGameMessage(game.game(), null);
    }

    private ServerMessage createNotificationMessage(String message) {
        return new NotificationMessage(message);
    }

    private ServerMessage createErrorMessage(String errorMessage) {
        return new ErrorMessage(errorMessage);
    }
}

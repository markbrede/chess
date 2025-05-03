package websocket.messages;

import chess.ChessGame;
import model.GameData;

public class LoadGameMessage extends ServerMessage {
    private final ChessGame game;
    private final ChessGame.TeamColor perspective;

    public LoadGameMessage(ChessGame game, ChessGame.TeamColor perspective) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
        this.perspective = perspective;
    }

    public ChessGame getGame() {
        return game;
    }

    public ChessGame.TeamColor getPerspective() {
        return perspective;
    }
}

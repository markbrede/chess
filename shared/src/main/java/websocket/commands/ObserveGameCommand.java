package websocket.commands;

public class ObserveGameCommand extends UserGameCommand {
    public ObserveGameCommand(String authToken, int gameID) {
        super(CommandType.OBSERVE, authToken, gameID);
    }
}

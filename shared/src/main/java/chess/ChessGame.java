package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private ChessGame.TeamColor correctColorsTurn;


    //loading up the constructor and linking them to the above fields
    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.correctColorsTurn = ChessGame.TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    //get correct team colors turn
    public TeamColor getTeamTurn() {
        return correctColorsTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    //set correct team colors turn
    public void setTeamTurn(TeamColor team) {
        this.correctColorsTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        throw new RuntimeException("Not implemented");
    }


    //CREATES COPY OF THE BOARD
    /**I am going to have this code create a copy of the board so that valid moves can be verified without changing
    the actual board in the game**/
    private ChessBoard copyOfCurrentBoard() {
        ChessBoard copiedBoard = new ChessBoard();
        for (int row = 0; row <= 7; row++) {
            for (int col = 0; col <= 7; col++) {
                ChessPosition pieceLocation = new ChessPosition(row, col);
                ChessPiece thePiece = board.getPiece(pieceLocation);
                if (thePiece != null) {
                    copiedBoard.addPiece(pieceLocation, new ChessPiece(thePiece.getTeamColor(), thePiece.getPieceType()));
                }
            }
        }
        return copiedBoard;
    }



    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */

    //set board
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    //get board
    public ChessBoard getBoard() {
        return board;
    }
}

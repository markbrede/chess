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

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.correctColorsTurn = ChessGame.TeamColor.WHITE;
    }
    /**
     * @return Which team's turn it is
     */
    // Determines correct teams turn
    public TeamColor getTeamTurn() {
        return correctColorsTurn;
    }
    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    // Set turn for correct team.
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
    // VALID MOVES METHOD
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        //piece is going to be the startPosition
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) return null;

        Collection<ChessMove> movePossibilities = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : movePossibilities) {
            ChessBoard tempBoard = copyOfCurrentBoard();
            tempBoard.addPiece(move.getEndPosition(), piece); // Move the piece to the new spot.
            tempBoard.addPiece(startPosition, null);
            if (!isInCheck(piece.getTeamColor(), tempBoard)) {
                validMoves.add(move);
            }
        }
        return validMoves; // Return all valid moves
    }
    //COPY OF BOARD.
    private ChessBoard copyOfCurrentBoard() {
        ChessBoard copiedBoard = new ChessBoard();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
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
        ChessPiece piece = board.getPiece(move.getStartPosition());

        if (piece == null || piece.getTeamColor() != correctColorsTurn) {
            throw new InvalidMoveException();
        }
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (validMoves == null || !validMoves.contains(move)) {
            throw new InvalidMoveException();
        }

        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);
        // Replace with promotion piece.
        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(correctColorsTurn, move.getPromotionPiece()));
        }
        //other players turn
        correctColorsTurn = (correctColorsTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }
    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(ChessGame.TeamColor teamColor) {
        return isInCheck(teamColor, board);
    }
    // Determine if opposing teams KING is in check.
    private boolean isInCheck(ChessGame.TeamColor teamColor, ChessBoard tempBoard) {
        ChessPosition kingPos = null;
        // Get position information
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition posInfo = new ChessPosition(row, col);
                ChessPiece piece = tempBoard.getPiece(posInfo);
                // Check if piece is KING
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPos = posInfo;
                    break;
                }
            }
        }
        if (kingPos == null) return false;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition posInfoOppT = new ChessPosition(row, col);
                ChessPiece piece = tempBoard.getPiece(posInfoOppT);

                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> moves = piece.pieceMoves(tempBoard, posInfoOppT);
                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(kingPos)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    // HELPER (isInCheck, isInStalemate) BOARD OPERATIONS
    @FunctionalInterface
    private interface BoardPositionConsumer {
        void accept(int row, int col);
    }
    // Iterate over every position on the board and apply the action
    private void forEachBoardPosition(BoardPositionConsumer action) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                action.accept(row, col);
            }
        }
    }
    // Check if a team has any valid moves
    private boolean hasAnyValidMoves(TeamColor teamColor) {
        final boolean[] hasMove = {false};
        forEachBoardPosition((row, col) -> {
            ChessPosition posInfo = new ChessPosition(row, col);
            ChessPiece piece = board.getPiece(posInfo);
            if (!hasMove[0] && piece != null && piece.getTeamColor() == teamColor) {
                if (!validMoves(posInfo).isEmpty()) {
                    hasMove[0] = true;
                }
            }
        });
        return hasMove[0];
    }
    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(ChessGame.TeamColor teamColor) {
        return isInCheck(teamColor) && !hasAnyValidMoves(teamColor);
    }
    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && !hasAnyValidMoves(teamColor);
    }
    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }
    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}

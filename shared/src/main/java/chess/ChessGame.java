package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    public TeamColor getTeamTurn() {
        return correctColorsTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
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
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null){
            return null;
        }

        Collection<ChessMove> movePossibilities = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : movePossibilities) {
            if (isValidMove(move, piece)) {
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    //HELPER METHOD. Validate individual moves.
    private boolean isValidMove(ChessMove move, ChessPiece piece) {
        ChessBoard tempBoard = copyOfCurrentBoard();
        tempBoard.addPiece(move.getEndPosition(), piece);
        tempBoard.addPiece(move.getStartPosition(), null);
        return !isInCheck(piece.getTeamColor(), tempBoard);
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
        correctColorsTurn = (correctColorsTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE; //Other players turn
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
        ChessPosition kingPos = getKing(teamColor, tempBoard);
        if (kingPos == null) {
            return false;
        }
        return inDanger(kingPos, teamColor, tempBoard);
    }
    //HELPER
    private ChessPosition getKing(TeamColor teamColor, ChessBoard tempBoard) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = tempBoard.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor &&
                        piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return pos;
                }
            }
        }
        return null;
    }
    //HELPER
    private boolean inDanger(ChessPosition position, TeamColor teamColor, ChessBoard tempBoard) {
        List<ChessMove> otherTeamMoves = new ArrayList<>();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = tempBoard.getPiece(pos);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    otherTeamMoves.addAll(piece.pieceMoves(tempBoard, pos));
                }
            }
        }

        for (ChessMove move : otherTeamMoves) {
            if (move.getEndPosition().equals(position)) {
                return true;
            }
        }

        return false;
    }

    //HELPER (isInCheck, isInStalemate) BOARD OPERATIONS
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
    private boolean anyValidMoves(TeamColor teamColor) {
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
        return isInCheck(teamColor) && !anyValidMoves(teamColor);
    }
    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && !anyValidMoves(teamColor);
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

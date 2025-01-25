package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor pieceColor;
    private ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }
    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();

        if (this.type == PieceType.BISHOP) {
            moves.addAll(getBishopMoves(board, myPosition));
        }
        // Handle other piece types...

        return moves;
    }

    private Collection<ChessMove> getBishopMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> bishopMoves = new ArrayList<>();
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}}; // Diagonal directions

        for (int[] direction : directions) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            while (true) {
                row += direction[0];
                col += direction[1];

                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    break; // Out of bounds
                }

                ChessPosition newPosition = new ChessPosition(row, col);
                ChessPiece pieceAtNewPosition = board.getPiece(newPosition);

                if (pieceAtNewPosition == null) {
                    bishopMoves.add(new ChessMove(myPosition, newPosition, null));
                } else if (pieceAtNewPosition.getTeamColor() != this.pieceColor) {
                    bishopMoves.add(new ChessMove(myPosition, newPosition, null));
                    break; // Can't move past an enemy piece
                } else {
                    break; // Can't move past a friendly piece
                }
            }
        }

        return bishopMoves;
    }


    //TOSTRING METHOD
    @Override
    public String toString() {
        char pieceChar;
        switch (type) {
            case KING -> pieceChar = 'K';
            case QUEEN -> pieceChar = 'Q';
            case BISHOP -> pieceChar = 'B';
            case KNIGHT -> pieceChar = 'N';
            case ROOK -> pieceChar = 'R';
            case PAWN -> pieceChar = 'P';
            default -> pieceChar = '?';
        }
        return pieceColor == ChessGame.TeamColor.WHITE ?
                String.valueOf(pieceChar) :
                String.valueOf(Character.toLowerCase(pieceChar));
    }

}














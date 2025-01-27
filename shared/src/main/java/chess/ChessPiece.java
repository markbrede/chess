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

        if (this.type == PieceType.KING) {
            moves.addAll(getKingMoves(board, myPosition));
        }

        return moves;
    }

    private ArrayList<ChessMove> getKingMoves(ChessBoard board, ChessPosition myPosition) {

        ArrayList<ChessMove> KingMoves = new ArrayList<>();

        /*Any direction one time*/
        ChessPosition[] positions = {
                new ChessPosition(myPosition.getRow()+1, myPosition.getColumn()),    //up
                new ChessPosition(myPosition.getRow()+1, myPosition.getColumn()+1),  //up right
                new ChessPosition(myPosition.getRow(), myPosition.getColumn()+1),    //right
                new ChessPosition(myPosition.getRow()-1, myPosition.getColumn()+1),  //down right
                new ChessPosition(myPosition.getRow()-1, myPosition.getColumn()),    //down
                new ChessPosition(myPosition.getRow()-1, myPosition.getColumn()-1),  //down left
                new ChessPosition(myPosition.getRow(), myPosition.getColumn()-1),    //left
                new ChessPosition(myPosition.getRow()+1, myPosition.getColumn()-1)   //up left
        };
        //check positions
        for (ChessPosition newPosition : positions) {
            //ensure the move is within the 1 8 matrix
            if (newPosition.getRow() >= 1 && newPosition.getRow() <= 8 &&
                    newPosition.getColumn() >= 1 && newPosition.getColumn() <= 8) {

                ChessPiece pieceOnPosition = board.getPiece(newPosition);

                //take the now validated pieceOnPosition and move if the square is empty or contains enemy piece
                if (pieceOnPosition == null ||
                        pieceOnPosition.getTeamColor() != this.getTeamColor()) {
                    KingMoves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }

        return KingMoves;
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














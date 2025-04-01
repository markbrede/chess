package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

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
        if (this.type == PieceType.ROOK) {
            moves.addAll(getRookMoves(board, myPosition));
        }
        if (this.type == PieceType.BISHOP) {
            moves.addAll(getBishopMoves(board, myPosition));
        }
        if (this.type == PieceType.QUEEN) {
            moves.addAll(getQueenMoves(board, myPosition));
        }
        if (this.type == PieceType.KNIGHT) {
            moves.addAll(getKnightMoves(board, myPosition));
        }
        if (this.type == PieceType.PAWN) {
            moves.addAll(getPawnMoves(board, myPosition));
        }

        return moves;
    }



    //ROOK
    private ArrayList<ChessMove> getRookMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> RookMoves = new ArrayList<>();

        //Directions
        int[][] directions = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1}
        };

        for (int[] direction : directions){
            int newRow = myPosition.getRow();
            int newCol = myPosition.getColumn();

            while (true){
                newRow += direction[0];
                newCol += direction[1];

                if (newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8){
                    break;
                }
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceOnPosition = board.getPiece(newPosition);

                // Add move if the square is empty or contains an enemy piece
                if (pieceOnPosition == null || pieceOnPosition.getTeamColor() != this.getTeamColor()) {
                    RookMoves.add(new ChessMove(myPosition, newPosition, null));
                }
                if (pieceOnPosition != null){
                    break;
                }
            }
        }

        return RookMoves;
    }

    //KNIGHT
    private ArrayList<ChessMove> getKnightMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> knightMoves = new ArrayList<>();

        // Define all possible L-shaped moves for the knight
        int[][] directions = {
                {2, 1},   {2, -1},   // Move two up, one left/right
                {-2, 1},  {-2, -1},  // Move two down, one left/right
                {1, 2},   {1, -2},   // Move one up, two left/right
                {-1, 2},  {-1, -2}   // Move one down, two left/right
        };

        for (int[] direction : directions) {
            int newRow = myPosition.getRow() + direction[0];
            int newCol = myPosition.getColumn() + direction[1];

            // Ensure the move is within the 8x8 board bounds
            if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceOnPosition = board.getPiece(newPosition);

                // Add move if the square is empty or contains an enemy piece
                if (pieceOnPosition == null || pieceOnPosition.getTeamColor() != this.getTeamColor()) {
                    knightMoves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }

        return knightMoves;
    }

    //BISHOP
    private ArrayList<ChessMove> getBishopMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> BishopMoves = new ArrayList<>();

        int[][] directions = {
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1}
        };
        for (int[] direction : directions) {
            int newRow = myPosition.getRow();
            int newCol = myPosition.getColumn();

            while (true) {
                newRow += direction[0];
                newCol += direction[1];

                if (newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8) {
                    break;
                }
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceOnPosition = board.getPiece(newPosition);

                // Add move if the square is empty or contains an enemy piece
                if (pieceOnPosition == null || pieceOnPosition.getTeamColor() != this.getTeamColor()) {
                    BishopMoves.add(new ChessMove(myPosition, newPosition, null));
                }
                if (pieceOnPosition != null) {
                    break;
                }
            }
        }

        return BishopMoves;
    }

    //QUEEN
    private ArrayList<ChessMove> getQueenMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> QueenMoves = new ArrayList<>();

        QueenMoves.addAll(getBishopMoves(board, myPosition));
        QueenMoves.addAll(getRookMoves(board, myPosition));

        return QueenMoves;
    }


    //KING
    private ArrayList<ChessMove> getKingMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> kingMoves = new ArrayList<>();

        // Define all possible directions for the king
        int[][] directions = {
                {1, 0},   // up
                {1, 1},   // up right
                {0, 1},   // right
                {-1, 1},  // down right
                {-1, 0},  // down
                {-1, -1}, // down left
                {0, -1},  // left
                {1, -1}   // up left
        };

        for (int[] direction : directions) {
            int newRow = myPosition.getRow() + direction[0];
            int newCol = myPosition.getColumn() + direction[1];

            // Ensure the move is within the 8x8 board bounds
            if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceOnPosition = board.getPiece(newPosition);

                // Add move if the square is empty or contains an enemy piece
                if (pieceOnPosition == null || pieceOnPosition.getTeamColor() != this.getTeamColor()) {
                    kingMoves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }

        return kingMoves;
    }


    //PAWN
    private ArrayList<ChessMove> getPawnMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> pawnMoves = new ArrayList<>();
        int direction = (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;

        int newRow = myPosition.getRow() + direction;
        int newCol = myPosition.getColumn();

        if (newRow >= 1 && newRow <=8){
            ChessPosition newPosition = new ChessPosition(newRow, newCol);
            ChessPiece pieceOnPosition = board.getPiece(newPosition);
            if (pieceOnPosition == null){
                if (newRow == 1 || newRow == 8){
                    pawnMoves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                    pawnMoves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                    pawnMoves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
                    pawnMoves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                } else {
                    pawnMoves.add(new ChessMove(myPosition, newPosition, null));
                }

                // Double move from starting position
                int startRow = (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;
                if (myPosition.getRow() == startRow) {
                    int twoStepRow = myPosition.getRow() + (2 * direction);
                    ChessPosition twoStepPosition = new ChessPosition(twoStepRow, newCol);
                    if (board.getPiece(twoStepPosition) == null) {
                        pawnMoves.add(new ChessMove(myPosition, twoStepPosition, null));
                    }
                }
            }
        }

        // Capturing diagonally
        int[][] attackDirections = {{direction, -1}, {direction, 1}}; // Left and right diagonal capture moves
        for (int[] attack : attackDirections) {
            int attackRow = myPosition.getRow() + attack[0];
            int attackCol = myPosition.getColumn() + attack[1];

            if (attackRow >= 1 && attackRow <= 8 && attackCol >= 1 && attackCol <= 8) {
                ChessPosition attackPosition = new ChessPosition(attackRow, attackCol);
                ChessPiece pieceOnPosition = board.getPiece(attackPosition);

                if (pieceOnPosition != null && pieceOnPosition.getTeamColor() != this.getTeamColor()) {
                    if (attackRow == 1 || attackRow == 8) { // Promotion on capture
                        pawnMoves.add(new ChessMove(myPosition, attackPosition, PieceType.QUEEN));
                        pawnMoves.add(new ChessMove(myPosition, attackPosition, PieceType.ROOK));
                        pawnMoves.add(new ChessMove(myPosition, attackPosition, PieceType.BISHOP));
                        pawnMoves.add(new ChessMove(myPosition, attackPosition, PieceType.KNIGHT));
                    } else {
                        pawnMoves.add(new ChessMove(myPosition, attackPosition, null));
                    }
                }
            }
        }

        return pawnMoves;
    }



    /**THE RECOMMENDED IntelliJ AUTOFILLED OVERRIDE STATEMENTS FOR toString, hashCode, AND EQUALS**/
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
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
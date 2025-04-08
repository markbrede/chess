package chess;

import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {
    private ChessPosition startPosition;
    private ChessPosition endPosition;
    private ChessPiece.PieceType promoPiece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promoPiece = promotionPiece;

    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promoPiece;
    }


    //TOSTRING METHOD
    @Override
    public String toString() {
        return String.format("(%d,%d) -> (%d,%d)%s",
                startPosition.getRow(), startPosition.getColumn(),
                endPosition.getRow(), endPosition.getColumn(),
                promoPiece != null ? " promotion: " + promoPiece : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessMove chessMove = (ChessMove) o;

        boolean isStartPositionEqual = Objects.equals(startPosition, chessMove.startPosition);
        boolean isEndPositionEqual = Objects.equals(endPosition, chessMove.endPosition);
        boolean isPromoPieceEqual = promoPiece == chessMove.promoPiece;

        return isStartPositionEqual && isEndPositionEqual && isPromoPieceEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPosition, endPosition, promoPiece);
    }
}

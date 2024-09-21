package chess;

import java.util.Objects;
/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    //final to ensure that row and column do not change.
    private final int row;
    private final int col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn(){
        return col;
    }
//compare the ChessPos obj for equality
    @Override
    public boolean equals(Object ob) {
        if (this == ob) return true;
        if (ob == null || getClass() != ob.getClass()) return false;
        ChessPosition that = (ChessPosition) ob; //cast the obj to chess pos
        return row == that.row && col == that.col; //compare row and col val
    }

    @Override //normally I'd do this manually
// but AI explained that I can be more concise by importing Objects.
// I'll be trying Objects.hash. Come back to this if I run into a related issue.

    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {   //get the chess specific representation for easier testing.
        return "" + (char) (col + 96) + row;
    }
}

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
// VALID MOVES METHOD
//when a piece is at its relative position more specifically.
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        //piece is going to be the startPosition
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) return null;

        //refer to pieceMoves for the moves I coded in phase 0. Get the piece, put in a array list ect...
        Collection<ChessMove> movePossibilities = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        //use the copied board and run the move to see if it is good to go.
        for (ChessMove move : movePossibilities) {
            ChessBoard tempBoard = copyOfCurrentBoard();

            tempBoard.addPiece(move.getEndPosition(), piece); //move the piece to the new spot
            tempBoard.addPiece(startPosition, null); //remove it from original pos. This better fix my issue... >:(

            //it would be great if my kingChecked method work but ofc it has been 2 hours and it does not
            //in theory, if not in check, then .add it to valid moves
            if (!kingChecked(piece.getTeamColor(), tempBoard)) {
                validMoves.add(move);
            }
        }
        return validMoves; // Return all valid moves
    }


// FINALLY! I am an idiot and assumed I understood what qualifies as "check".
//Admittedly, I caved and asked GTP what I was doing wrong. Because of the assistance, I have thoroughly
//studied the fix, typed the code out by myself, and have added detailed notes to demonstrate my understanding.
//if this raises concern, I would be very happy to discuss the why of the code in person or code an alternative method if necessary.

//is the teams king in check? I will make a data structure that will give the boolean answer.
    private boolean kingChecked(ChessGame.TeamColor teamColor, ChessBoard tempBoard) {
        ChessPosition kingPos = null;

        //in the 8 x 8 matrix, loop through getting the positions info and the piece
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition posInfo = new ChessPosition(row, col);
                ChessPiece piece = tempBoard.getPiece(posInfo);
                //if there is a piece there, get the team color and check if it is king
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPos = posInfo; //kings position is the posInfo which is now set to kingPos
                    break;
                }
            }
        }

        //for test that tests for a board without a king, return false.
        if (kingPos == null) return false;

        //in the 8 x 8 matrix, loop through getting the positions info and the piece
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition posInfoOppT = new ChessPosition(row, col);
                ChessPiece piece = tempBoard.getPiece(posInfoOppT);

                //if there is a piece and the piece is not your teams color... moves = possible for more that piece
                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> moves = piece.pieceMoves(tempBoard, posInfoOppT);

                    //now, using what I got from kingPos and moves, return true if the end position will take out the king
                    //AKA is the king in check. Otherwise the method will return false.
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





    //CREATES COPY OF THE BOARD
    //iterate with for loop
    /**I am going to have this code create a copy of the board so that valid moves can be verified without changing
    the actual board in the game**/
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
        //piece is the piece at the start position
        ChessPiece piece = board.getPiece(move.getStartPosition());

        //if there is no piece at the start position or it's not the current teams turn, throw an exception
        if (piece == null || piece.getTeamColor() != correctColorsTurn) {
            throw new InvalidMoveException();
        }

        //using validMoves, this will get the okay moves for the piece at the start position
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());

        //if there are no valid moves o r the given move is not in the list, throw the invalid moves exception
        if (validMoves == null || !validMoves.contains(move)) {
            throw new InvalidMoveException();
        }

        //resuing the code from validMove to move the piece to the new position
        //this time with board instead of the temp board
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);

        //using my getPromoPiece from ChessMove
        //if the move is a promotion, replace the piece at the end position with the promoted piece
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

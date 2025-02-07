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



    /**KING**/
    private ArrayList<ChessMove> getKingMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> KingMoves = new ArrayList<>();

        //Any direction one time
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
        //check positions with what I learned about enhanced for loops
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



    /**ROOK**/    //similar process to what I did for the KING
    /**TA NOTE! I know we got these to work but come back to the continuous
     * pieces and consider what we talked about (Concise purposeful code)*/
    private ArrayList<ChessMove> getRookMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> RookMoves = new ArrayList<>();
        //moves to any valid potion front-back and side-side
        //up 1-8 squares only for all directions
        ChessPosition[] upPositions = {
                new ChessPosition(myPosition.getRow()+1, myPosition.getColumn()),
                new ChessPosition(myPosition.getRow()+2, myPosition.getColumn()),
                new ChessPosition(myPosition.getRow()+3, myPosition.getColumn()),
                new ChessPosition(myPosition.getRow()+4, myPosition.getColumn()),
                new ChessPosition(myPosition.getRow()+5, myPosition.getColumn()),
                new ChessPosition(myPosition.getRow()+6, myPosition.getColumn()),
                new ChessPosition(myPosition.getRow()+7, myPosition.getColumn())
        };

        //down
        ChessPosition[] downPositions = {
                new ChessPosition(myPosition.getRow()-1, myPosition.getColumn()),
                new ChessPosition(myPosition.getRow()-2, myPosition.getColumn()),
                new ChessPosition(myPosition.getRow()-3, myPosition.getColumn()),
                new ChessPosition(myPosition.getRow()-4, myPosition.getColumn()),
                new ChessPosition(myPosition.getRow()-5, myPosition.getColumn()),
                new ChessPosition(myPosition.getRow()-6, myPosition.getColumn()),
                new ChessPosition(myPosition.getRow()-7, myPosition.getColumn())
        };

        //right
        ChessPosition[] rightPositions = {
                new ChessPosition(myPosition.getRow(), myPosition.getColumn()+1),
                new ChessPosition(myPosition.getRow(), myPosition.getColumn()+2),
                new ChessPosition(myPosition.getRow(), myPosition.getColumn()+3),
                new ChessPosition(myPosition.getRow(), myPosition.getColumn()+4),
                new ChessPosition(myPosition.getRow(), myPosition.getColumn()+5),
                new ChessPosition(myPosition.getRow(), myPosition.getColumn()+6),
                new ChessPosition(myPosition.getRow(), myPosition.getColumn()+7)
        };

        //left
        ChessPosition[] leftPositions = {
                new ChessPosition(myPosition.getRow(), myPosition.getColumn()-1),
                new ChessPosition(myPosition.getRow(), myPosition.getColumn()-2),
                new ChessPosition(myPosition.getRow(), myPosition.getColumn()-3),
                new ChessPosition(myPosition.getRow(), myPosition.getColumn()-4),
                new ChessPosition(myPosition.getRow(), myPosition.getColumn()-5),
                new ChessPosition(myPosition.getRow(), myPosition.getColumn()-6),
                new ChessPosition(myPosition.getRow(), myPosition.getColumn()-7)
        };

        //find all rook positions on the 8x8 matrix aka the board
        for (ChessPosition[] travel : new ChessPosition[][]{upPositions, downPositions, rightPositions, leftPositions}) {
            for (ChessPosition newPosition : travel) {
                if (newPosition.getRow() >= 1 && newPosition.getRow() <= 8 &&
                        newPosition.getColumn() >= 1 && newPosition.getColumn() <= 8) {

                    ChessPiece pieceOnPosition = board.getPiece(newPosition);
                        //if the pieceOnPosition is null add position
                    if (pieceOnPosition == null) {
                        RookMoves.add(new ChessMove(myPosition, newPosition, null));
                    } else {
                            //if null is not found identify that the position has the other teams piece
                        if (pieceOnPosition.getTeamColor() != this.getTeamColor()) {
                            RookMoves.add(new ChessMove(myPosition, newPosition, null));
                        }
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        return RookMoves;
    }



    /**BISHOP**/
    private ArrayList<ChessMove> getBishopMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> BishopMoves = new ArrayList<>();

        //like the rook there should be four directional arrays except this time I need to make them for diagonals
        //up and to the right diagonal
        ChessPosition[] upRightPositions = {
                new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1),
                new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn() + 2),
                new ChessPosition(myPosition.getRow() + 3, myPosition.getColumn() + 3),
                new ChessPosition(myPosition.getRow() + 4, myPosition.getColumn() + 4),
                new ChessPosition(myPosition.getRow() + 5, myPosition.getColumn() + 5),
                new ChessPosition(myPosition.getRow() + 6, myPosition.getColumn() + 6),
                new ChessPosition(myPosition.getRow() + 7, myPosition.getColumn() + 7)
        };

        //up and to the left diagonal
        ChessPosition[] upLeftPositions = {
                new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1),
                new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn() - 2),
                new ChessPosition(myPosition.getRow() + 3, myPosition.getColumn() - 3),
                new ChessPosition(myPosition.getRow() + 4, myPosition.getColumn() - 4),
                new ChessPosition(myPosition.getRow() + 5, myPosition.getColumn() - 5),
                new ChessPosition(myPosition.getRow() + 6, myPosition.getColumn() - 6),
                new ChessPosition(myPosition.getRow() + 7, myPosition.getColumn() - 7)
        };

        //down and to the right diagonal
        ChessPosition[] downRightPositions = {
                new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1),
                new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn() + 2),
                new ChessPosition(myPosition.getRow() - 3, myPosition.getColumn() + 3),
                new ChessPosition(myPosition.getRow() - 4, myPosition.getColumn() + 4),
                new ChessPosition(myPosition.getRow() - 5, myPosition.getColumn() + 5),
                new ChessPosition(myPosition.getRow() - 6, myPosition.getColumn() + 6),
                new ChessPosition(myPosition.getRow() - 7, myPosition.getColumn() + 7)
        };

        //down and to the left diagonal
        ChessPosition[] downLeftPositions = {
                new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1),
                new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn() - 2),
                new ChessPosition(myPosition.getRow() - 3, myPosition.getColumn() - 3),
                new ChessPosition(myPosition.getRow() - 4, myPosition.getColumn() - 4),
                new ChessPosition(myPosition.getRow() - 5, myPosition.getColumn() - 5),
                new ChessPosition(myPosition.getRow() - 6, myPosition.getColumn() - 6),
                new ChessPosition(myPosition.getRow() - 7, myPosition.getColumn() - 7)
        };

        //separately analyze each direction
        for (ChessPosition[] travel : new ChessPosition[][]{upRightPositions, upLeftPositions, downRightPositions, downLeftPositions}) {
            for (ChessPosition newPosition : travel) {
                //find all bishop positions on the 8x8 matrix aka the board
                if (newPosition.getRow() >= 1 && newPosition.getRow() <= 8 &&
                        newPosition.getColumn() >= 1 && newPosition.getColumn() <= 8) {
                    //if the pieceOnPosition is null add position
                    ChessPiece pieceOnPosition = board.getPiece(newPosition);
                    if (pieceOnPosition == null) {
                        BishopMoves.add(new ChessMove(myPosition, newPosition, null));
                    } else {
                        //if null is not found identify that the position has the other teams piece
                        if (pieceOnPosition.getTeamColor() != this.getTeamColor()) {
                            BishopMoves.add(new ChessMove(myPosition, newPosition, null));
                        }
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        return BishopMoves;
    }



    /**QUEEN**/
    private ArrayList<ChessMove> getQueenMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> queenMoves = new ArrayList<>();

        //combining my rook and bishop moves for full implementation of the queen
        queenMoves.addAll(getBishopMoves(board, myPosition)); // Diagonal moves
        queenMoves.addAll(getRookMoves(board, myPosition));   // Straight moves

        return queenMoves;
    }



    /**KNIGHT**/
    private ArrayList<ChessMove> getKnightMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> knightMoves = new ArrayList<>();

        //the L shape for knight moves is going to need to be a 2, 1 pattern
        ChessPosition[] positions = {
                new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn() + 1),  // up 2, right 1
                new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn() - 1),  // up 2, left 1
                new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn() + 1),  // down 2, right 1
                new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn() - 1),  // down 2, left 1
                new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 2),  // up 1, right 2
                new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 2),  // up 1, left 2
                new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 2),  // down 1, right 2
                new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 2)   // down 1, left 2
        };

        //check positions with what I learned about enhanced for loops
        for (ChessPosition newPosition : positions) {
            //ensure the move is within the 1 8 matrix
            if (newPosition.getRow() >= 1 && newPosition.getRow() <= 8 &&
                    newPosition.getColumn() >= 1 && newPosition.getColumn() <= 8) {

                ChessPiece pieceOnPosition = board.getPiece(newPosition);

                //take the now validated pieceOnPosition and move if the square is empty or contains enemy piece
                if (pieceOnPosition == null ||
                        pieceOnPosition.getTeamColor() != this.getTeamColor()) {
                    knightMoves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }

        return knightMoves;
    }



    /**PAWN**/
    private ArrayList<ChessMove> getPawnMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> pawnMoves = new ArrayList<>();

        //pawn moves up or down board based on the team color
        int teamdirection = (pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;

        //positions that move forward
        ChessPosition[] forwardPawn;

        //determine if pawn is in start position
        //2 for white team pawns and 7 for black team pawns
        if ((pieceColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2) ||
                (pieceColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7)) {
            //if pawn is in start position, allow it to move 2 squares
            forwardPawn = new ChessPosition[]{
                    new ChessPosition(myPosition.getRow() + teamdirection, myPosition.getColumn()),      // One square forward
                    new ChessPosition(myPosition.getRow() + (2 * teamdirection), myPosition.getColumn()) // Two squares forward
            };
        } else {
            //prevent pawn from moving more than two squares when out of start position
            forwardPawn = new ChessPosition[]{
                    new ChessPosition(myPosition.getRow() + teamdirection, myPosition.getColumn())
            };
        }

        //diagonally forward are the position a pawn can KO the opposing team pieces.
        ChessPosition[] KOPositions = {
                new ChessPosition(myPosition.getRow() + teamdirection, myPosition.getColumn() - 1), // knock out left
                new ChessPosition(myPosition.getRow() + teamdirection, myPosition.getColumn() + 1)  // knocj out right
        };

        //check positions with what I learned about enhanced for loops
        for (ChessPosition newPosition : forwardPawn) {
            //ensure the move is within the 1 8 matrix
            if (newPosition.getRow() >= 1 && newPosition.getRow() <= 8 &&
                    newPosition.getColumn() >= 1 && newPosition.getColumn() <= 8) {

                ChessPiece pieceOnPosition = board.getPiece(newPosition);
                //spot needs to be empty for pawn to move forward
                if (pieceOnPosition == null) {
                    //if pawn reaches promotion
                    if (newPosition.getRow() == 1 || newPosition.getRow() == 8) {
                        promoPawn(pawnMoves, myPosition, newPosition);
                    } else {
                        //else normal move forward
                        pawnMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                } else {
                    break;
                }
            }
        }

        //KO moves
        for (ChessPosition newPosition : KOPositions) {
            //ensure the move is within the 1 8 matrix
            if (newPosition.getRow() >= 1 && newPosition.getRow() <= 8 &&
                    newPosition.getColumn() >= 1 && newPosition.getColumn() <= 8) {

                ChessPiece pieceOnPosition = board.getPiece(newPosition);
                //able to knock out opposing team piece if present
                if (pieceOnPosition != null && pieceOnPosition.getTeamColor() != this.pieceColor) {
                    //is the knocked out piece a position for a promotion
                    if (newPosition.getRow() == 1 || newPosition.getRow() == 8) {
                        // Add all possible promotion moves
                        promoPawn(pawnMoves, myPosition, newPosition);
                    } else {
                        pawnMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
        }

        return pawnMoves;
    }

    //array list of all possible promo
    //used when a pawn reaches promo through forward movement
    //used when a pawn reaches promo through KO
    private void promoPawn(ArrayList<ChessMove> moves, ChessPosition start, ChessPosition end) {
        //.add all of my previous pieces
        moves.add(new ChessMove(start, end, PieceType.QUEEN));
        moves.add(new ChessMove(start, end, PieceType.ROOK));
        moves.add(new ChessMove(start, end, PieceType.BISHOP));
        moves.add(new ChessMove(start, end, PieceType.KNIGHT));
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
package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

public class ChessBoardUI {

    public void drawBoard(boolean isWhitePerspective, ChessGame game) {
        clearScreen();
        System.out.println("Chess Game");
        System.out.println();

        drawColumnLabels(isWhitePerspective);

        //needs to draw board/ board rows
        for (int row = 8; row >= 1; row--) {
            int displayRow = isWhitePerspective ? row : 9 - row;

            drawRowLabel(displayRow);

            for (int col = 1; col <= 8; col++) {
                int displayCol = isWhitePerspective ? col : 9 - col;

                //determine square color... row+col is even = white squares
                boolean isLightSquare = (displayRow + displayCol) % 2 == 0;

                //set the background color
                if (isLightSquare) {
                    System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
                } else {
                    System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY);
                }

                //at this position, get a piece.
                ChessPosition position = new ChessPosition(displayRow, displayCol);
                ChessPiece piece = game.getBoard().getPiece(position);

                //draw a piece
                if (piece == null) {
                    System.out.print(EscapeSequences.EMPTY);
                } else {
                    drawPiece(piece); //else empty square
                }
            }

            //bg reset
            System.out.print(EscapeSequences.RESET_BG_COLOR);
            drawRowLabel(displayRow); // after back color reset, add row label at end
            System.out.println();
        }

        //should draw column labels
        drawColumnLabels(isWhitePerspective);

        // Reset all formatting
        System.out.print(EscapeSequences.RESET_BG_COLOR);
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
    }

    private void clearScreen() {
        System.out.print(EscapeSequences.ERASE_SCREEN);
    }

    private void drawColumnLabels(boolean isWhitePerspective) {
        System.out.print("   ");
        for (int col = 1; col <= 8; col++) {
            char label;
            if (isWhitePerspective) {
                label = (char) ('a' + col - 1);
            } else {
                label = (char) ('h' - col + 1);
            }
            System.out.print(" " + label + " ");
        }
        System.out.println();
    }

    private void drawRowLabel(int row) {
        System.out.print(" " + row + " ");
    }

    //needs to draw the piece
    private void drawPiece(ChessPiece piece) {
        boolean isWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;

        //if piece is white, I want the txt white, else black txt
        if (isWhite) {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
        } else {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
        }

        //set up using the specs tips for unicode
        switch (piece.getPieceType()) {
            case KING:
                System.out.print(isWhite ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING);
                break;
            case QUEEN:
                System.out.print(isWhite ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN);
                break;
            case BISHOP:
                System.out.print(isWhite ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP);
                break;
            case KNIGHT:
                System.out.print(isWhite ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT);
                break;
            case ROOK:
                System.out.print(isWhite ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK);
                break;
            case PAWN:
                System.out.print(isWhite ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN);
                break;
        }
    }
}


package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

public class ChessBoardUI {

    public void drawBoard(boolean isWhitePerspective, ChessGame game) {
        clearScreen();
        System.out.println("Chess Game");
        System.out.println();

        // Top column labels
        System.out.print(EscapeSequences.SET_BG_COLOR_BLUE);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        drawColumnLabels(isWhitePerspective);

        int start = isWhitePerspective ? 8 : 1;
        int end = isWhitePerspective ? 0 : 9;
        int step = isWhitePerspective ? -1 : 1;

        for (int row = start; row != end; row += step) {
            // Row label (left)
            System.out.print(EscapeSequences.SET_BG_COLOR_BLUE);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
            drawRowLabel(row);

            for (int col = 1; col <= 8; col++) {
                int displayCol = isWhitePerspective ? col : 9 - col;

                boolean isLightSquare = (row + displayCol) % 2 != 0;

                if (isLightSquare) {
                    System.out.print(EscapeSequences.SET_BG_COLOR_YELLOW);  // light square
                } else {
                    System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREEN);  // dark square
                }

                ChessPosition position = new ChessPosition(row, displayCol);
                ChessPiece piece = game.getBoard().getPiece(position);

                if (piece == null) {
                    System.out.print(EscapeSequences.EMPTY);
                } else {
                    drawPiece(piece);  // sets its own text color
                }
            }

            // Reset & draw row label (right)
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);
            System.out.print(EscapeSequences.SET_BG_COLOR_BLUE);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
            drawRowLabel(row);
            System.out.println();
        }

        // Bottom column labels
        System.out.print(EscapeSequences.SET_BG_COLOR_BLUE);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
        drawColumnLabels(isWhitePerspective);

        // Final formatting reset
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


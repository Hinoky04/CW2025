package com.comp2042.helpers;

import javafx.beans.property.BooleanProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

/**
 * Helper class for danger zone logic extracted from GuiController.
 * Contains exact same code, just moved to separate file.
 */
public class GuiDangerHelper {
    
    private static final int HIDDEN_TOP_ROWS = 3;
    
    private final int dangerVisibleRows;
    private final Text dangerText;
    private final BorderPane gameBoard;
    private final BooleanProperty isDanger;
    
    /**
     * Creates a new danger helper for managing danger zone warnings.
     *
     * @param dangerVisibleRows the number of visible rows considered danger zone
     * @param dangerText the text component for danger warning
     * @param gameBoard the game board border pane for styling
     * @param isDanger the boolean property for danger state
     */
    public GuiDangerHelper(
            int dangerVisibleRows,
            Text dangerText,
            BorderPane gameBoard,
            BooleanProperty isDanger) {
        this.dangerVisibleRows = dangerVisibleRows;
        this.dangerText = dangerText;
        this.gameBoard = gameBoard;
        this.isDanger = isDanger;
    }
    
    /**
     * Updates the danger state based on the board matrix.
     * Checks if any blocks are in the danger zone (top visible rows).
     *
     * @param board the board matrix to check
     */
    public void updateDangerFromBoard(int[][] board) {
        boolean found = false;

        int visibleRows = board.length - HIDDEN_TOP_ROWS;
        int limit = Math.min(dangerVisibleRows, visibleRows);

        for (int row = HIDDEN_TOP_ROWS; row < HIDDEN_TOP_ROWS + limit; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] != 0) {
                    found = true;
                    break;
                }
            }
            if (found) {
                break;
            }
        }

        setDanger(found);
    }

    /**
     * Sets the danger state and updates UI accordingly.
     *
     * @param value true if in danger zone, false otherwise
     */
    public void setDanger(boolean value) {
        if (isDanger.get() == value) {
            return;
        }
        isDanger.set(value);

        if (dangerText != null) {
            dangerText.setVisible(value);
        }
        if (gameBoard != null) {
            if (value) {
                if (!gameBoard.getStyleClass().contains("dangerBoard")) {
                    gameBoard.getStyleClass().add("dangerBoard");
                }
            } else {
                gameBoard.getStyleClass().remove("dangerBoard");
            }
        }
    }
}


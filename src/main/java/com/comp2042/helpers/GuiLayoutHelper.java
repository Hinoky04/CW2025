package com.comp2042.helpers;

import javafx.geometry.Bounds;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import com.comp2042.models.ViewData;

/**
 * Helper class for layout calibration logic extracted from GuiController.
 * Contains exact same code, just moved to separate file.
 */
public class GuiLayoutHelper {
    
    private static final int HIDDEN_TOP_ROWS = 3;
    private static final int BRICK_SIZE = 30;
    
    private boolean boardLayoutCalibrated = false;
    private double boardOriginX;
    private double boardOriginY;
    private double boardCellWidth;
    private double boardCellHeight;
    
    private final GridPane gamePanel;
    private final GridPane brickPanel;
    private final GridPane ghostPanel;
    private final java.util.function.Supplier<Rectangle[][]> displayMatrixSupplier;
    
    /**
     * Creates a new layout helper for board alignment.
     *
     * @param gamePanel the main game board panel
     * @param brickPanel the panel for the falling brick
     * @param ghostPanel the panel for the ghost piece
     * @param displayMatrixSupplier supplier to get the display matrix for calibration
     */
    public GuiLayoutHelper(
            GridPane gamePanel,
            GridPane brickPanel,
            GridPane ghostPanel,
            java.util.function.Supplier<Rectangle[][]> displayMatrixSupplier) {
        this.gamePanel = gamePanel;
        this.brickPanel = brickPanel;
        this.ghostPanel = ghostPanel;
        this.displayMatrixSupplier = displayMatrixSupplier;
    }
    
    /**
     * Sets the display matrix supplier (note: requires recreating helper for full effect).
     *
     * @param displayMatrixSupplier supplier to get the display matrix
     */
    public void setDisplayMatrixSupplier(java.util.function.Supplier<Rectangle[][]> displayMatrixSupplier) {
        // Note: This requires recreating the helper, but keeping interface simple
    }
    
    /**
     * Checks if the board layout has been calibrated.
     *
     * @return true if calibrated, false otherwise
     */
    public boolean isCalibrated() {
        return boardLayoutCalibrated;
    }
    
    /**
     * Resets the calibration state.
     */
    public void reset() {
        boardLayoutCalibrated = false;
    }
    
    /**
     * Compute the origin and cell size of the main board once, so the
     * falling brick layer can be aligned without "jitter".
     */
    public void calibrateBoardLayout() {
        if (boardLayoutCalibrated || gamePanel == null || brickPanel == null) {
            return;
        }

        Rectangle[][] displayMatrix = displayMatrixSupplier.get();
        if (displayMatrix == null
                || displayMatrix.length <= HIDDEN_TOP_ROWS
                || displayMatrix[HIDDEN_TOP_ROWS].length < 2
                || displayMatrix[HIDDEN_TOP_ROWS][0] == null
                || displayMatrix[HIDDEN_TOP_ROWS][1] == null) {
            return;
        }

        // Make sure CSS and layout are applied
        gamePanel.applyCss();
        gamePanel.layout();

        Rectangle originCell = displayMatrix[HIDDEN_TOP_ROWS][0];
        Rectangle nextCellInRow = displayMatrix[HIDDEN_TOP_ROWS][1];

        Bounds originScene = originCell.localToScene(originCell.getBoundsInLocal());
        Bounds originInParent = brickPanel.getParent().sceneToLocal(originScene);

        Bounds nextScene = nextCellInRow.localToScene(nextCellInRow.getBoundsInLocal());
        Bounds nextInParent = brickPanel.getParent().sceneToLocal(nextScene);

        double originX = originInParent.getMinX();
        double originY = originInParent.getMinY();

        double cellWidth = nextInParent.getMinX() - originX;

        double cellHeight;
        if (displayMatrix.length > HIDDEN_TOP_ROWS + 1
                && displayMatrix[HIDDEN_TOP_ROWS + 1][0] != null) {
            Rectangle belowCell = displayMatrix[HIDDEN_TOP_ROWS + 1][0];
            Bounds belowScene = belowCell.localToScene(belowCell.getBoundsInLocal());
            Bounds belowInParent = brickPanel.getParent().sceneToLocal(belowScene);
            cellHeight = belowInParent.getMinY() - originY;
        } else {
            cellHeight = BRICK_SIZE + gamePanel.getVgap();
        }

        boardOriginX = originX;
        boardOriginY = originY;
        boardCellWidth = cellWidth;
        boardCellHeight = cellHeight;

        boardLayoutCalibrated = true;

        // Now it's safe to show the falling-brick overlay and ghost overlay
        brickPanel.setVisible(true);
        if (ghostPanel != null) {
            ghostPanel.setVisible(true);
        }
    }

    /**
     * Moves the brickPanel so that the falling piece lines up exactly with
     * the background grid inside gamePanel.
     *
     * @param brick the view data containing brick position
     */
    public void updateBrickPanelPosition(ViewData brick) {
        if (!boardLayoutCalibrated || brickPanel == null || brick == null) {
            return;
        }

        double x = boardOriginX + brick.getxPosition() * boardCellWidth;
        double y = boardOriginY + (brick.getyPosition() - HIDDEN_TOP_ROWS) * boardCellHeight;

        brickPanel.setLayoutX(Math.round(x));
        brickPanel.setLayoutY(Math.round(y));
    }

    /**
     * Moves the ghostPanel so that the shadow piece lines up exactly with
     * the background grid at the landing position.
     *
     * @param brick the view data containing ghost position
     */
    public void updateGhostPanelPosition(ViewData brick) {
        if (!boardLayoutCalibrated || ghostPanel == null || brick == null) {
            return;
        }

        double x = boardOriginX + brick.getGhostXPosition() * boardCellWidth;
        double y = boardOriginY + (brick.getGhostYPosition() - HIDDEN_TOP_ROWS) * boardCellHeight;

        ghostPanel.setLayoutX(Math.round(x));
        ghostPanel.setLayoutY(Math.round(y));
    }
}


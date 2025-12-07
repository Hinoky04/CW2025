package com.comp2042.ui;

import javafx.geometry.Bounds;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import com.comp2042.models.ViewData;

/**
 * Handles board layout calibration to keep bricks perfectly aligned.
 * Prevents visual "shaking" by calculating precise cell positions.
 */
public class LayoutCalibrator {
    
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
    private final Rectangle[][] displayMatrix;
    
    public LayoutCalibrator(GridPane gamePanel, GridPane brickPanel, 
                           GridPane ghostPanel, Rectangle[][] displayMatrix) {
        this.gamePanel = gamePanel;
        this.brickPanel = brickPanel;
        this.ghostPanel = ghostPanel;
        this.displayMatrix = displayMatrix;
    }
    
    /**
     * Compute the origin and cell size of the main board once,
     * so the falling brick layer can be aligned without "jitter".
     */
    public void calibrateBoardLayout() {
        if (boardLayoutCalibrated || gamePanel == null || brickPanel == null) {
            return;
        }
        
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
    
    public boolean isCalibrated() {
        return boardLayoutCalibrated;
    }
    
    public void reset() {
        boardLayoutCalibrated = false;
    }
}


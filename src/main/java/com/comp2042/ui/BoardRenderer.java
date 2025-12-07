package com.comp2042.ui;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import com.comp2042.models.GameState;
import com.comp2042.models.ViewData;

/**
 * Handles all rendering logic for the game board including background, falling brick, ghost piece, and previews.
 */
public class BoardRenderer {
    
    private static final int BRICK_SIZE = 30;
    private static final int NEXT_BRICK_SIZE = 24;
    private static final int HIDDEN_TOP_ROWS = 3;
    private static final int PREVIEW_OFFSET_ROW = 0;
    private static final int PREVIEW_OFFSET_COL = 0;
    
    private final GridPane gamePanel;
    private final GridPane brickPanel;
    private final GridPane ghostPanel;
    private final GridPane holdBrickPanel;
    private final GridPane nextBrickPanelTop;
    private final GridPane nextBrickPanelMid;
    private final GridPane nextBrickPanelBottom;
    
    private final ColorManager colorManager;
    private final LayoutCalibrator layoutCalibrator;
    
    // Background cells (for the board)
    private Rectangle[][] displayMatrix;
    
    // Current falling piece cells
    private Rectangle[][] rectangles;
    
    // Shadow/ghost piece cells (landing position preview)
    private Rectangle[][] ghostRectangles;
    
    // NEXT preview cells (for 3 upcoming bricks)
    private Rectangle[][] nextBrickRectanglesTop;
    private Rectangle[][] nextBrickRectanglesMid;
    private Rectangle[][] nextBrickRectanglesBottom;
    
    // HOLD preview cells
    private Rectangle[][] holdBrickRectangles;
    
    // Last ViewData snapshot describing the current falling brick
    private ViewData lastViewData;
    
    private GameState gameState;
    
    public BoardRenderer(GridPane gamePanel, GridPane brickPanel, GridPane ghostPanel,
                         GridPane holdBrickPanel, GridPane nextBrickPanelTop,
                         GridPane nextBrickPanelMid, GridPane nextBrickPanelBottom,
                         ColorManager colorManager, LayoutCalibrator layoutCalibrator) {
        this.gamePanel = gamePanel;
        this.brickPanel = brickPanel;
        this.ghostPanel = ghostPanel;
        this.holdBrickPanel = holdBrickPanel;
        this.nextBrickPanelTop = nextBrickPanelTop;
        this.nextBrickPanelMid = nextBrickPanelMid;
        this.nextBrickPanelBottom = nextBrickPanelBottom;
        this.colorManager = colorManager;
        this.layoutCalibrator = layoutCalibrator;
    }
    
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
    
    /**
     * Initialise background cells: from the very beginning, use the final
     * rendering style (no "blur then become solid" jump).
     */
    public void initBackgroundCells(int[][] boardMatrix) {
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];
        
        for (int row = HIDDEN_TOP_ROWS; row < boardMatrix.length; row++) {
            for (int col = 0; col < boardMatrix[row].length; col++) {
                Rectangle cell = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                
                // Use the same rendering path as runtime refresh:
                setBackgroundRectangleData(boardMatrix[row][col], cell);
                
                // Grey grid line on top of the fill.
                cell.setStroke(Color.rgb(55, 55, 55));
                cell.setStrokeWidth(0.7);
                
                displayMatrix[row][col] = cell;
                gamePanel.add(cell, col, row - HIDDEN_TOP_ROWS);
            }
        }
    }
    
    public void initFallingBrick(ViewData brick) {
        int[][] brickData = brick.getBrickData();
        rectangles = new Rectangle[brickData.length][brickData[0].length];
        for (int row = 0; row < brickData.length; row++) {
            for (int col = 0; col < brickData[row].length; col++) {
                Rectangle cell = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                setRectangleData(brickData[row][col], cell);
                rectangles[row][col] = cell;
                brickPanel.add(cell, col, row);
            }
        }
    }
    
    /**
     * Initialize the ghost/shadow piece overlay.
     * Creates rectangles for the shadow block at the landing position.
     */
    public void initGhost(ViewData brick) {
        if (ghostPanel == null) {
            return;
        }
        int[][] brickData = brick.getBrickData();
        ghostRectangles = new Rectangle[brickData.length][brickData[0].length];
        for (int row = 0; row < brickData.length; row++) {
            for (int col = 0; col < brickData[row].length; col++) {
                Rectangle cell = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                setGhostRectangleData(brickData[row][col], cell);
                ghostRectangles[row][col] = cell;
                ghostPanel.add(cell, col, row);
            }
        }
    }
    
    public void initNextBrick(ViewData brick) {
        int[][][] queue = brick.getNextQueue();
        
        if ((queue == null || queue.length == 0) && brick.getNextBrickData() != null) {
            queue = new int[][][]{brick.getNextBrickData()};
        }
        if (queue == null || queue.length == 0) {
            clearNextPanels();
            return;
        }
        
        nextBrickRectanglesTop = buildCenteredPreview(
                nextBrickPanelTop,
                queue.length > 0 ? queue[0] : null
        );
        nextBrickRectanglesMid = buildCenteredPreview(
                nextBrickPanelMid,
                queue.length > 1 ? queue[1] : null
        );
        nextBrickRectanglesBottom = buildCenteredPreview(
                nextBrickPanelBottom,
                queue.length > 2 ? queue[2] : null
        );
    }
    
    /**
     * Draws a brick shape into the given panel, centered inside a fixed 4x4
     * preview grid, so all pieces look nicely centered.
     */
    private Rectangle[][] buildCenteredPreview(GridPane panel, int[][] data) {
        if (panel == null) {
            return null;
        }
        panel.getChildren().clear();
        
        if (data == null || data.length == 0 || data[0].length == 0) {
            return null;
        }
        
        int rows = data.length;
        int cols = data[0].length;
        
        // bounding box of non-zero cells
        int minRow = rows, maxRow = -1, minCol = cols, maxCol = -1;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (data[r][c] != 0) {
                    if (r < minRow) minRow = r;
                    if (r > maxRow) maxRow = r;
                    if (c < minCol) minCol = c;
                    if (c > maxCol) maxCol = c;
                }
            }
        }
        
        if (maxRow == -1) {
            return null;
        }
        
        int shapeRows = maxRow - minRow + 1;
        int shapeCols = maxCol - minCol + 1;
        
        final int targetRows = 4;
        final int targetCols = 4;
        
        int offsetRow = (targetRows - shapeRows) / 2 + PREVIEW_OFFSET_ROW;
        int offsetCol = (targetCols - shapeCols) / 2 + PREVIEW_OFFSET_COL;
        
        Rectangle[][] rects = new Rectangle[targetRows][targetCols];
        
        for (int r = 0; r < shapeRows; r++) {
            for (int c = 0; c < shapeCols; c++) {
                int value = data[minRow + r][minCol + c];
                if (value == 0) {
                    continue;
                }
                
                Rectangle cell = new Rectangle(NEXT_BRICK_SIZE, NEXT_BRICK_SIZE);
                setRectangleData(value, cell);
                
                int gridRow = offsetRow + r;
                int gridCol = offsetCol + c;
                
                if (gridRow >= 0 && gridRow < targetRows
                        && gridCol >= 0 && gridCol < targetCols) {
                    rects[gridRow][gridCol] = cell;
                    panel.add(cell, gridCol, gridRow);
                }
            }
        }
        
        return rects;
    }
    
    private void clearNextPanels() {
        if (nextBrickPanelTop != null) {
            nextBrickPanelTop.getChildren().clear();
        }
        if (nextBrickPanelMid != null) {
            nextBrickPanelMid.getChildren().clear();
        }
        if (nextBrickPanelBottom != null) {
            nextBrickPanelBottom.getChildren().clear();
        }
        nextBrickRectanglesTop = null;
        nextBrickRectanglesMid = null;
        nextBrickRectanglesBottom = null;
    }
    
    public void refreshNextBrick(ViewData brick) {
        int[][][] queue = brick.getNextQueue();
        
        if ((queue == null || queue.length == 0) && brick.getNextBrickData() != null) {
            queue = new int[][][]{brick.getNextBrickData()};
        }
        if (queue == null || queue.length == 0) {
            clearNextPanels();
            return;
        }
        
        nextBrickRectanglesTop = buildCenteredPreview(
                nextBrickPanelTop,
                queue.length > 0 ? queue[0] : null
        );
        nextBrickRectanglesMid = buildCenteredPreview(
                nextBrickPanelMid,
                queue.length > 1 ? queue[1] : null
        );
        nextBrickRectanglesBottom = buildCenteredPreview(
                nextBrickPanelBottom,
                queue.length > 2 ? queue[2] : null
        );
    }
    
    public void initHoldBrick(ViewData brick) {
        if (holdBrickPanel == null) {
            return;
        }
        int[][] holdData = brick.getHoldBrickData();
        
        holdBrickRectangles = buildCenteredPreview(holdBrickPanel, holdData);
    }
    
    public void refreshHoldBrick(ViewData brick) {
        if (holdBrickPanel == null) {
            return;
        }
        initHoldBrick(brick);
    }
    
    /**
     * Refresh the ghost/shadow piece to show where the current brick will land.
     * Updates both position and visual representation.
     */
    public void refreshGhost(ViewData brick) {
        if (ghostPanel == null || brick == null || gameState != GameState.PLAYING) {
            return;
        }
        
        // Initialize ghost rectangles if not already created
        if (ghostRectangles == null) {
            initGhost(brick);
        }
        
        // Update position to landing location
        layoutCalibrator.updateGhostPanelPosition(brick);
        
        // Update visual representation
        int[][] brickData = brick.getBrickData();
        if (ghostRectangles != null && ghostRectangles.length == brickData.length
                && ghostRectangles[0].length == brickData[0].length) {
            for (int row = 0; row < brickData.length; row++) {
                for (int col = 0; col < brickData[row].length; col++) {
                    if (ghostRectangles[row][col] != null) {
                        setGhostRectangleData(brickData[row][col], ghostRectangles[row][col]);
                    }
                }
            }
        } else {
            // If dimensions changed, reinitialize
            ghostPanel.getChildren().clear();
            initGhost(brick);
        }
    }
    
    /** Draw a single cell of the active falling layer (no background). */
    private void setRectangleData(int colorCode, Rectangle rectangle) {
        rectangle.setFill(colorManager.getActiveBrickFillColor(colorCode));
        rectangle.setArcHeight(9);
        rectangle.setArcWidth(9);
    }
    
    /** Draw a single cell of the background matrix. */
    private void setBackgroundRectangleData(int colorCode, Rectangle rectangle) {
        rectangle.setFill(colorManager.getBackgroundFillColor(colorCode));
        rectangle.setArcHeight(9);
        rectangle.setArcWidth(9);
    }
    
    /**
     * Draw a single cell of the ghost/shadow layer.
     * Uses semi-transparent fill with an outline to make it visually distinct.
     */
    private void setGhostRectangleData(int colorCode, Rectangle rectangle) {
        if (colorCode == 0) {
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setStroke(null);
        } else {
            rectangle.setFill(colorManager.getGhostFillColor(colorCode));
            // Add a subtle outline to make it more visible
            rectangle.setStroke(Color.WHITE);
            rectangle.setStrokeWidth(1.5);
            rectangle.setArcHeight(9);
            rectangle.setArcWidth(9);
        }
    }
    
    public void refreshBrick(ViewData brick) {
        if (gameState == GameState.PLAYING) {
            lastViewData = brick;
            
            layoutCalibrator.updateBrickPanelPosition(brick);
            
            int[][] brickData = brick.getBrickData();
            for (int row = 0; row < brickData.length; row++) {
                for (int col = 0; col < brickData[row].length; col++) {
                    setRectangleData(brickData[row][col], rectangles[row][col]);
                }
            }
            
            refreshNextBrick(brick);
            refreshHoldBrick(brick);
            refreshGhost(brick);
        }
    }
    
    public void refreshGameBackground(int[][] board) {
        for (int row = HIDDEN_TOP_ROWS; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                setBackgroundRectangleData(board[row][col], displayMatrix[row][col]);
            }
        }
        
        // After the background changes we must recompute and redraw the landing shadow.
        if (lastViewData != null) {
            refreshGhost(lastViewData);
        }
    }
    
    public void clearBrickPanel() {
        if (brickPanel != null) {
            brickPanel.getChildren().clear();
        }
    }
    
    public void clearGhostPanel() {
        if (ghostPanel != null) {
            ghostPanel.getChildren().clear();
            ghostRectangles = null;
        }
    }
    
    public Rectangle[][] getDisplayMatrix() {
        return displayMatrix;
    }
    
    public ViewData getLastViewData() {
        return lastViewData;
    }
    
    public void setLastViewData(ViewData lastViewData) {
        this.lastViewData = lastViewData;
    }
}


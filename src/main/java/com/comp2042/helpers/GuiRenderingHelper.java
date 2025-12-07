package com.comp2042.helpers;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import com.comp2042.models.ViewData;
import com.comp2042.models.GameState;

/**
 * Helper class for rendering logic extracted from GuiController.
 * Contains exact same code, just moved to separate file.
 */
public class GuiRenderingHelper {
    
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
    private final GuiColorHelper colorHelper;
    
    private Rectangle[][] displayMatrix;
    private Rectangle[][] rectangles;
    private Rectangle[][] ghostRectangles;
    private Rectangle[][] nextBrickRectanglesTop;
    private Rectangle[][] nextBrickRectanglesMid;
    private Rectangle[][] nextBrickRectanglesBottom;
    private Rectangle[][] holdBrickRectangles;
    private ViewData lastViewData;
    private GameState gameState;
    
    /**
     * Creates a new rendering helper with the specified UI panels and color helper.
     *
     * @param gamePanel the main game board panel
     * @param brickPanel the panel for the falling brick
     * @param ghostPanel the panel for the ghost/shadow piece
     * @param holdBrickPanel the panel for the held brick preview
     * @param nextBrickPanelTop the top panel for next brick queue
     * @param nextBrickPanelMid the middle panel for next brick queue
     * @param nextBrickPanelBottom the bottom panel for next brick queue
     * @param colorHelper the color helper for styling
     */
    public GuiRenderingHelper(
            GridPane gamePanel,
            GridPane brickPanel,
            GridPane ghostPanel,
            GridPane holdBrickPanel,
            GridPane nextBrickPanelTop,
            GridPane nextBrickPanelMid,
            GridPane nextBrickPanelBottom,
            GuiColorHelper colorHelper) {
        this.gamePanel = gamePanel;
        this.brickPanel = brickPanel;
        this.ghostPanel = ghostPanel;
        this.holdBrickPanel = holdBrickPanel;
        this.nextBrickPanelTop = nextBrickPanelTop;
        this.nextBrickPanelMid = nextBrickPanelMid;
        this.nextBrickPanelBottom = nextBrickPanelBottom;
        this.colorHelper = colorHelper;
    }
    
    /**
     * Sets the current game state (affects rendering behavior).
     *
     * @param gameState the current game state
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
    
    /**
     * Gets the display matrix of background rectangles.
     *
     * @return the display matrix, or null if not initialized
     */
    public Rectangle[][] getDisplayMatrix() {
        return displayMatrix;
    }
    
    /**
     * Gets the last view data snapshot.
     *
     * @return the last view data, or null if not set
     */
    public ViewData getLastViewData() {
        return lastViewData;
    }
    
    /**
     * Sets the last view data snapshot.
     *
     * @param lastViewData the view data to store
     */
    public void setLastViewData(ViewData lastViewData) {
        this.lastViewData = lastViewData;
    }
    
    /**
     * Initialise background cells: from the very beginning, use the final
     * rendering style (no "blur then become solid" jump).
     *
     * @param boardMatrix the board matrix to render
     */
    public void initBackgroundCells(int[][] boardMatrix) {
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];

        for (int row = HIDDEN_TOP_ROWS; row < boardMatrix.length; row++) {
            for (int col = 0; col < boardMatrix[row].length; col++) {
                Rectangle cell = new Rectangle(BRICK_SIZE, BRICK_SIZE);

                // Use the same rendering path as runtime refresh:
                colorHelper.setBackgroundRectangleData(boardMatrix[row][col], cell);

                // Grey grid line on top of the fill.
                cell.setStroke(Color.rgb(55, 55, 55));
                cell.setStrokeWidth(0.7);

                displayMatrix[row][col] = cell;
                gamePanel.add(cell, col, row - HIDDEN_TOP_ROWS);
            }
        }
    }

    /**
     * Initializes the falling brick display.
     *
     * @param brick the view data containing brick information
     */
    public void initFallingBrick(ViewData brick) {
        int[][] brickData = brick.getBrickData();
        rectangles = new Rectangle[brickData.length][brickData[0].length];
        for (int row = 0; row < brickData.length; row++) {
            for (int col = 0; col < brickData[row].length; col++) {
                Rectangle cell = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                colorHelper.setRectangleData(brickData[row][col], cell);
                rectangles[row][col] = cell;
                brickPanel.add(cell, col, row);
            }
        }
    }

    /**
     * Initialize the ghost/shadow piece overlay.
     * Creates rectangles for the shadow block at the landing position.
     *
     * @param brick the view data containing ghost position information
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
                colorHelper.setGhostRectangleData(brickData[row][col], cell);
                ghostRectangles[row][col] = cell;
                ghostPanel.add(cell, col, row);
            }
        }
    }

    // === NEXT (3-queue) initialisation & refresh ===

    /**
     * Initializes the next brick queue display (up to 3 bricks).
     *
     * @param brick the view data containing next queue information
     */
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
                colorHelper.setRectangleData(value, cell);

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

    /**
     * Refreshes the next brick queue display.
     *
     * @param brick the view data containing next queue information
     */
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

    // === HOLD initialisation & refresh ===

    /**
     * Initializes the held brick display.
     *
     * @param brick the view data containing hold brick information
     */
    public void initHoldBrick(ViewData brick) {
        if (holdBrickPanel == null) {
            return;
        }
        int[][] holdData = brick.getHoldBrickData();

        holdBrickRectangles = buildCenteredPreview(holdBrickPanel, holdData);
    }

    /**
     * Refreshes the held brick display.
     *
     * @param brick the view data containing hold brick information
     */
    public void refreshHoldBrick(ViewData brick) {
        if (holdBrickPanel == null) {
            return;
        }
        initHoldBrick(brick);
    }

    // === GHOST / SHADOW initialisation & refresh ===

    /**
     * Refresh the ghost/shadow piece to show where the current brick will land.
     * Updates both position and visual representation.
     *
     * @param brick the view data containing ghost position information
     * @param updateGhostPanelPosition callback to update ghost panel position
     */
    public void refreshGhost(ViewData brick, java.util.function.Consumer<ViewData> updateGhostPanelPosition) {
        if (ghostPanel == null || brick == null || gameState != GameState.PLAYING) {
            return;
        }

        // Initialize ghost rectangles if not already created
        if (ghostRectangles == null) {
            initGhost(brick);
        }

        // Update position to landing location
        updateGhostPanelPosition.accept(brick);

        // Update visual representation
        int[][] brickData = brick.getBrickData();
        if (ghostRectangles != null && ghostRectangles.length == brickData.length
                && ghostRectangles[0].length == brickData[0].length) {
            for (int row = 0; row < brickData.length; row++) {
                for (int col = 0; col < brickData[row].length; col++) {
                    if (ghostRectangles[row][col] != null) {
                        colorHelper.setGhostRectangleData(brickData[row][col], ghostRectangles[row][col]);
                    }
                }
            }
        } else {
            // If dimensions changed, reinitialize
            ghostPanel.getChildren().clear();
            initGhost(brick);
        }
    }

    /**
     * Refreshes the falling brick display and related previews.
     *
     * @param brick the view data containing current brick information
     * @param updateBrickPanelPosition callback to update brick panel position
     * @param updateGhostPanelPosition callback to update ghost panel position
     */
    public void refreshBrick(ViewData brick, java.util.function.Consumer<ViewData> updateBrickPanelPosition, java.util.function.Consumer<ViewData> updateGhostPanelPosition) {
        if (gameState == GameState.PLAYING) {
            lastViewData = brick;

            updateBrickPanelPosition.accept(brick);

            int[][] brickData = brick.getBrickData();
            for (int row = 0; row < brickData.length; row++) {
                for (int col = 0; col < brickData[row].length; col++) {
                    colorHelper.setRectangleData(brickData[row][col], rectangles[row][col]);
                }
            }

            refreshNextBrick(brick);
            refreshHoldBrick(brick);
            refreshGhost(brick, updateGhostPanelPosition);
        }
    }

    /**
     * Refreshes the game background display.
     *
     * @param board the board matrix to render
     * @param updateDangerFromBoard callback to update danger zone state
     * @param refreshGhostCallback callback to refresh ghost piece after background update
     */
    public void refreshGameBackground(int[][] board, java.util.function.Consumer<int[][]> updateDangerFromBoard, java.util.function.Consumer<ViewData> refreshGhostCallback) {
        for (int row = HIDDEN_TOP_ROWS; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                colorHelper.setBackgroundRectangleData(board[row][col], displayMatrix[row][col]);
            }
        }
        updateDangerFromBoard.accept(board);

        // After the background changes we must recompute and redraw the landing shadow.
        if (lastViewData != null && refreshGhostCallback != null) {
            refreshGhostCallback.accept(lastViewData);
        }
    }
    
    /**
     * Clears the falling brick panel.
     */
    public void clearBrickPanel() {
        if (brickPanel != null) {
            brickPanel.getChildren().clear();
        }
    }
    
    /**
     * Clears the ghost/shadow piece panel.
     */
    public void clearGhostPanel() {
        if (ghostPanel != null) {
            ghostPanel.getChildren().clear();
            ghostRectangles = null;
        }
    }
}


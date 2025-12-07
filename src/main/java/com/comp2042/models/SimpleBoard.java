package com.comp2042.models;

import com.comp2042.logic.bricks.Brick;
import com.comp2042.logic.bricks.BrickGenerator;
import com.comp2042.logic.bricks.RandomBrickGenerator;
import com.comp2042.logic.BrickMovementHandler;
import com.comp2042.logic.BrickRotator;
import com.comp2042.logic.CollisionDetector;
import com.comp2042.logic.GhostPieceCalculator;
import com.comp2042.logic.MatrixOperations;

import java.awt.Point;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Core game model that holds board state, active brick, and score.
 * Handles movement, rotation, collision detection, line clearing, and garbage rows.
 * Implements the Board interface to provide game logic functionality.
 */
public class SimpleBoard implements Board {

    // Board configuration: logical grid size.
    private final int rows;
    private final int columns;

    // Default brick spawn position (x, y) in grid coordinates.
    private static final int SPAWN_X = 4;
    private static final int SPAWN_Y = 1;

    // Core fields
    private final BrickGenerator brickGenerator;
    private final BrickRotator brickRotator;
    private final Score score;

    private int[][] boardMatrix;
    private Point currentOffset;
    
    // Extracted handlers for single responsibility
    private CollisionDetector collisionDetector;
    private BrickMovementHandler movementHandler;
    private GhostPieceCalculator ghostCalculator;

    // Active and held bricks
    private Brick currentBrick;
    private Brick heldBrick;
    private boolean hasHeldThisTurn;

    /**
     * Constructs a board with the given logical size.
     * Initializes the board matrix, brick generator, rotator, and score system.
     *
     * @param rows    number of rows (including hidden rows at the top for spawn area)
     * @param columns number of columns (standard Tetris uses 10)
     */
    public SimpleBoard(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.boardMatrix = new int[rows][columns];
        this.brickGenerator = new RandomBrickGenerator();
        this.brickRotator = new BrickRotator();
        this.score = new Score();
        this.currentBrick = null;
        this.heldBrick = null;
        this.hasHeldThisTurn = false;
        
        // Initialize handlers
        this.collisionDetector = new CollisionDetector(columns);
        this.movementHandler = new BrickMovementHandler(collisionDetector, brickRotator, boardMatrix);
        this.ghostCalculator = new GhostPieceCalculator(rows, boardMatrix);
    }

    /**
     * Move the current brick down by one cell.
     *
     * @return true if move succeeded, false if blocked
     */
    @Override
    public boolean moveBrickDown() {
        return tryMove(0, 1);
    }

    /**
     * Moves the current brick left by one cell.
     * Checks for collisions and left boundary before moving.
     *
     * @return true if move succeeded, false if blocked
     */
    @Override
    public boolean moveBrickLeft() {
        return tryMove(-1, 0);
    }

    /**
     * Moves the current brick right by one cell.
     * Checks for collisions and right boundary before moving.
     *
     * @return true if move succeeded, false if blocked
     */
    @Override
    public boolean moveBrickRight() {
        return tryMove(1, 0);
    }

    /**
     * Shared helper for moving the brick by a given offset.
     * Delegates to BrickMovementHandler for validation and movement.
     *
     * @return true if move is valid (no collision)
     */
    private boolean tryMove(int dx, int dy) {
        if (currentBrick == null || currentOffset == null) {
            return false;
        }
        return movementHandler.tryMove(currentOffset, dx, dy);
    }

    /**
     * Rotates the current brick to its next orientation.
     * Uses a wall-kick system: tries rotating in place first, then attempts
     * horizontal offsets (-1, +1, -2, +2) if the initial rotation would cause a collision.
     * This provides Tetris-style wall kicks on both left and right edges.
     *
     * @return true if rotation succeeded, false if all positions are blocked
     */
    @Override
    public boolean rotateLeftBrick() {
        if (currentBrick == null || currentOffset == null) {
            return false;
        }

        int[][] snapshot = MatrixOperations.copy(boardMatrix);
        NextShapeInfo nextShape = brickRotator.getNextShape();
        int[][] nextShapeMatrix = nextShape.getShape();

        int currentX = (int) currentOffset.getX();
        int currentY = (int) currentOffset.getY();

        // Candidate horizontal offsets: in place, then small kicks.
        int[] kicks = {0, -1, 1, -2, 2};

        for (int dx : kicks) {
            int newX = currentX + dx;

            // Must stay inside horizontal bounds.
            if (!collisionDetector.isWithinHorizontalBounds(nextShapeMatrix, newX)) {
                continue;
            }

            boolean conflict = collisionDetector.hasCollision(
                    snapshot,
                    nextShapeMatrix,
                    newX,
                    currentY
            );

            if (!conflict) {
                // Apply this rotation + horizontal shift.
                brickRotator.setCurrentShape(nextShape.getPosition());
                currentOffset.setLocation(newX, currentY);
                return true;
            }
        }

        // No valid rotation position was found.
        return false;
    }


    /**
     * Creates a new brick at the spawn position.
     * Generates a random brick from the brick generator and places it at the top center.
     *
     * @return true if the new brick immediately collides with existing blocks (game over),
     *         false if the brick spawns successfully
     */
    @Override
    public boolean createNewBrick() {
        return spawnNewBrickFromGenerator();
    }

    /**
     * Spawn the next brick from the generator and position it at the spawn point.
     *
     * @return true if the new brick immediately collides with existing blocks
     */
    private boolean spawnNewBrickFromGenerator() {
        currentBrick = brickGenerator.getBrick();
        brickRotator.setBrick(currentBrick);
        currentOffset = new Point(SPAWN_X, SPAWN_Y);

        // If we already intersect something, the game is over.
        return collisionDetector.hasCollision(
                boardMatrix,
                brickRotator.getCurrentShape(),
                (int) currentOffset.getX(),
                (int) currentOffset.getY()
        );
    }

    /**
     * Holds or swaps the current brick.
     * If no brick is held, moves current brick into hold and spawns the next brick.
     * If a brick is already held, swaps it with the current one.
     * This can only be used once per brick life-cycle (until the brick lands).
     *
     * @return true if the new active brick immediately collides with existing blocks (game over),
     *         false if the hold/swap succeeded
     */
    @Override
    public boolean holdCurrentBrick() {
        // No active brick or already used hold for this piece.
        if (currentBrick == null || hasHeldThisTurn) {
            return false;
        }

        boolean collision;

        if (heldBrick == null) {
            // First time holding: store current brick and spawn a new one.
            heldBrick = currentBrick;
            collision = spawnNewBrickFromGenerator();
        } else {
            // Swap current and held bricks, then respawn at the top.
            Brick temp = currentBrick;
            currentBrick = heldBrick;
            heldBrick = temp;

            brickRotator.setBrick(currentBrick);
            currentOffset = new Point(SPAWN_X, SPAWN_Y);

            collision = collisionDetector.hasCollision(
                    boardMatrix,
                    brickRotator.getCurrentShape(),
                    (int) currentOffset.getX(),
                    (int) currentOffset.getY()
            );
        }

        hasHeldThisTurn = true;
        return collision;
    }

    /**
     * Returns a defensive copy of the board background matrix.
     * The matrix represents the static blocks that have been placed on the board.
     *
     * @return a copy of the board matrix (rows x columns)
     */
    @Override
    public int[][] getBoardMatrix() {
        return boardMatrix;
    }

    /**
     * Returns a snapshot of the current view state for rendering.
     * Includes the active brick, its position, next/hold previews, and ghost position.
     *
     * @return ViewData containing all information needed to render the current game state
     */
    @Override
    public ViewData getViewData() {
        // Get up to 3 upcoming bricks from the generator.
        Brick[] upcomingBricks = brickGenerator.getNextQueue(3);
        int[][][] nextQueue = null;
        int[][] nextData = null;

        if (upcomingBricks != null && upcomingBricks.length > 0) {
            nextQueue = new int[upcomingBricks.length][][];
            for (int i = 0; i < upcomingBricks.length; i++) {
                nextQueue[i] = MatrixOperations.copy(upcomingBricks[i].getShapeMatrix().get(0));
            }
            // First upcoming piece used for backwards-compatible API.
            nextData = MatrixOperations.copy(nextQueue[0]);
        }

        int[][] holdData = null;
        if (heldBrick != null) {
            holdData = heldBrick.getShapeMatrix().get(0);
        }

        int currentX = (int) currentOffset.getX();
        int currentY = (int) currentOffset.getY();
        int[][] currentShape = brickRotator.getCurrentShape();

        // Compute where this brick would finally land if hard-dropped straight down.
        int ghostY = ghostCalculator.computeLandingY(currentX, currentY, currentShape);

        return new ViewData(
                currentShape,
                currentX,
                currentY,
                nextData,
                nextQueue,
                holdData,
                currentX,
                ghostY
        );
    }

    /**
     * Returns the upcoming brick queue as shape matrices for preview.
     * Index 0 is the next brick to spawn.
     */
    @Override
    public int[][][] getNextQueue() {
        Brick[] upcomingBricks = brickGenerator.getNextQueue(3);
        if (upcomingBricks == null || upcomingBricks.length == 0) {
            return null;
        }
        int[][][] queue = new int[upcomingBricks.length][][];
        for (int i = 0; i < upcomingBricks.length; i++) {
            queue[i] = MatrixOperations.copy(upcomingBricks[i].getShapeMatrix().get(0));
        }
        return queue;
    }

    /**
     * Merges the falling brick into the background matrix.
     * Called when a brick can no longer move down. The brick's cells are permanently
     * added to the board matrix. After merging, a new brick will be spawned.
     */
    @Override
    public void mergeBrickToBackground() {
        boardMatrix = MatrixOperations.merge(
                boardMatrix,
                brickRotator.getCurrentShape(),
                (int) currentOffset.getX(),
                (int) currentOffset.getY()
        );
        // Update handler references to the new board matrix
        movementHandler.updateBoardMatrix(boardMatrix);
        ghostCalculator.updateBoardMatrix(boardMatrix);
        // A new brick will spawn after this, so allow hold again.
        hasHeldThisTurn = false;
    }

    /**
     * Checks for and clears full rows, updating the matrix safely.
     * Removes complete horizontal rows and shifts remaining blocks down.
     * Calculates score bonus based on the number of lines cleared.
     *
     * @return ClearRow object containing the number of lines removed and score bonus,
     *         or null if no lines were cleared
     */
    @Override
    public ClearRow clearRows() {
        ClearRow result = MatrixOperations.checkRemoving(boardMatrix);
        if (result != null) {
            boardMatrix = result.getNewMatrix();
            // Update handler references to the new board matrix
            movementHandler.updateBoardMatrix(boardMatrix);
            ghostCalculator.updateBoardMatrix(boardMatrix);
        }
        return result;
    }

    /**
     * Pushes existing rows up by one and inserts a garbage row at the bottom.
     * The garbage row uses a single random colour with exactly one hole,
     * similar to typical survival-style Tetris garbage.
     * Used in Survival mode when the player fails to clear lines.
     */
    @Override
    public void addGarbageRow() {
        // Shift all rows up: row 1 becomes row 0, row 2 becomes row 1, etc.
        for (int row = 0; row < rows - 1; row++) {
            boardMatrix[row] = Arrays.copyOf(boardMatrix[row + 1], columns);
        }

        // Build a new bottom row: one hole and a solid colour everywhere else.
        int[] garbageRow = new int[columns];
        Arrays.fill(garbageRow, 0);

        // Pick the hole column.
        int holeIndex = ThreadLocalRandom.current().nextInt(columns);

        // Pick a colour id for the entire garbage row (1..7 to match normal bricks).
        int colourId = ThreadLocalRandom.current().nextInt(1, 8);

        for (int x = 0; x < columns; x++) {
            if (x != holeIndex) {
                garbageRow[x] = colourId;
            }
        }

        boardMatrix[rows - 1] = garbageRow;
    }

    /**
     * Returns the Score object associated with this board.
     * Used to track points, level, lines cleared, and combo multiplier.
     *
     * @return the Score instance for this board
     */
    @Override
    public Score getScore() {
        return score;
    }

    /**
     * Resets the board, score, and spawns a new brick.
     * Clears the board matrix, resets the score to zero, and starts a fresh game.
     */
    @Override
    public void newGame() {
        boardMatrix = new int[rows][columns];
        score.reset();
        currentBrick = null;
        heldBrick = null;
        hasHeldThisTurn = false;
        
        // Reinitialize handlers with new board matrix
        this.collisionDetector = new CollisionDetector(columns);
        this.movementHandler = new BrickMovementHandler(collisionDetector, brickRotator, boardMatrix);
        this.ghostCalculator = new GhostPieceCalculator(rows, boardMatrix);
        
        createNewBrick();
    }

}

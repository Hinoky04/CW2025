package com.comp2042;

import com.comp2042.logic.bricks.Brick;
import com.comp2042.logic.bricks.BrickGenerator;
import com.comp2042.logic.bricks.RandomBrickGenerator;

import java.awt.Point;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Core game model that holds board state, active brick, and score.
 * Handles movement, rotation, collision, clearing rows and garbage rows.
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

    // Active and held bricks
    private Brick currentBrick;
    private Brick heldBrick;
    private boolean hasHeldThisTurn;

    /**
     * Construct a board with the given logical size.
     *
     * @param rows    number of rows (including hidden rows at the top)
     * @param columns number of columns
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
     * Move the current brick left by one cell.
     */
    @Override
    public boolean moveBrickLeft() {
        return tryMove(-1, 0);
    }

    /**
     * Move the current brick right by one cell.
     */
    @Override
    public boolean moveBrickRight() {
        return tryMove(1, 0);
    }

    /**
     * Shared helper for moving the brick by a given offset.
     *
     * It also enforces that the shape stays fully inside the horizontal
     * board bounds so the active piece never sticks out of the board.
     *
     * @return true if move is valid (no collision)
     */
    private boolean tryMove(int dx, int dy) {
        if (currentBrick == null || currentOffset == null) {
            return false;
        }

        int[][] snapshot = MatrixOperations.copy(boardMatrix);
        Point next = new Point(currentOffset);
        next.translate(dx, dy);

        int nextX = (int) next.getX();
        int nextY = (int) next.getY();
        int[][] currentShape = brickRotator.getCurrentShape();

        // Prevent moving outside horizontal bounds.
        if (!isWithinHorizontalBounds(currentShape, nextX)) {
            return false;
        }

        boolean conflict = MatrixOperations.intersect(
                snapshot,
                currentShape,
                nextX,
                nextY
        );
        if (conflict) {
            return false;
        }
        currentOffset = next;
        return true;
    }

    /**
     * Rotate the current brick to its next orientation.
     *
     * Strategy:
     *   1. Try rotating in place at the current offset.
     *   2. If that fails, try a small set of horizontal "kicks":
     *      dx in { -1, +1, -2, +2 }.
     *   3. The first position that is inside the board and collision-free
     *      is accepted.
     *
     * This gives a symmetric, Tetris-style wall kick on both left and right
     * edges without moving the board itself.
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
            if (!isWithinHorizontalBounds(nextShapeMatrix, newX)) {
                continue;
            }

            boolean conflict = MatrixOperations.intersect(
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
     * Returns true if placing the given shape at offsetX keeps all non-empty cells
     * inside the horizontal board bounds [0, columns - 1].
     */
    private boolean isWithinHorizontalBounds(int[][] shape, int offsetX) {
        int minLocalX = Integer.MAX_VALUE;
        int maxLocalX = Integer.MIN_VALUE;

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] != 0) {
                    if (col < minLocalX) {
                        minLocalX = col;
                    }
                    if (col > maxLocalX) {
                        maxLocalX = col;
                    }
                }
            }
        }

        // Empty shape (should not happen), treat as in bounds.
        if (minLocalX == Integer.MAX_VALUE) {
            return true;
        }

        int left = offsetX + minLocalX;
        int right = offsetX + maxLocalX;

        return left >= 0 && right < columns;
    }

    /**
     * Create a new brick at the spawn position.
     *
     * @return true if the new brick immediately collides with existing blocks
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
        return MatrixOperations.intersect(
                boardMatrix,
                brickRotator.getCurrentShape(),
                (int) currentOffset.getX(),
                (int) currentOffset.getY()
        );
    }

    /**
     * Hold or swap the current brick.
     * If no brick is held, move current brick into hold and spawn the next brick.
     * If a brick is already held, swap it with the current one.
     * This can only be used once per brick life-cycle.
     *
     * @return true if the new active brick immediately collides with existing blocks
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

            collision = MatrixOperations.intersect(
                    boardMatrix,
                    brickRotator.getCurrentShape(),
                    (int) currentOffset.getX(),
                    (int) currentOffset.getY()
            );
        }

        hasHeldThisTurn = true;
        return collision;
    }

    @Override
    public int[][] getBoardMatrix() {
        return boardMatrix;
    }

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

        return new ViewData(
                brickRotator.getCurrentShape(),
                (int) currentOffset.getX(),
                (int) currentOffset.getY(),
                nextData,
                nextQueue,
                holdData
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
     * Merge the falling brick into the background matrix.
     */
    @Override
    public void mergeBrickToBackground() {
        boardMatrix = MatrixOperations.merge(
                boardMatrix,
                brickRotator.getCurrentShape(),
                (int) currentOffset.getX(),
                (int) currentOffset.getY()
        );
        // A new brick will spawn after this, so allow hold again.
        hasHeldThisTurn = false;
    }

    /**
     * Check and clear full rows, update matrix safely.
     */
    @Override
    public ClearRow clearRows() {
        ClearRow result = MatrixOperations.checkRemoving(boardMatrix);
        if (result != null) {
            boardMatrix = result.getNewMatrix();
        }
        return result;
    }

    /**
     * Pushes existing rows up by one and inserts a garbage row at the bottom.
     * The garbage row uses a single random colour with exactly one hole,
     * similar to typical survival-style Tetris garbage.
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

    @Override
    public Score getScore() {
        return score;
    }

    /**
     * Reset board, score, and spawn a new brick.
     */
    @Override
    public void newGame() {
        boardMatrix = new int[rows][columns];
        score.reset();
        currentBrick = null;
        heldBrick = null;
        hasHeldThisTurn = false;
        createNewBrick();
    }
}

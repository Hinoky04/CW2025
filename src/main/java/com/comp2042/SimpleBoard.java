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

    /**
     * Construct a board with the given logical size.
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
    }

    /**
     * Move the current brick down by one cell.
     * @return true if move succeeded, false if blocked
     */
    @Override
    public boolean moveBrickDown() {
        return tryMove(0, 1);
    }

    /** Move the current brick left by one cell. */
    @Override
    public boolean moveBrickLeft() {
        return tryMove(-1, 0);
    }

    /** Move the current brick right by one cell. */
    @Override
    public boolean moveBrickRight() {
        return tryMove(1, 0);
    }

    /**
     * Shared helper for moving the brick by a given offset.
     * @return true if move is valid (no collision)
     */
    private boolean tryMove(int dx, int dy) {
        int[][] snapshot = MatrixOperations.copy(boardMatrix);
        Point next = new Point(currentOffset);
        next.translate(dx, dy);
        boolean conflict = MatrixOperations.intersect(
                snapshot,
                brickRotator.getCurrentShape(),
                (int) next.getX(),
                (int) next.getY()
        );
        if (conflict) {
            return false;
        }
        currentOffset = next;
        return true;
    }

    /**
     * Rotate the current brick to its next orientation.
     * Uses a simple wall-kick so rotations near the border are less frustrating.
     */
    @Override
    public boolean rotateLeftBrick() {
        int[][] snapshot = MatrixOperations.copy(boardMatrix);
        NextShapeInfo nextShape = brickRotator.getNextShape();

        int currentX = (int) currentOffset.getX();
        int currentY = (int) currentOffset.getY();

        // 1) Try rotating in place first.
        boolean conflictInPlace = MatrixOperations.intersect(
                snapshot,
                nextShape.getShape(),
                currentX,
                currentY
        );
        if (!conflictInPlace) {
            brickRotator.setCurrentShape(nextShape.getPosition());
            return true;
        }

        // 2) Simple horizontal wall kick: try shifting one cell left or right.
        int[] kicks = {-1, 1};
        for (int dx : kicks) {
            int kickedX = currentX + dx;
            boolean conflictWithKick = MatrixOperations.intersect(
                    snapshot,
                    nextShape.getShape(),
                    kickedX,
                    currentY
            );
            if (!conflictWithKick) {
                brickRotator.setCurrentShape(nextShape.getPosition());
                currentOffset.translate(dx, 0);
                return true;
            }
        }

        // 3) No valid rotation position found → keep the brick as it is.
        return false;
    }

    /**
     * Create a new brick at the spawn position.
     * @return true if the new brick immediately collides with existing blocks
     */
    @Override
    public boolean createNewBrick() {
        Brick newBrick = brickGenerator.getBrick();
        brickRotator.setBrick(newBrick);
        currentOffset = new Point(SPAWN_X, SPAWN_Y);

        // If we already intersect something, the game is over.
        return MatrixOperations.intersect(
                boardMatrix,
                brickRotator.getCurrentShape(),
                (int) currentOffset.getX(),
                (int) currentOffset.getY()
        );
    }

    @Override
    public int[][] getBoardMatrix() {
        return boardMatrix;
    }

    @Override
    public ViewData getViewData() {
        return new ViewData(
                brickRotator.getCurrentShape(),
                (int) currentOffset.getX(),
                (int) currentOffset.getY(),
                brickGenerator.getNextBrick().getShapeMatrix().get(0)
        );
    }

    /** Merge the falling brick into the background matrix. */
    @Override
    public void mergeBrickToBackground() {
        boardMatrix = MatrixOperations.merge(
                boardMatrix,
                brickRotator.getCurrentShape(),
                (int) currentOffset.getX(),
                (int) currentOffset.getY()
        );
    }

    /** Check and clear full rows, update matrix safely. */
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

    /** Reset board, score, and spawn a new brick. */
    @Override
    public void newGame() {
        boardMatrix = new int[rows][columns];
        score.reset();
        createNewBrick();
    }
}

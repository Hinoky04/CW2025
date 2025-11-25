package com.comp2042;

import com.comp2042.logic.bricks.Brick;
import com.comp2042.logic.bricks.BrickGenerator;
import com.comp2042.logic.bricks.RandomBrickGenerator;

import java.awt.Point;

/**
 * Core game model that holds board state, active brick, and score.
 * Handles movement, rotation, collision, and clearing rows.
 */
public class SimpleBoard implements Board {

    // === Board configuration ===

    // OLD (fixed size, parameters ignored):
    // private static final int ROWS = 25;
    // private static final int COLUMNS = 10;

    // NEW: store board size as fields so the constructor parameters matter.
    // This makes the class more reusable and matches the "width/height" rubric bullet.
    private final int rows;
    private final int columns;

    // Default brick spawn position (x,y)
    private static final int SPAWN_X = 4;
    private static final int SPAWN_Y = 1;

    // Core fields
    private final BrickGenerator brickGenerator;
    private final BrickRotator brickRotator;
    private final Score score;
    private int[][] boardMatrix;
    private Point currentOffset;

    // OLD constructor (ignored width/height and used constants):
    //
    // public SimpleBoard(int width, int height) {
    //     // ignore parameters for now; keep constants for clarity
    //     this.boardMatrix = new int[ROWS][COLUMNS];
    //     this.brickGenerator = new RandomBrickGenerator();
    //     this.brickRotator = new BrickRotator();
    //     this.score = new Score();
    // }

    /**
     * NEW constructor: use the given dimensions for the internal matrix.
     * This removes the confusion around width/height and makes the board configurable.
     */
    public SimpleBoard(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.boardMatrix = new int[rows][columns];
        this.brickGenerator = new RandomBrickGenerator();
        this.brickRotator = new BrickRotator();
        this.score = new Score();
    }

    /** Move the current brick down by one cell. */
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

    /** Rotate current brick left if possible, with a simple wall-kick near borders. */
    @Override
    public boolean rotateLeftBrick() {
        int[][] snapshot = MatrixOperations.copy(boardMatrix);
        NextShapeInfo nextShape = brickRotator.getNextShape();

        int currentX = (int) currentOffset.getX();
        int currentY = (int) currentOffset.getY();

        // 1) Try rotating in place first
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

        // 2) Simple wall-kick: try shifting one column left or right
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
                // Move the brick one step away from the wall and apply the rotation
                currentOffset = new Point(kickedX, currentY);
                brickRotator.setCurrentShape(nextShape.getPosition());
                return true;
            }
        }

        // 3) No valid rotation position found → keep the brick as it is
        return false;
    }

    /**
     * Creates a new falling brick.
     * @return true if the new brick immediately collides (→ game over)
     */
    @Override
    public boolean createNewBrick() {
        Brick newBrick = brickGenerator.getBrick();
        brickRotator.setBrick(newBrick);
        currentOffset = new Point(SPAWN_X, SPAWN_Y);
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

    @Override
    public Score getScore() {
        return score;
    }

    /** Reset board, score, and spawn a new brick. */
    @Override
    public void newGame() {
        // OLD:
        // boardMatrix = new int[ROWS][COLUMNS];

        // NEW: use the stored rows/columns so the board respects constructor size.
        boardMatrix = new int[rows][columns];
        score.reset();
        createNewBrick();
    }
}

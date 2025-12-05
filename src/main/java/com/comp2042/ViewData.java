package com.comp2042;

/**
 * Data class containing a snapshot of the current game view state.
 * Used to pass rendering information from the game logic to the GUI.
 * Includes active brick, position, next/hold previews, and ghost piece position.
 */
public final class ViewData {

    private final int[][] brickData;
    private final int xPosition;
    private final int yPosition;

    /** First upcoming brick (kept for backwards compatibility). */
    private final int[][] nextBrickData;

    /** Queue of upcoming bricks for preview (index 0 is the next brick). May be null. */
    private final int[][][] nextQueue;

    /** Held brick data, or null if no brick is held. */
    private final int[][] holdBrickData;

    /**
     * Ghost landing position for the current brick.
     * If the board does not provide a ghost, these may simply match (xPosition, yPosition).
     */
    private final int ghostXPosition;
    private final int ghostYPosition;

    /**
     * Constructor for ViewData without hold brick or next queue.
     * Used for backwards compatibility.
     *
     * @param brickData the current falling brick shape
     * @param xPosition the X position of the brick
     * @param yPosition the Y position of the brick
     * @param nextBrickData the next brick to spawn
     */
    public ViewData(int[][] brickData,
                    int xPosition,
                    int yPosition,
                    int[][] nextBrickData) {
        this(brickData, xPosition, yPosition, nextBrickData, null, null, xPosition, yPosition);
    }

    /**
     * Constructor for ViewData with hold brick but no next queue.
     *
     * @param brickData the current falling brick shape
     * @param xPosition the X position of the brick
     * @param yPosition the Y position of the brick
     * @param nextBrickData the next brick to spawn
     * @param holdBrickData the held brick (may be null)
     */
    public ViewData(int[][] brickData,
                    int xPosition,
                    int yPosition,
                    int[][] nextBrickData,
                    int[][] holdBrickData) {
        this(brickData, xPosition, yPosition, nextBrickData, null, holdBrickData, xPosition, yPosition);
    }

    /**
     * Full constructor supporting next queue, hold brick, and ghost position.
     *
     * @param brickData current falling brick shape
     * @param xPosition X position of the brick on the board
     * @param yPosition Y position of the brick on the board
     * @param nextBrickData first upcoming brick (same as queue[0] if provided)
     * @param nextQueue queue of upcoming bricks (up to 3, may be null)
     * @param holdBrickData held brick (may be null)
     * @param ghostXPosition X position where the brick would land if hard-dropped
     * @param ghostYPosition Y position where the brick would land if hard-dropped
     */
    public ViewData(int[][] brickData,
                    int xPosition,
                    int yPosition,
                    int[][] nextBrickData,
                    int[][][] nextQueue,
                    int[][] holdBrickData,
                    int ghostXPosition,
                    int ghostYPosition) {
        this.brickData = brickData;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.nextBrickData = nextBrickData;
        this.nextQueue = nextQueue;
        this.holdBrickData = holdBrickData;
        this.ghostXPosition = ghostXPosition;
        this.ghostYPosition = ghostYPosition;
    }

    /**
     * Gets a defensive copy of the current falling brick shape.
     *
     * @return a copy of the brick data matrix
     */
    public int[][] getBrickData() {
        return MatrixOperations.copy(brickData);
    }

    /**
     * Gets the X position of the current brick.
     *
     * @return the X coordinate
     */
    public int getxPosition() {
        return xPosition;
    }

    /**
     * Gets the Y position of the current brick.
     *
     * @return the Y coordinate
     */
    public int getyPosition() {
        return yPosition;
    }

    /**
     * Returns a copy of the first upcoming brick for backwards compatibility.
     */
    public int[][] getNextBrickData() {
        return MatrixOperations.copy(nextBrickData);
    }

    /**
     * Returns a defensive copy of the full next queue (up to 3 bricks),
     * or null if the board does not provide a queue.
     */
    public int[][][] getNextQueue() {
        if (nextQueue == null) {
            return null;
        }
        int[][][] copy = new int[nextQueue.length][][];
        for (int i = 0; i < nextQueue.length; i++) {
            copy[i] = MatrixOperations.copy(nextQueue[i]);
        }
        return copy;
    }

    /**
     * Returns a defensive copy of the held brick data, or null if no brick is held.
     */
    public int[][] getHoldBrickData() {
        if (holdBrickData == null) {
            return null;
        }
        return MatrixOperations.copy(holdBrickData);
    }

    /** X position where the current brick would finally land if hard-dropped. */
    public int getGhostXPosition() {
        return ghostXPosition;
    }

    /** Y position where the current brick would finally land if hard-dropped. */
    public int getGhostYPosition() {
        return ghostYPosition;
    }
}

package com.comp2042;

public final class ViewData {

    private final int[][] brickData;
    private final int xPosition;
    private final int yPosition;

    /**
     * First upcoming brick (kept for backwards compatibility).
     */
    private final int[][] nextBrickData;

    /**
     * Queue of upcoming bricks for preview (index 0 is the next brick).
     * May be null if not provided by the board.
     */
    private final int[][][] nextQueue;

    /**
     * Held brick data, or null if no brick is held.
     */
    private final int[][] holdBrickData;

    /**
     * Existing constructor – no hold brick, no queue.
     */
    public ViewData(int[][] brickData,
                    int xPosition,
                    int yPosition,
                    int[][] nextBrickData) {
        this(brickData, xPosition, yPosition, nextBrickData, null, null);
    }

    /**
     * Existing constructor – with hold brick, no queue.
     */
    public ViewData(int[][] brickData,
                    int xPosition,
                    int yPosition,
                    int[][] nextBrickData,
                    int[][] holdBrickData) {
        this(brickData, xPosition, yPosition, nextBrickData, null, holdBrickData);
    }

    /**
     * New constructor – supports a full next queue (up to 3 bricks) and hold brick.
     *
     * @param brickData     current falling brick.
     * @param xPosition     x position on the board.
     * @param yPosition     y position on the board.
     * @param nextBrickData first upcoming brick (same as queue[0] if provided).
     * @param nextQueue     queue of upcoming bricks (may be null).
     * @param holdBrickData held brick (may be null).
     */
    public ViewData(int[][] brickData,
                    int xPosition,
                    int yPosition,
                    int[][] nextBrickData,
                    int[][][] nextQueue,
                    int[][] holdBrickData) {
        this.brickData = brickData;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.nextBrickData = nextBrickData;
        this.nextQueue = nextQueue;
        this.holdBrickData = holdBrickData;
    }

    public int[][] getBrickData() {
        return MatrixOperations.copy(brickData);
    }

    public int getxPosition() {
        return xPosition;
    }

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
}

package com.comp2042;

/**
 * Core game board API used by the controller and GUI.
 * Defines the interface for game board implementations.
 * Implementations manage the falling brick, background matrix,
 * line clearing, scoring, and next/hold previews.
 */
public interface Board {

    boolean moveBrickDown();

    boolean moveBrickLeft();

    boolean moveBrickRight();

    boolean rotateLeftBrick();

    /**
     * Holds or swaps the current active brick.
     *
     * @return true if the newly active brick immediately collides with existing blocks (game over),
     *         false otherwise
     */
    boolean holdCurrentBrick();

    /**
     * Creates a new brick at the spawn position.
     *
     * @return true if the new brick immediately collides with existing blocks (game over),
     *         false if spawn succeeded
     */
    boolean createNewBrick();

    /**
     * Returns a defensive copy of the board background matrix.
     *
     * @return a copy of the board matrix
     */
    int[][] getBoardMatrix();

    /**
     * Returns a snapshot of the current view state.
     * Includes active brick, its position, next/hold previews, and ghost position.
     *
     * @return ViewData containing all rendering information
     */
    ViewData getViewData();

    /**
     * Returns a queue of upcoming bricks for preview.
     * Index 0 is the next brick to spawn.
     * Implementations should normally provide up to 3 entries,
     * but may return fewer at the very start of a game.
     *
     * @return array of brick shape matrices, or null if no bricks are queued
     */
    int[][][] getNextQueue();

    /**
     * Merges the current falling brick into the background matrix.
     * Called when a brick can no longer move down.
     */
    void mergeBrickToBackground();

    /**
     * Clears any full rows and returns information about what was removed.
     *
     * @return ClearRow object containing lines removed and score bonus, or null if no lines cleared
     */
    ClearRow clearRows();

    /**
     * Adds a garbage row at the bottom of the board and pushes existing rows up.
     * Implementations decide the exact garbage pattern and colour.
     * Used in Survival mode when the player fails to clear lines.
     */
    void addGarbageRow();

    /**
     * Gets the Score object associated with this board.
     *
     * @return the Score instance
     */
    Score getScore();

    /**
     * Resets the board to a fresh game state and spawns the first brick.
     * Clears the board matrix and resets the score.
     */
    void newGame();
}

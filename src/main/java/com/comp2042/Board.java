package com.comp2042;

/**
 * Core game board API used by the controller and GUI.
 *
 * Implementations manage the falling brick, background matrix,
 * line clearing, scoring and next/hold previews.
 */
public interface Board {

    boolean moveBrickDown();

    boolean moveBrickLeft();

    boolean moveBrickRight();

    boolean rotateLeftBrick();

    /**
     * Hold or swap the current active brick.
     *
     * @return true if the newly active brick immediately collides with existing blocks (game over), false otherwise
     */
    boolean holdCurrentBrick();

    /**
     * Create a new brick at the spawn position.
     *
     * @return true if the new brick immediately collides with existing blocks.
     */
    boolean createNewBrick();

    /**
     * Returns a defensive copy of the board background matrix.
     */
    int[][] getBoardMatrix();

    /**
     * Returns a snapshot of the current view state:
     * active brick, its position, next/hold previews, etc.
     */
    ViewData getViewData();

    /**
     * Returns a queue of upcoming bricks for preview.
     * Index 0 is the next brick to spawn.
     * Implementations should normally provide up to 3 entries,
     * but may return fewer at the very start of a game.
     */
    int[][][] getNextQueue();

    /**
     * Merge the current falling brick into the background matrix.
     */
    void mergeBrickToBackground();

    /**
     * Clear any full rows and return information about what was removed.
     */
    ClearRow clearRows();

    /**
     * Adds a garbage row at the bottom of the board and pushes existing rows up.
     * Implementations decide the exact garbage pattern and colour.
     */
    void addGarbageRow();

    Score getScore();

    /**
     * Resets the board to a fresh game state and spawns the first brick.
     */
    void newGame();
}

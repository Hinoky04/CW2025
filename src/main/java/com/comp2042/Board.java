package com.comp2042;

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
     * @return true if the new brick immediately collides with existing blocks
     */
    boolean createNewBrick();

    int[][] getBoardMatrix();

    ViewData getViewData();

    void mergeBrickToBackground();

    ClearRow clearRows();

    /**
     * Adds a garbage row at the bottom of the board and pushes existing rows up.
     * Implementations decide the exact garbage pattern and colour.
     */
    void addGarbageRow();

    Score getScore();

    void newGame();
}

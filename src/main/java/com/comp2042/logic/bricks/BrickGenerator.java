package com.comp2042.logic.bricks;

/**
 * Provides bricks for the game. Implementations are responsible for
 * the randomisation / bag system and any preview queue.
 */
public interface BrickGenerator {

    /**
     * Consumes and returns the next brick to be spawned on the board.
     */
    Brick getBrick();

    /**
     * Returns the next brick that will be spawned without consuming it.
     */
    Brick getNextBrick();

    /**
     * Returns a snapshot of the upcoming bricks queue, up to {@code maxCount}
     * bricks, without consuming them.
     *
     * Index 0 is the brick that would be returned by {@link #getBrick()} next.
     *
     * @param maxCount maximum number of bricks to return
     * @return an array of upcoming bricks (length 0..maxCount), never null
     */
    Brick[] getNextQueue(int maxCount);
}

package com.comp2042;

import com.comp2042.logic.bricks.Brick;

/**
 * Handles rotation logic for Tetris bricks.
 * Manages the current rotation state and provides methods to get the next rotation.
 */
public class BrickRotator {

    private Brick brick;
    private int currentShape = 0;

    /**
     * Gets the next rotation shape for the current brick.
     *
     * @return NextShapeInfo containing the next shape matrix and its rotation index
     */
    public NextShapeInfo getNextShape() {
        int nextShape = currentShape;
        nextShape = (++nextShape) % brick.getShapeMatrix().size();
        return new NextShapeInfo(brick.getShapeMatrix().get(nextShape), nextShape);
    }

    /**
     * Gets the current rotation shape of the brick.
     *
     * @return the current shape matrix
     */
    public int[][] getCurrentShape() {
        return brick.getShapeMatrix().get(currentShape);
    }

    /**
     * Sets the current rotation index of the brick.
     *
     * @param currentShape the rotation index to set (0-based)
     */
    public void setCurrentShape(int currentShape) {
        this.currentShape = currentShape;
    }

    /**
     * Sets the brick to rotate and resets to the first rotation.
     *
     * @param brick the brick to set
     */
    public void setBrick(Brick brick) {
        this.brick = brick;
        currentShape = 0;
    }


}

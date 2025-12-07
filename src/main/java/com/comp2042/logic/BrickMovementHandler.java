package com.comp2042.logic;

import java.awt.Point;

/**
 * Handles brick movement logic including validation and position updates.
 * Responsible for moving bricks left, right, and down while checking for collisions.
 */
public class BrickMovementHandler {
    
    private final CollisionDetector collisionDetector;
    private final BrickRotator brickRotator;
    private int[][] boardMatrix;  // Not final - can be updated when board changes
    
    /**
     * Creates a movement handler with the necessary dependencies.
     * 
     * @param collisionDetector the collision detector for validating moves
     * @param brickRotator the rotator that provides the current brick shape
     * @param boardMatrix the current board state (reference, not copied)
     */
    public BrickMovementHandler(CollisionDetector collisionDetector, 
                                BrickRotator brickRotator,
                                int[][] boardMatrix) {
        this.collisionDetector = collisionDetector;
        this.brickRotator = brickRotator;
        this.boardMatrix = boardMatrix;
    }
    
    /**
     * Attempts to move the brick by the given offset.
     * Validates the move against board boundaries and collisions.
     * 
     * @param currentOffset the current brick position (will be updated if move succeeds)
     * @param dx the horizontal offset (negative for left, positive for right)
     * @param dy the vertical offset (positive for down)
     * @return true if the move succeeded, false if blocked
     */
    public boolean tryMove(Point currentOffset, int dx, int dy) {
        if (currentOffset == null) {
            return false;
        }

        int[][] snapshot = MatrixOperations.copy(boardMatrix);
        Point next = new Point(currentOffset);
        next.translate(dx, dy);

        int nextX = (int) next.getX();
        int nextY = (int) next.getY();
        int[][] currentShape = brickRotator.getCurrentShape();

        // Prevent moving outside horizontal bounds.
        if (!collisionDetector.isWithinHorizontalBounds(currentShape, nextX)) {
            return false;
        }

        // Check for collision with existing blocks.
        if (collisionDetector.hasCollision(snapshot, currentShape, nextX, nextY)) {
            return false;
        }
        
        // Move is valid, update position.
        currentOffset.setLocation(next);
        return true;
    }
    
    /**
     * Updates the board matrix reference when the board state changes.
     * This is necessary because mergeBrickToBackground() creates a new array.
     * 
     * @param newBoardMatrix the new board matrix
     */
    public void updateBoardMatrix(int[][] newBoardMatrix) {
        this.boardMatrix = newBoardMatrix;
    }
}


package com.comp2042.logic;

/**
 * Handles collision detection for brick placement on the board.
 * Checks if a brick shape can be placed at a given position without conflicts.
 */
public class CollisionDetector {
    
    private final int columns;
    
    /**
     * Creates a collision detector for a board with the given number of columns.
     * 
     * @param columns the number of columns in the board
     */
    public CollisionDetector(int columns) {
        this.columns = columns;
    }
    
    /**
     * Returns true if placing the given shape at offsetX keeps all non-empty cells
     * inside the horizontal board bounds [0, columns - 1].
     * 
     * @param shape the brick shape matrix
     * @param offsetX the X coordinate where the shape would be placed
     * @return true if the shape is within horizontal bounds, false otherwise
     */
    public boolean isWithinHorizontalBounds(int[][] shape, int offsetX) {
        int minLocalX = Integer.MAX_VALUE;
        int maxLocalX = Integer.MIN_VALUE;

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] != 0) {
                    if (col < minLocalX) {
                        minLocalX = col;
                    }
                    if (col > maxLocalX) {
                        maxLocalX = col;
                    }
                }
            }
        }

        // Empty shape (should not happen), treat as in bounds.
        if (minLocalX == Integer.MAX_VALUE) {
            return true;
        }

        int left = offsetX + minLocalX;
        int right = offsetX + maxLocalX;

        return left >= 0 && right < columns;
    }
    
    /**
     * Checks if placing a shape at the given position would cause a collision
     * with existing blocks in the board matrix.
     * 
     * @param boardMatrix the current board state
     * @param shape the brick shape matrix to check
     * @param offsetX the X coordinate
     * @param offsetY the Y coordinate
     * @return true if there's a collision, false if placement is valid
     */
    public boolean hasCollision(int[][] boardMatrix, int[][] shape, int offsetX, int offsetY) {
        return MatrixOperations.intersect(boardMatrix, shape, offsetX, offsetY);
    }
}


package com.comp2042;

/**
 * Represents the result of clearing lines from the board.
 * Contains information about how many lines were removed, the new board state,
 * and the score bonus awarded.
 */
public final class ClearRow {

    private final int linesRemoved;
    private final int[][] newMatrix;
    private final int scoreBonus;

    /**
     * Creates a new ClearRow result.
     *
     * @param linesRemoved the number of lines that were cleared
     * @param newMatrix the board matrix after clearing lines
     * @param scoreBonus the score bonus awarded for clearing these lines
     */
    public ClearRow(int linesRemoved, int[][] newMatrix, int scoreBonus) {
        this.linesRemoved = linesRemoved;
        this.newMatrix = newMatrix;
        this.scoreBonus = scoreBonus;
    }

    /**
     * Gets the number of lines that were removed.
     *
     * @return the number of lines cleared
     */
    public int getLinesRemoved() {
        return linesRemoved;
    }

    /**
     * Gets a defensive copy of the board matrix after clearing lines.
     *
     * @return a copy of the new board matrix
     */
    public int[][] getNewMatrix() {
        return MatrixOperations.copy(newMatrix);
    }

    /**
     * Gets the score bonus awarded for clearing these lines.
     *
     * @return the score bonus points
     */
    public int getScoreBonus() {
        return scoreBonus;
    }
}

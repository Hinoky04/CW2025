package com.comp2042;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Tracks score, level, total cleared lines and combo.
 *
 * Level:
 *  - Increases every LINES_PER_LEVEL lines, up to MAX_LEVEL.
 *
 * Combo:
 *  - Each landing that clears at least one line increases combo by 1 (up to MAX_COMBO).
 *  - Score bonus for that clear is multiplied by the current combo.
 *  - A landing with no clear resets combo to 0.
 */
public final class Score {

    private static final int LINES_PER_LEVEL = 10;
    private static final int MAX_LEVEL = 10;
    private static final int MAX_COMBO = 4;

    private final IntegerProperty score = new SimpleIntegerProperty(0);
    private final IntegerProperty level = new SimpleIntegerProperty(1);
    private final IntegerProperty totalLines = new SimpleIntegerProperty(0);
    private final IntegerProperty combo = new SimpleIntegerProperty(0);

    /**
     * Gets the score property for JavaFX binding.
     *
     * @return the score IntegerProperty
     */
    public IntegerProperty scoreProperty() {
        return score;
    }

    /**
     * Gets the level property for JavaFX binding.
     *
     * @return the level IntegerProperty
     */
    public IntegerProperty levelProperty() {
        return level;
    }

    /**
     * Gets the total lines property for JavaFX binding.
     *
     * @return the total lines IntegerProperty
     */
    public IntegerProperty totalLinesProperty() {
        return totalLines;
    }

    /**
     * Gets the combo property for JavaFX binding.
     *
     * @return the combo IntegerProperty
     */
    public IntegerProperty comboProperty() {
        return combo;
    }

    /**
     * Gets the current level.
     *
     * @return the current level (1-10)
     */
    public int getLevel() {
        return level.get();
    }

    /**
     * Gets the total number of lines cleared.
     *
     * @return the total lines cleared
     */
    public int getTotalLines() {
        return totalLines.get();
    }

    /**
     * Gets the current combo multiplier.
     *
     * @return the combo value (0-4)
     */
    public int getCombo() {
        return combo.get();
    }

    /**
     * Adds a raw score bonus (used for soft drop, hard drop, rotation, etc.).
     * Does not affect combo or level.
     *
     * @param points the number of points to add
     */
    public void add(int points) {
        score.set(score.get() + points);
    }

    /**
     * Awards points for rotating a piece.
     */
    public void addRotationScore() {
        add(5); // Small bonus for rotating
    }

    /**
     * Awards points for moving a piece horizontally.
     */
    public void addMoveScore() {
        add(1); // Small bonus for moving left/right
    }

    /**
     * Awards points for hard dropping a piece.
     * @param cellsDropped number of cells the piece dropped
     */
    public void addHardDropScore(int cellsDropped) {
        add(cellsDropped * 2); // 2 points per cell for hard drop
    }

    /**
     * Awards points for holding a piece.
     */
    public void addHoldScore() {
        add(3); // Small bonus for using hold strategically
    }

    /**
     * Registers that some lines were cleared on a landing and applies:
     * total line count, combo multiplier, and level progression.
     *
     * @param linesRemoved number of lines cleared by this landing
     * @param scoreBonus base score reward for this clear (before combo multiplier)
     */
    public void registerLinesCleared(int linesRemoved, int scoreBonus) {
        if (linesRemoved <= 0) {
            // Defensive: treat this as a non-clear landing.
            resetComboInternal();
            return;
        }

        // Track total cleared lines across the whole game.
        totalLines.set(totalLines.get() + linesRemoved);

        // Combo: each consecutive clear increases combo, capped at MAX_COMBO.
        int newCombo = combo.get() + 1;
        if (newCombo > MAX_COMBO) {
            newCombo = MAX_COMBO;
        }
        combo.set(newCombo);

        // Apply combo multiplier to the base score bonus.
        int comboMultiplier = combo.get(); // 1x, 2x, 3x, 4x...
        int totalBonus = scoreBonus * comboMultiplier;
        score.set(score.get() + totalBonus);

        // Level progression based on total cleared lines.
        int computedLevel = 1 + totalLines.get() / LINES_PER_LEVEL;
        int targetLevel = Math.min(MAX_LEVEL, computedLevel);

        if (targetLevel > level.get()) {
            level.set(targetLevel);
        }
    }

    /**
     * Called when a brick lands but no lines are cleared.
     * Resets the combo chain to zero.
     */
    public void registerLandingWithoutClear() {
        resetComboInternal();
    }

    private void resetComboInternal() {
        combo.set(0);
    }

    /**
     * Resets all score-related values to their initial state.
     * Sets score to 0, level to 1, total lines to 0, and combo to 0.
     */
    public void reset() {
        score.set(0);
        level.set(1);
        totalLines.set(0);
        combo.set(0);
    }
}

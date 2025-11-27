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

    public IntegerProperty scoreProperty() {
        return score;
    }

    public IntegerProperty levelProperty() {
        return level;
    }

    public IntegerProperty totalLinesProperty() {
        return totalLines;
    }

    public IntegerProperty comboProperty() {
        return combo;
    }

    public int getLevel() {
        return level.get();
    }

    public int getTotalLines() {
        return totalLines.get();
    }

    public int getCombo() {
        return combo.get();
    }

    /**
     * Adds a raw score bonus (used for soft drop etc.).
     * Does not affect combo or level.
     */
    public void add(int points) {
        score.set(score.get() + points);
    }

    /**
     * Registers that some lines were cleared on a landing and applies:
     *  - total line count
     *  - combo multiplier
     *  - level progression
     *
     * @param linesRemoved number of lines cleared by this landing
     * @param scoreBonus   base score reward for this clear (before combo)
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
     * Breaks the combo chain.
     */
    public void registerLandingWithoutClear() {
        resetComboInternal();
    }

    private void resetComboInternal() {
        combo.set(0);
    }

    public void reset() {
        score.set(0);
        level.set(1);
        totalLines.set(0);
        combo.set(0);
    }
}

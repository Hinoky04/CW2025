package com.comp2042;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Tracks score, level and total cleared lines.
 * Level increases every N lines and will later be bound to the GUI.
 */
public final class Score {

    private static final int LINES_PER_LEVEL = 10;
    private static final int MAX_LEVEL = 10;

    private final IntegerProperty score = new SimpleIntegerProperty(0);
    private final IntegerProperty level = new SimpleIntegerProperty(1);
    private final IntegerProperty totalLines = new SimpleIntegerProperty(0);

    public IntegerProperty scoreProperty() {
        return score;
    }

    public IntegerProperty levelProperty() {
        return level;
    }

    public IntegerProperty totalLinesProperty() {
        return totalLines;
    }

    public int getLevel() {
        return level.get();
    }

    public int getTotalLines() {
        return totalLines.get();
    }

    /**
     * Adds a raw score bonus (used for soft drop etc.).
     */
    public void add(int points) {
        score.set(score.get() + points);
    }

    /**
     * Registers that some lines were cleared and applies score + level logic.
     * This will be called from GameController when rows are removed.
     */
    public void registerLinesCleared(int linesRemoved, int scoreBonus) {
        if (linesRemoved <= 0) {
            return;
        }

        totalLines.set(totalLines.get() + linesRemoved);
        add(scoreBonus);

        int computedLevel = 1 + totalLines.get() / LINES_PER_LEVEL;
        int targetLevel = Math.min(MAX_LEVEL, computedLevel);

        if (targetLevel > level.get()) {
            level.set(targetLevel);
        }
    }

    public void reset() {
        score.set(0);
        level.set(1);
        totalLines.set(0);
    }
}

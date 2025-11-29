package com.comp2042;

/**
 * Immutable configuration for a game mode.
 * Controls drop speed curve and danger zone.
 */
public final class GameConfig {

    // Base fall interval in milliseconds for level 1.
    private final int baseFallIntervalMs;

    // How much faster each level becomes (e.g. 0.15 = +15% per level).
    private final double levelSpeedFactor;

    // How many of the top visible rows count as "danger zone".
    private final int dangerVisibleRows;

    public GameConfig(int baseFallIntervalMs, double levelSpeedFactor, int dangerVisibleRows) {
        this.baseFallIntervalMs = baseFallIntervalMs;
        this.levelSpeedFactor = levelSpeedFactor;
        this.dangerVisibleRows = dangerVisibleRows;
    }

    public int getBaseFallIntervalMs() {
        return baseFallIntervalMs;
    }

    public double getLevelSpeedFactor() {
        return levelSpeedFactor;
    }

    public int getDangerVisibleRows() {
        return dangerVisibleRows;
    }
}

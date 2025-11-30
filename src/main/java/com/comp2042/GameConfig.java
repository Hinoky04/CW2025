package com.comp2042;

/**
 * Immutable configuration for a given GameMode.
 *
 * At Phase 5.4 this is mainly a central place for tuning values.
 * Later phases (Survival garbage, Hyper visibility, Rush-40) will
 * read from this class instead of hard-coding numbers everywhere.
 */
public final class GameConfig {

    // --- Generic tuning knobs ---

    /**
     * Base fall interval in milliseconds (level 1 speed)
     * before any multipliers are applied.
     */
    private final int baseFallIntervalMs;

    /**
     * Multiplier applied to the base drop speed.
     * 1.0 = normal classic speed, >1.0 = faster.
     */
    private final double speedMultiplier;

    /**
     * How much faster each new level becomes.
     * e.g. 0.15 = 15% faster per level.
     */
    private final double levelSpeedFactor;

    /**
     * How many visible rows at the top are considered "danger zone".
     */
    private final int dangerVisibleRows;

    // --- Mode-specific knobs (used in later phases) ---

    /**
     * For Survival: how many consecutive landings without a
     * line clear before we inject a garbage row.
     * 0 or negative means "feature disabled".
     */
    private final int maxNoClearBeforeGarbage;

    /**
     * For Rush-40: how many lines the player must clear to win.
     * 0 or negative means "not a target-lines mode".
     */
    private final int targetLinesToWin;

    private GameConfig(int baseFallIntervalMs,
                       double speedMultiplier,
                       double levelSpeedFactor,
                       int dangerVisibleRows,
                       int maxNoClearBeforeGarbage,
                       int targetLinesToWin) {

        this.baseFallIntervalMs = baseFallIntervalMs;
        this.speedMultiplier = speedMultiplier;
        this.levelSpeedFactor = levelSpeedFactor;
        this.dangerVisibleRows = dangerVisibleRows;
        this.maxNoClearBeforeGarbage = maxNoClearBeforeGarbage;
        this.targetLinesToWin = targetLinesToWin;
    }

    // --- Factory: one config per GameMode ---

    public static GameConfig forMode(GameMode mode) {
        switch (mode) {
            case CLASSIC:
                // Baseline tuning.
                return new GameConfig(
                        400,   // baseFallIntervalMs (ms)
                        1.0,   // speedMultiplier
                        0.15,  // levelSpeedFactor
                        3,     // dangerVisibleRows
                        0,     // maxNoClearBeforeGarbage (off)
                        0      // targetLinesToWin (no target)
                );
            case SURVIVAL:
                // Same basic speed as classic, will later add garbage
                // pressure using maxNoClearBeforeGarbage.
                return new GameConfig(
                        400,
                        1.0,
                        0.15,
                        3,
                        4,     // after 4 non-clearing landings -> garbage
                        0
                );
            case HYPER:
                // Faster, more aggressive; later we will also make
                // background bricks semi-invisible.
                return new GameConfig(
                        350,   // slightly faster base speed
                        1.4,   // speedMultiplier
                        0.20,  // sharper speed curve
                        3,
                        0,
                        0
                );
            case RUSH_40:
                // Classic-like feel but with a 40-line goal.
                return new GameConfig(
                        400,
                        1.0,
                        0.15,
                        3,
                        0,
                        40    // clear 40 lines to win
                );
            default:
                throw new IllegalArgumentException("Unknown mode: " + mode);
        }
    }

    // --- Getters (used by GameController / GuiController) ---

    /**
     * Matches existing code that calls config.getBaseFallIntervalMs().
     */
    public int getBaseFallIntervalMs() {
        return baseFallIntervalMs;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public double getLevelSpeedFactor() {
        return levelSpeedFactor;
    }

    public int getDangerVisibleRows() {
        return dangerVisibleRows;
    }

    public int getMaxNoClearBeforeGarbage() {
        return maxNoClearBeforeGarbage;
    }

    public int getTargetLinesToWin() {
        return targetLinesToWin;
    }
}

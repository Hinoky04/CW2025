package com.comp2042.models;

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

    /**
     * For Hyper: how much to dim landed blocks in the background.
     * 1.0 = normal brightness; values between 0 and 1 make blocks darker.
     */
    private final double backgroundDimFactor;

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

    /**
     * Whether this mode should display a running timer in the HUD.
     */
    private final boolean showTimer;

    private GameConfig(int baseFallIntervalMs,
                       double speedMultiplier,
                       double levelSpeedFactor,
                       int dangerVisibleRows,
                       double backgroundDimFactor,
                       int maxNoClearBeforeGarbage,
                       int targetLinesToWin,
                       boolean showTimer) {

        this.baseFallIntervalMs = baseFallIntervalMs;
        this.speedMultiplier = speedMultiplier;
        this.levelSpeedFactor = levelSpeedFactor;
        this.dangerVisibleRows = dangerVisibleRows;
        this.backgroundDimFactor = backgroundDimFactor;
        this.maxNoClearBeforeGarbage = maxNoClearBeforeGarbage;
        this.targetLinesToWin = targetLinesToWin;
        this.showTimer = showTimer;
    }

    // --- Factory: one config per GameMode ---

    public static GameConfig forMode(GameMode mode) {
        switch (mode) {
            case CLASSIC:
                // Baseline tuning. Timer shows elapsed time.
                return new GameConfig(
                        400,   // baseFallIntervalMs (ms)
                        1.0,   // speedMultiplier
                        0.15,  // levelSpeedFactor
                        3,     // dangerVisibleRows
                        1.0,   // backgroundDimFactor (no dimming)
                        0,     // maxNoClearBeforeGarbage (off)
                        0,     // targetLinesToWin (no target)
                        true   // showTimer
                );
            case SURVIVAL:
                // Same basic speed as classic, with garbage pressure using
                // maxNoClearBeforeGarbage. Timer shows how long you survived.
                return new GameConfig(
                        400,
                        1.0,
                        0.15,
                        3,
                        1.0,   // backgroundDimFactor (no dimming)
                        4,     // after 4 non-clearing landings -> garbage
                        0,
                        true   // showTimer
                );
            case HYPER:
                // Faster, more aggressive; background will be drawn dimmer
                // using backgroundDimFactor to make stacking harder to read.
                // Timer shows how long you can survive the chaos.
                return new GameConfig(
                        350,   // slightly faster base speed
                        1.4,   // speedMultiplier
                        0.20,  // sharper speed curve
                        3,
                        0.35,  // backgroundDimFactor (landed blocks are dimmed)
                        0,
                        0,
                        true   // showTimer
                );
            case RUSH_40:
                // Classic-like feel but with a 40-line goal.
                // Timer is critical here to measure completion time.
                return new GameConfig(
                        400,
                        1.0,
                        0.15,
                        3,
                        1.0,   // backgroundDimFactor (no dimming)
                        0,
                        40,    // clear 40 lines to win
                        true   // showTimer
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

    public double getBackgroundDimFactor() {
        return backgroundDimFactor;
    }

    public int getMaxNoClearBeforeGarbage() {
        return maxNoClearBeforeGarbage;
    }

    public int getTargetLinesToWin() {
        return targetLinesToWin;
    }

    public boolean isShowTimer() {
        return showTimer;
    }
}

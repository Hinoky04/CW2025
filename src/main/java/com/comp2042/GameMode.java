package com.comp2042;

/**
 * Supported game modes for TetrisJFX.
 * Classic and Survival currently share the same rules
 * but use different GameConfig values (speed curve, danger zone, etc.).
 */
public enum GameMode {

    CLASSIC("Classic"),
    SURVIVAL("Survival");

    private final String displayName;

    GameMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Configuration for this mode.
     */
    public GameConfig getConfig() {
        switch (this) {
            case CLASSIC:
                // Very close to your original behaviour.
                return new GameConfig(
                        400,   // base fall interval (ms)
                        0.15,  // gentle speed-up per level
                        3      // top 3 rows = danger
                );
            case SURVIVAL:
                // Harder, faster mode.
                return new GameConfig(
                        300,   // faster base fall
                        0.25,  // more aggressive speed-up
                        5      // larger danger zone
                );
            default:
                throw new IllegalStateException("Unknown mode: " + this);
        }
    }
}

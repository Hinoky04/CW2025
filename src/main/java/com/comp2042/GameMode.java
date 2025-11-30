package com.comp2042;

/**
 * High-level game modes available from the main menu.
 * Each mode can expose its own GameConfig for tuning.
 */
public enum GameMode {

    CLASSIC("Classic"),
    SURVIVAL("Survival"),
    HYPER("Hyper"),
    RUSH_40("Rush 40");

    private final String displayName;

    GameMode(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Short name used in menus / HUD.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convenience helper so existing code that calls
     * gameMode.getConfig() keeps working.
     *
     * Internally this just delegates to GameConfig.forMode(...).
     */
    public GameConfig getConfig() {
        return GameConfig.forMode(this);
    }
}

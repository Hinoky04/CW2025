package com.comp2042.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GameConfig class.
 * Verifies that configuration values are correctly set for each game mode.
 */
class GameConfigTest {

    @Test
    void testClassicModeConfig() {
        GameConfig config = GameConfig.forMode(GameMode.CLASSIC);
        
        assertEquals(400, config.getBaseFallIntervalMs());
        assertEquals(1.0, config.getSpeedMultiplier(), 0.001);
        assertEquals(0.15, config.getLevelSpeedFactor(), 0.001);
        assertEquals(3, config.getDangerVisibleRows());
        assertEquals(1.0, config.getBackgroundDimFactor(), 0.001);
        assertEquals(0, config.getMaxNoClearBeforeGarbage());
        assertEquals(0, config.getTargetLinesToWin());
        assertTrue(config.isShowTimer());
    }

    @Test
    void testSurvivalModeConfig() {
        GameConfig config = GameConfig.forMode(GameMode.SURVIVAL);
        
        assertEquals(400, config.getBaseFallIntervalMs());
        assertEquals(1.0, config.getSpeedMultiplier(), 0.001);
        assertEquals(0.15, config.getLevelSpeedFactor(), 0.001);
        assertEquals(3, config.getDangerVisibleRows());
        assertEquals(1.0, config.getBackgroundDimFactor(), 0.001);
        assertEquals(4, config.getMaxNoClearBeforeGarbage());
        assertEquals(0, config.getTargetLinesToWin());
        assertTrue(config.isShowTimer());
    }

    @Test
    void testHyperModeConfig() {
        GameConfig config = GameConfig.forMode(GameMode.HYPER);
        
        assertEquals(350, config.getBaseFallIntervalMs());
        assertEquals(1.4, config.getSpeedMultiplier(), 0.001);
        assertEquals(0.20, config.getLevelSpeedFactor(), 0.001);
        assertEquals(3, config.getDangerVisibleRows());
        assertEquals(0.35, config.getBackgroundDimFactor(), 0.001);
        assertEquals(0, config.getMaxNoClearBeforeGarbage());
        assertEquals(0, config.getTargetLinesToWin());
        assertTrue(config.isShowTimer());
    }

    @Test
    void testRush40ModeConfig() {
        GameConfig config = GameConfig.forMode(GameMode.RUSH_40);
        
        assertEquals(400, config.getBaseFallIntervalMs());
        assertEquals(1.0, config.getSpeedMultiplier(), 0.001);
        assertEquals(0.15, config.getLevelSpeedFactor(), 0.001);
        assertEquals(3, config.getDangerVisibleRows());
        assertEquals(1.0, config.getBackgroundDimFactor(), 0.001);
        assertEquals(0, config.getMaxNoClearBeforeGarbage());
        assertEquals(40, config.getTargetLinesToWin());
        assertTrue(config.isShowTimer());
    }

    @Test
    void testAllValidModesWork() {
        // Test that all valid modes return valid configs
        assertDoesNotThrow(() -> GameConfig.forMode(GameMode.CLASSIC));
        assertDoesNotThrow(() -> GameConfig.forMode(GameMode.SURVIVAL));
        assertDoesNotThrow(() -> GameConfig.forMode(GameMode.HYPER));
        assertDoesNotThrow(() -> GameConfig.forMode(GameMode.RUSH_40));
    }

    @Test
    void testConfigImmutability() {
        // Verify that configs for the same mode return consistent values
        GameConfig config1 = GameConfig.forMode(GameMode.CLASSIC);
        GameConfig config2 = GameConfig.forMode(GameMode.CLASSIC);
        
        // They should have the same values (but may be different instances)
        assertEquals(config1.getBaseFallIntervalMs(), config2.getBaseFallIntervalMs());
        assertEquals(config1.getSpeedMultiplier(), config2.getSpeedMultiplier(), 0.001);
        assertEquals(config1.getLevelSpeedFactor(), config2.getLevelSpeedFactor(), 0.001);
    }

    @Test
    void testAllModesHaveValidConfigs() {
        // Test that all enum values have valid configurations
        for (GameMode mode : GameMode.values()) {
            assertDoesNotThrow(() -> {
                GameConfig config = GameConfig.forMode(mode);
                assertNotNull(config);
                assertTrue(config.getBaseFallIntervalMs() > 0);
                assertTrue(config.getSpeedMultiplier() > 0);
                assertTrue(config.getLevelSpeedFactor() >= 0);
                assertTrue(config.getDangerVisibleRows() >= 0);
            });
        }
    }
}

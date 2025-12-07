package com.comp2042.ui;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import com.comp2042.models.GameMode;

/**
 * Manages color and styling for game elements.
 * Handles color mapping, dimming, and mode-specific visual effects.
 */
public class ColorManager {
    
    private final GameMode currentMode;
    
    public ColorManager(GameMode currentMode) {
        this.currentMode = currentMode;
    }
    
    /**
     * Gets the base fill color for a given value.
     */
    public Paint getFillColor(int value) {
        switch (value) {
            case 0:
                return Color.TRANSPARENT;
            case 1:
                return Color.AQUA;
            case 2:
                return Color.BLUEVIOLET;
            case 3:
                return Color.DARKGREEN;
            case 4:
                return Color.YELLOW;
            case 5:
                return Color.RED;
            case 6:
                return Color.BEIGE;
            case 7:
                return Color.BURLYWOOD;
            default:
                return Color.WHITE;
        }
    }
    
    /**
     * Applies a dimming factor to a color.
     */
    public Paint applyDimFactor(Paint base, double factor) {
        if (!(base instanceof Color)) {
            return base;
        }
        Color c = (Color) base;
        double r = c.getRed() * factor;
        double g = c.getGreen() * factor;
        double b = c.getBlue() * factor;
        return new Color(
                clamp01(r),
                clamp01(g),
                clamp01(b),
                c.getOpacity()
        );
    }
    
    private double clamp01(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }
    
    /**
     * Gets the color for the currently falling brick (always full brightness).
     */
    public Paint getActiveBrickFillColor(int value) {
        if (value == 0) {
            return Color.TRANSPARENT;
        }
        return getFillColor(value);
    }
    
    /**
     * Gets the color for settled blocks in the background.
     * Classic / Survival / Rush-40: landed blocks are drawn with normal colours.
     * Hyper: landed blocks are fully transparent.
     */
    public Paint getBackgroundFillColor(int value) {
        if (value == 0) {
            return Color.TRANSPARENT;
        }
        
        Paint base = getFillColor(value);
        
        if (currentMode == GameMode.HYPER) {
            return Color.TRANSPARENT;
        }
        return base;
    }
    
    /**
     * Gets the color for the ghost/shadow piece.
     * Uses semi-transparent fill with an outline to make it visually distinct.
     */
    public Paint getGhostFillColor(int colorCode) {
        if (colorCode == 0) {
            return Color.TRANSPARENT;
        }
        Paint baseColor = getFillColor(colorCode);
        // Make it semi-transparent (30% opacity)
        if (baseColor instanceof Color) {
            Color c = (Color) baseColor;
            return new Color(
                    c.getRed(),
                    c.getGreen(),
                    c.getBlue(),
                    0.3  // 30% opacity
            );
        }
        return baseColor;
    }
}


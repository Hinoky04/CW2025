package com.comp2042.helpers;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import com.comp2042.models.GameMode;

/**
 * Helper class for color and styling logic extracted from GuiController.
 * Contains exact same code, just moved to separate file.
 */
public class GuiColorHelper {
    
    private final GameMode currentMode;
    private boolean gameOver = false;
    
    /**
     * Creates a new color helper for the specified game mode.
     *
     * @param currentMode the current game mode (affects color rendering)
     */
    public GuiColorHelper(GameMode currentMode) {
        this.currentMode = currentMode;
    }
    
    /**
     * Sets whether the game is over. When true, blocks become visible even in Invisible mode.
     *
     * @param gameOver true if game is over, false otherwise
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
    
    /**
     * Gets the base fill color for a given value.
     *
     * @param value the color code (0-7)
     * @return the paint color for the value
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
     *
     * @param base the base color to dim
     * @param factor the dimming factor (0.0-1.0)
     * @return the dimmed color
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
     *
     * @param value the color code (0-7)
     * @return the paint color for the active brick
     */
    public Paint getActiveBrickFillColor(int value) {
        if (value == 0) {
            return Color.TRANSPARENT;
        }
        return getFillColor(value);
    }

    /**
     * Colour for settled blocks in the background.
     *
     * Classic / Survival / Rush-40: landed blocks are drawn with normal colours.
     * Hyper/Invisible: landed blocks are fully transparent during gameplay; they still exist in the matrix for
     * collision / line clear, but visually "merge into" the empty background.
     * When game is over, all blocks become visible even in Invisible mode.
     *
     * @param value the color code (0-7)
     * @return the paint color for the background
     */
    public Paint getBackgroundFillColor(int value) {
        if (value == 0) {
            return Color.TRANSPARENT;
        }

        Paint base = getFillColor(value);

        // In Invisible mode, blocks are transparent during gameplay, but visible after game over
        if (currentMode == GameMode.HYPER && !gameOver) {
            return Color.TRANSPARENT;
        }
        return base;
    }

    /**
     * Draw a single cell of the active falling layer (no background).
     *
     * @param colorCode the color code (0-7)
     * @param rectangle the rectangle to style
     */
    public void setRectangleData(int colorCode, Rectangle rectangle) {
        rectangle.setFill(getActiveBrickFillColor(colorCode));
        rectangle.setArcHeight(9);
        rectangle.setArcWidth(9);
    }

    /**
     * Draw a single cell of the background matrix.
     *
     * @param colorCode the color code (0-7)
     * @param rectangle the rectangle to style
     */
    public void setBackgroundRectangleData(int colorCode, Rectangle rectangle) {
        rectangle.setFill(getBackgroundFillColor(colorCode));
        rectangle.setArcHeight(9);
        rectangle.setArcWidth(9);
    }

    /**
     * Draw a single cell of the ghost/shadow layer.
     * Uses semi-transparent fill with an outline to make it visually distinct.
     *
     * @param colorCode the color code (0-7)
     * @param rectangle the rectangle to style
     */
    public void setGhostRectangleData(int colorCode, Rectangle rectangle) {
        if (colorCode == 0) {
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setStroke(null);
        } else {
            Paint baseColor = getFillColor(colorCode);
            // Make it semi-transparent (30% opacity)
            if (baseColor instanceof Color) {
                Color c = (Color) baseColor;
                Color ghostColor = new Color(
                        c.getRed(),
                        c.getGreen(),
                        c.getBlue(),
                        0.3  // 30% opacity
                );
                rectangle.setFill(ghostColor);
            } else {
                rectangle.setFill(baseColor);
            }
            // Add a subtle outline to make it more visible
            rectangle.setStroke(Color.WHITE);
            rectangle.setStrokeWidth(1.5);
            rectangle.setArcHeight(9);
            rectangle.setArcWidth(9);
        }
    }
    
    public void setCurrentMode(GameMode mode) {
        // Note: This requires recreating the helper, but keeping interface simple
    }
}


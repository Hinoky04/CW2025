package com.comp2042.models;

import javafx.scene.input.KeyCode;

import java.io.*;
import java.util.Properties;

/**
 * Manages game settings including key bindings.
 * Settings are persisted to a properties file.
 */
public class GameSettings {
    
    private static final String SETTINGS_FILE = "tetris_settings.properties";
    
    // Default key bindings
    private KeyCode moveLeft = KeyCode.LEFT;
    private KeyCode moveRight = KeyCode.RIGHT;
    private KeyCode rotate = KeyCode.UP;
    private KeyCode softDrop = KeyCode.DOWN;
    private KeyCode hardDrop = KeyCode.SPACE;
    private KeyCode hold = KeyCode.C;
    private KeyCode pause = KeyCode.P;
    private KeyCode pauseAlt = KeyCode.ESCAPE;
    private KeyCode restart = KeyCode.N;
    
    private static GameSettings instance;
    
    private GameSettings() {
        loadSettings();
    }
    
    public static GameSettings getInstance() {
        if (instance == null) {
            instance = new GameSettings();
        }
        return instance;
    }
    
    /**
     * Loads settings from the properties file, or uses defaults if file doesn't exist.
     */
    public void loadSettings() {
        Properties props = new Properties();
        File settingsFile = new File(SETTINGS_FILE);
        
        if (settingsFile.exists()) {
            try (FileInputStream fis = new FileInputStream(settingsFile)) {
                props.load(fis);
                
                moveLeft = KeyCode.valueOf(props.getProperty("moveLeft", "LEFT"));
                moveRight = KeyCode.valueOf(props.getProperty("moveRight", "RIGHT"));
                rotate = KeyCode.valueOf(props.getProperty("rotate", "UP"));
                softDrop = KeyCode.valueOf(props.getProperty("softDrop", "DOWN"));
                hardDrop = KeyCode.valueOf(props.getProperty("hardDrop", "SPACE"));
                hold = KeyCode.valueOf(props.getProperty("hold", "C"));
                pause = KeyCode.valueOf(props.getProperty("pause", "P"));
                pauseAlt = KeyCode.valueOf(props.getProperty("pauseAlt", "ESCAPE"));
                restart = KeyCode.valueOf(props.getProperty("restart", "N"));
            } catch (IOException e) {
                System.err.println("Failed to load settings: " + e.getMessage());
                // Use defaults
            }
        }
    }
    
    /**
     * Saves current settings to the properties file.
     */
    public void saveSettings() {
        Properties props = new Properties();
        props.setProperty("moveLeft", moveLeft.name());
        props.setProperty("moveRight", moveRight.name());
        props.setProperty("rotate", rotate.name());
        props.setProperty("softDrop", softDrop.name());
        props.setProperty("hardDrop", hardDrop.name());
        props.setProperty("hold", hold.name());
        props.setProperty("pause", pause.name());
        props.setProperty("pauseAlt", pauseAlt.name());
        props.setProperty("restart", restart.name());
        
        try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
            props.store(fos, "TetrisJFX Game Settings");
        } catch (IOException e) {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }
    
    /**
     * Gets the key code for moving left.
     *
     * @return the KeyCode for left movement
     */
    public KeyCode getMoveLeft() { return moveLeft; }
    
    /**
     * Sets the key code for moving left.
     *
     * @param key the KeyCode to set
     */
    public void setMoveLeft(KeyCode key) { this.moveLeft = key; }
    
    /**
     * Gets the key code for moving right.
     *
     * @return the KeyCode for right movement
     */
    public KeyCode getMoveRight() { return moveRight; }
    
    /**
     * Sets the key code for moving right.
     *
     * @param key the KeyCode to set
     */
    public void setMoveRight(KeyCode key) { this.moveRight = key; }
    
    /**
     * Gets the key code for rotating.
     *
     * @return the KeyCode for rotation
     */
    public KeyCode getRotate() { return rotate; }
    
    /**
     * Sets the key code for rotating.
     *
     * @param key the KeyCode to set
     */
    public void setRotate(KeyCode key) { this.rotate = key; }
    
    /**
     * Gets the key code for soft drop.
     *
     * @return the KeyCode for soft drop
     */
    public KeyCode getSoftDrop() { return softDrop; }
    
    /**
     * Sets the key code for soft drop.
     *
     * @param key the KeyCode to set
     */
    public void setSoftDrop(KeyCode key) { this.softDrop = key; }
    
    /**
     * Gets the key code for hard drop.
     *
     * @return the KeyCode for hard drop
     */
    public KeyCode getHardDrop() { return hardDrop; }
    
    /**
     * Sets the key code for hard drop.
     *
     * @param key the KeyCode to set
     */
    public void setHardDrop(KeyCode key) { this.hardDrop = key; }
    
    /**
     * Gets the key code for hold/swap.
     *
     * @return the KeyCode for hold
     */
    public KeyCode getHold() { return hold; }
    
    /**
     * Sets the key code for hold/swap.
     *
     * @param key the KeyCode to set
     */
    public void setHold(KeyCode key) { this.hold = key; }
    
    /**
     * Gets the primary key code for pause.
     *
     * @return the KeyCode for pause
     */
    public KeyCode getPause() { return pause; }
    
    /**
     * Sets the primary key code for pause.
     *
     * @param key the KeyCode to set
     */
    public void setPause(KeyCode key) { this.pause = key; }
    
    /**
     * Gets the alternate key code for pause.
     *
     * @return the KeyCode for alternate pause
     */
    public KeyCode getPauseAlt() { return pauseAlt; }
    
    /**
     * Sets the alternate key code for pause.
     *
     * @param key the KeyCode to set
     */
    public void setPauseAlt(KeyCode key) { this.pauseAlt = key; }
    
    /**
     * Gets the key code for restart.
     *
     * @return the KeyCode for restart
     */
    public KeyCode getRestart() { return restart; }
    
    /**
     * Sets the key code for restart.
     *
     * @param key the KeyCode to set
     */
    public void setRestart(KeyCode key) { this.restart = key; }
    
    /**
     * Resets all key bindings to their default values.
     * Does not automatically save; call saveSettings() to persist changes.
     */
    public void resetToDefaults() {
        moveLeft = KeyCode.LEFT;
        moveRight = KeyCode.RIGHT;
        rotate = KeyCode.UP;
        softDrop = KeyCode.DOWN;
        hardDrop = KeyCode.SPACE;
        hold = KeyCode.C;
        pause = KeyCode.P;
        pauseAlt = KeyCode.ESCAPE;
        restart = KeyCode.N;
    }
}


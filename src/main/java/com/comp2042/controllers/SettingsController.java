package com.comp2042.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

import com.comp2042.Main;
import com.comp2042.models.GameMode;
import com.comp2042.models.GameSettings;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Controller for the settings page.
 * Handles key binding configuration.
 */
public class SettingsController {
    
    private Main mainApp;
    private GameSettings settings;
    private Button currentButtonBeingEdited = null;
    private GameMode returnToGameMode = null; // If set, return to game instead of main menu
    
    // Key binding buttons
    @FXML private Button moveLeftButton;
    @FXML private Button moveRightButton;
    @FXML private Button rotateButton;
    @FXML private Button softDropButton;
    @FXML private Button hardDropButton;
    @FXML private Button holdButton;
    @FXML private Button pauseButton;
    @FXML private Button restartButton;
    
    // Action buttons
    @FXML private Button resetButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    @FXML
    private StackPane rootPane;
    
    /**
     * Called by Main after loading the FXML.
     * @param mainApp the main application
     * @param returnToGameMode if not null, return to this game mode after saving/canceling
     */
    public void init(Main mainApp, GameMode returnToGameMode) {
        this.mainApp = mainApp;
        this.returnToGameMode = returnToGameMode;
        this.settings = GameSettings.getInstance();
        loadSettingsToUI();
        setupKeyListeners();
        
        // Make sure the root pane can receive focus for key events
        if (rootPane != null) {
            rootPane.setFocusTraversable(true);
            rootPane.requestFocus();
        }
    }
    
    /**
     * Overloaded init method for backward compatibility (from main menu).
     */
    public void init(Main mainApp) {
        init(mainApp, null);
    }
    
    /**
     * Loads current settings and displays them in the UI.
     */
    private void loadSettingsToUI() {
        moveLeftButton.setText(settings.getMoveLeft().getName());
        moveRightButton.setText(settings.getMoveRight().getName());
        rotateButton.setText(settings.getRotate().getName());
        softDropButton.setText(settings.getSoftDrop().getName());
        hardDropButton.setText(settings.getHardDrop().getName());
        holdButton.setText(settings.getHold().getName());
        pauseButton.setText(settings.getPause().getName());
        restartButton.setText(settings.getRestart().getName());
    }
    
    /**
     * Sets up click handlers for all key binding buttons.
     */
    private void setupKeyListeners() {
        moveLeftButton.setOnAction(e -> startEditingKey(moveLeftButton, "moveLeft"));
        moveRightButton.setOnAction(e -> startEditingKey(moveRightButton, "moveRight"));
        rotateButton.setOnAction(e -> startEditingKey(rotateButton, "rotate"));
        softDropButton.setOnAction(e -> startEditingKey(softDropButton, "softDrop"));
        hardDropButton.setOnAction(e -> startEditingKey(hardDropButton, "hardDrop"));
        holdButton.setOnAction(e -> startEditingKey(holdButton, "hold"));
        pauseButton.setOnAction(e -> startEditingKey(pauseButton, "pause"));
        restartButton.setOnAction(e -> startEditingKey(restartButton, "restart"));
    }
    
    /**
     * Starts editing mode for a key binding button.
     * The next key pressed will be assigned to this action.
     */
    private void startEditingKey(Button button, String actionName) {
        // Reset previous button if any
        if (currentButtonBeingEdited != null && currentButtonBeingEdited != button) {
            currentButtonBeingEdited.setStyle("");
        }
        
        currentButtonBeingEdited = button;
        button.setText("Press any key...");
        button.setStyle("-fx-background-color: #ffaa00; -fx-text-fill: white;");
        
        // Request focus on root pane to capture all key events including arrow keys
        if (rootPane != null) {
            rootPane.requestFocus();
        }
        
        // Set up a one-time key listener on the scene using addEventFilter
        // This captures keys even when focus is on buttons, including arrow keys
        if (button.getScene() != null) {
            final Button targetButton = button;
            final String targetAction = actionName;
            
            // Use AtomicReference to allow the handler to reference itself
            final AtomicReference<EventHandler<KeyEvent>> handlerRef = 
                new AtomicReference<>();
            
            EventHandler<KeyEvent> keyHandler = event -> {
                if (currentButtonBeingEdited == targetButton) {
                    KeyCode newKey = event.getCode();
                    
                    // Skip if it's ESC (cancel editing)
                    if (newKey == KeyCode.ESCAPE) {
                        loadSettingsToUI();
                        targetButton.setStyle("");
                        currentButtonBeingEdited = null;
                        if (targetButton.getScene() != null && handlerRef.get() != null) {
                            targetButton.getScene().removeEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, handlerRef.get());
                        }
                        event.consume();
                        return;
                    }
                    
                    // Update the setting
                    switch (targetAction) {
                        case "moveLeft": settings.setMoveLeft(newKey); break;
                        case "moveRight": settings.setMoveRight(newKey); break;
                        case "rotate": settings.setRotate(newKey); break;
                        case "softDrop": settings.setSoftDrop(newKey); break;
                        case "hardDrop": settings.setHardDrop(newKey); break;
                        case "hold": settings.setHold(newKey); break;
                        case "pause": settings.setPause(newKey); break;
                        case "restart": settings.setRestart(newKey); break;
                    }
                    
                    // Update UI
                    targetButton.setText(newKey.getName());
                    targetButton.setStyle("");
                    currentButtonBeingEdited = null;
                    
                    // Remove the listener
                    if (targetButton.getScene() != null && handlerRef.get() != null) {
                        targetButton.getScene().removeEventFilter(KeyEvent.KEY_PRESSED, handlerRef.get());
                    }
                    
                    event.consume();
                }
            };
            
            handlerRef.set(keyHandler);
            button.getScene().addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        }
    }
    
    @FXML
    private void handleReset(ActionEvent event) {
        settings.resetToDefaults();
        loadSettingsToUI();
    }
    
    @FXML
    private void handleSave(ActionEvent event) {
        settings.saveSettings();
        if (mainApp != null) {
            if (returnToGameMode != null) {
                // Return to the paused game
                mainApp.showGameScene(returnToGameMode);
            } else {
                // Return to main menu
                mainApp.showMainMenu();
            }
        }
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        // Reload settings to discard changes
        settings.loadSettings();
        if (mainApp != null) {
            if (returnToGameMode != null) {
                // Return to the paused game
                mainApp.showGameScene(returnToGameMode);
            } else {
                // Return to main menu
                mainApp.showMainMenu();
            }
        }
    }
}


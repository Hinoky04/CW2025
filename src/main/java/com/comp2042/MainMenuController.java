package com.comp2042;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Controller for the main menu screen.
 * Responsible only for menu actions (choose mode, quit).
 */
public class MainMenuController {

    // Reference to the Main application so we can switch scenes.
    private Main mainApp;

    /**
     * Called by Main after loading the FXML.
     * We do not own the Stage, we just ask Main to switch scenes.
     */
    void init(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private Label titleLabel;

    @FXML
    private Button classicButton;

    @FXML
    private Button survivalButton;

    @FXML
    private Button hyperButton;

    @FXML
    private Button rushButton;

    @FXML
    private Button quitButton;

    // --- Button handlers ---

    @FXML
    private void handleClassic(ActionEvent event) {
        if (mainApp != null) {
            mainApp.showGameScene(GameMode.CLASSIC);
        }
    }

    @FXML
    private void handleSurvival(ActionEvent event) {
        if (mainApp != null) {
            mainApp.showGameScene(GameMode.SURVIVAL);
        }
    }

    @FXML
    private void handleHyper(ActionEvent event) {
        if (mainApp != null) {
            mainApp.showGameScene(GameMode.HYPER);
        }
    }

    @FXML
    private void handleRush(ActionEvent event) {
        if (mainApp != null) {
            mainApp.showGameScene(GameMode.RUSH_40);
        }
    }

    @FXML
    private void handleQuit(ActionEvent event) {
        Platform.exit();
    }
}

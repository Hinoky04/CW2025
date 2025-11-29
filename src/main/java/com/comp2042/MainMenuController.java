package com.comp2042;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Controller for the main menu screen.
 * Lets the user choose a game mode or quit the app.
 */
public class MainMenuController {

    // Reference to the Main application so we can change scenes.
    private Main mainApp;

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
    private Button quitButton;

    @FXML
    private void handlePlayClassic(ActionEvent event) {
        if (mainApp != null) {
            mainApp.showGameScene(GameMode.CLASSIC);
        }
    }

    @FXML
    private void handlePlaySurvival(ActionEvent event) {
        if (mainApp != null) {
            mainApp.showGameScene(GameMode.SURVIVAL);
        }
    }

    @FXML
    private void handleQuit(ActionEvent event) {
        Platform.exit();
    }
}

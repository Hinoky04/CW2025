package com.comp2042;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Controller for the main menu screen.
 * Responsible only for menu actions (play, quit).
 */
public class MainMenuController {

    // Reference to the Main application so we can switch scenes.
    private Main mainApp;

    void init(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private Label titleLabel;

    @FXML
    private Button playButton;

    @FXML
    private Button quitButton;

    @FXML
    private void handlePlay(ActionEvent event) {
        if (mainApp != null) {
            mainApp.showGameScene();
        }
    }

    @FXML
    private void handleQuit(ActionEvent event) {
        Platform.exit();
    }
}

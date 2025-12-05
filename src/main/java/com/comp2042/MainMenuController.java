package com.comp2042;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the main menu screen.
 * Responsible only for menu actions (choose mode, quit).
 */
public class MainMenuController implements Initializable {

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
    private Button settingsButton;

    @FXML
    private Button quitButton;

    @FXML
    private StackPane tutorialOverlay;

    @FXML
    private Button tutorialCloseButton;

    // Track if tutorial has been shown (static so it persists across menu visits)
    private static boolean tutorialShown = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Show tutorial on first visit
        if (!tutorialShown && tutorialOverlay != null) {
            Platform.runLater(() -> {
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(300));
                delay.setOnFinished(e -> {
                    tutorialOverlay.setVisible(true);
                    tutorialOverlay.toFront();
                    tutorialOverlay.setMouseTransparent(false);
                });
                delay.play();
            });
        }

        // Wire tutorial close button
        if (tutorialCloseButton != null) {
            tutorialCloseButton.setOnAction(e -> closeTutorial());
        }
    }

    /**
     * Closes the tutorial overlay.
     */
    private void closeTutorial() {
        if (tutorialOverlay != null) {
            tutorialOverlay.setVisible(false);
            tutorialShown = true;
        }
    }

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
    private void handleSettings(ActionEvent event) {
        if (mainApp != null) {
            mainApp.showSettingsScene();
        }
    }

    @FXML
    private void handleQuit(ActionEvent event) {
        Platform.exit();
    }
}

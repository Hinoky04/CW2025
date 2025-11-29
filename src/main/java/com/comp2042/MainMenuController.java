package com.comp2042;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Controller for the main menu screen.
 * Only handles menu actions (choose mode, quit).
 */
public class MainMenuController {

    // Reference to the Main application so we can switch scenes.
    private Main mainApp;

    /**
     * Called by Main.showMainMenu() so the menu can
     * start games in different modes via showGameScene(...).
     */
    void init(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private Label titleLabel;

    @FXML
    private Button playClassicButton;

    @FXML
    private Button playSurvivalButton;

    @FXML
    private Button quitButton;

    /**
     * Start a new game in Classic mode (current baseline behaviour).
     */
    @FXML
    private void handlePlayClassic(ActionEvent event) {
        if (mainApp != null) {
            mainApp.showGameScene(GameMode.CLASSIC);
        }
    }

    /**
     * Start a new game in Survival mode.
     * For now this behaves like Classic; rules will diverge in Phase 5.3.
     */
    @FXML
    private void handlePlaySurvival(ActionEvent event) {
        if (mainApp != null) {
            mainApp.showGameScene(GameMode.SURVIVAL);
        }
    }

    /**
     * Exit the application from the main menu.
     */
    @FXML
    private void handleQuit(ActionEvent event) {
        Platform.exit();
    }
}

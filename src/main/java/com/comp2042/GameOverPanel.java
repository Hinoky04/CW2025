package com.comp2042;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Simple game over panel shown on top of the board.
 * Displays a "GAME OVER" label and buttons for restarting
 * or returning to the main menu. Actual actions are provided
 * via callbacks from GuiController.
 */
public class GameOverPanel extends BorderPane {

    private Runnable onRestart;
    private Runnable onMainMenu;

    public GameOverPanel() {
        // Big "GAME OVER" title.
        Label gameOverLabel = new Label("GAME OVER");
        gameOverLabel.getStyleClass().add("gameOverStyle");

        // Restart button: retry the same mode.
        Button restartButton = new Button("Restart");
        restartButton.getStyleClass().add("ipad-dark-grey");
        restartButton.setOnAction(e -> {
            if (onRestart != null) {
                onRestart.run();
            }
        });

        // Main menu button: go back to the mode selection screen.
        Button mainMenuButton = new Button("Main Menu");
        mainMenuButton.getStyleClass().add("ipad-dark-grey");
        mainMenuButton.setOnAction(e -> {
            if (onMainMenu != null) {
                onMainMenu.run();
            }
        });

        VBox vbox = new VBox(10, gameOverLabel, restartButton, mainMenuButton);
        vbox.setAlignment(Pos.CENTER);

        setCenter(vbox);
    }

    /**
     * Set callback used when the user presses the Restart button.
     */
    public void setOnRestart(Runnable onRestart) {
        this.onRestart = onRestart;
    }

    /**
     * Set callback used when the user presses the Main Menu button.
     */
    public void setOnMainMenu(Runnable onMainMenu) {
        this.onMainMenu = onMainMenu;
    }
}

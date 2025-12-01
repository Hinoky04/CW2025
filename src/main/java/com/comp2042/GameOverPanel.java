package com.comp2042;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Game over / result panel shown on top of the board.
 * Displays result summary (mode, score, lines, optional time)
 * and buttons for restarting or returning to the main menu.
 */
public class GameOverPanel extends BorderPane {

    private Runnable onRestart;
    private Runnable onMainMenu;

    private final Label titleLabel;
    private final Label modeLabel;
    private final Label scoreLabel;
    private final Label linesLabel;
    private final Label timeLabel;

    public GameOverPanel() {
        // Title ("GAME OVER" or "YOU WIN").
        titleLabel = new Label("GAME OVER");
        titleLabel.getStyleClass().add("gameOverStyle");

        // Simple summary labels.
        modeLabel = new Label();
        modeLabel.getStyleClass().add("hud-value");

        scoreLabel = new Label();
        scoreLabel.getStyleClass().add("hud-value");

        linesLabel = new Label();
        linesLabel.getStyleClass().add("hud-value");

        timeLabel = new Label();
        timeLabel.getStyleClass().add("hud-value");

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

        VBox vbox = new VBox(
                10,
                titleLabel,
                modeLabel,
                scoreLabel,
                linesLabel,
                timeLabel,
                restartButton,
                mainMenuButton
        );
        vbox.setAlignment(Pos.CENTER);

        setCenter(vbox);
        clearSummary();
    }

    private void clearSummary() {
        modeLabel.setText("");
        scoreLabel.setText("");
        linesLabel.setText("");
        timeLabel.setText("");
    }

    /**
     * Populate the result panel with final run information.
     *
     * @param mode              game mode that was played
     * @param score             final score
     * @param totalLinesCleared total lines cleared in this run
     * @param targetLines       Rush-40 target (0 if not applicable)
     * @param timeSeconds       completion time in seconds (<=0 means "no time")
     * @param win               true if player achieved the mode's win condition
     */
    public void setResult(GameMode mode,
                          int score,
                          int totalLinesCleared,
                          int targetLines,
                          double timeSeconds,
                          boolean win) {

        titleLabel.setText(win ? "YOU WIN" : "GAME OVER");

        if (mode != null) {
            modeLabel.setText("Mode: " + mode.getDisplayName());
        } else {
            modeLabel.setText("");
        }

        scoreLabel.setText("Score: " + score);

        if (targetLines > 0) {
            linesLabel.setText("Lines: " + totalLinesCleared + " / " + targetLines);
        } else {
            linesLabel.setText("Lines: " + totalLinesCleared);
        }

        if (timeSeconds > 0.0) {
            long totalSecs = (long) timeSeconds;
            long minutes = totalSecs / 60;
            long seconds = totalSecs % 60;
            timeLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
        } else {
            timeLabel.setText("");
        }
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

package com.comp2042.ui;

import com.comp2042.models.GameMode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Game over / result panel shown on top of the board.
 * Displays result summary (mode, score, lines, optional time)
 * and buttons for restarting or returning to the main menu.
 * Matches the pause menu design style.
 */
public class GameOverPanel extends StackPane {

    private Runnable onRestart;
    private Runnable onMainMenu;

    private final Label titleLabel1;  // "GAME" or "YOU"
    private final Label titleLabel2;  // "OVER" or "WIN"
    private final Label modeLabel;
    private final Label scoreLabel;
    private final Label linesLabel;
    private final Label timeLabel;

    public GameOverPanel() {
        setAlignment(Pos.CENTER);
        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);
        
        // No background overlay - game board fully visible
        
        // Title split into two lines - using pause-title style
        titleLabel1 = new Label("GAME");
        titleLabel1.getStyleClass().add("pause-title");
        
        titleLabel2 = new Label("OVER");
        titleLabel2.getStyleClass().add("pause-title");

        // Summary labels - using hud-value style for consistency
        modeLabel = new Label();
        modeLabel.getStyleClass().add("hud-value");

        scoreLabel = new Label();
        scoreLabel.getStyleClass().add("hud-value");

        linesLabel = new Label();
        linesLabel.getStyleClass().add("hud-value");

        timeLabel = new Label();
        timeLabel.getStyleClass().add("hud-value");

        // Restart button: retry the same mode - using pause-button style
        Button restartButton = new Button("Restart");
        restartButton.getStyleClass().add("pause-button");
        restartButton.setOnAction(e -> {
            if (onRestart != null) {
                onRestart.run();
            }
        });

        // Main menu button: go back to the mode selection screen - using pause-button style
        Button mainMenuButton = new Button("Main Menu");
        mainMenuButton.getStyleClass().add("pause-button");
        mainMenuButton.setOnAction(e -> {
            if (onMainMenu != null) {
                onMainMenu.run();
            }
        });

        // Centered menu panel with transparent background so blocks are visible
        VBox menuPanel = new VBox(24.0);
        menuPanel.setAlignment(Pos.CENTER);
        menuPanel.getStyleClass().add("pause-menu-panel");
        menuPanel.setPadding(new Insets(40.0, 50.0, 40.0, 50.0));
        // Override background to be transparent so game blocks are visible
        menuPanel.setStyle("-fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 2; -fx-border-radius: 20; -fx-background-radius: 20;");
        
        // Title container - two lines
        VBox titleContainer = new VBox(0);
        titleContainer.setAlignment(Pos.CENTER);
        titleContainer.getChildren().addAll(
                titleLabel1,
                titleLabel2
        );
        
        // Info labels container
        VBox infoContainer = new VBox(14.0);
        infoContainer.setAlignment(Pos.CENTER);
        infoContainer.getChildren().addAll(
                modeLabel,
                scoreLabel,
                linesLabel,
                timeLabel
        );
        
        // Buttons container
        VBox buttonContainer = new VBox(14.0);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(
                restartButton,
                mainMenuButton
        );
        
        menuPanel.getChildren().addAll(
                titleContainer,
                infoContainer,
                buttonContainer
        );

        getChildren().add(menuPanel);
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
     * @param timeSeconds       completion time in seconds ({@code <=0} means "no time")
     * @param win               true if player achieved the mode's win condition
     */
    public void setResult(GameMode mode,
                          int score,
                          int totalLinesCleared,
                          int targetLines,
                          double timeSeconds,
                          boolean win) {

        if (win) {
            titleLabel1.setText("YOU");
            titleLabel2.setText("WIN");
        } else {
            titleLabel1.setText("GAME");
            titleLabel2.setText("OVER");
        }

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

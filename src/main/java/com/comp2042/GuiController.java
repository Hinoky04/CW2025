package com.comp2042;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    // Size of each cell (brick) in the grid, in pixels
    private static final int BRICK_SIZE = 20;

    // Number of hidden rows at the top of the board (spawn area)
    private static final int HIDDEN_TOP_ROWS = 2;

    // Y offset for the brickPanel so it lines up visually with the grid
    private static final int BRICK_PANEL_Y_OFFSET = -42;

    // How often the piece falls automatically (milliseconds)
    private static final int FALL_INTERVAL_MS = 400;

    @FXML
    private GridPane gamePanel;          // main board grid

    @FXML
    private Group groupNotification;     // group for score popups

    @FXML
    private GridPane brickPanel;         // grid used to display current piece

    @FXML
    private GameOverPanel gameOverPanel; // overlay shown when the game ends

    @FXML
    private Pane pauseOverlay;           // overlay shown when the game is paused

    @FXML
    private Text scoreText;              // shows current score in the HUD

    @FXML
    private Text levelText;              // shows current level in the HUD

    @FXML
    private Text comboText;              // shows current combo multiplier

    // Background cells (for the board)
    private Rectangle[][] displayMatrix;

    // Current falling piece cells
    private Rectangle[][] rectangles;

    // Listener that sends user input events to the game logic
    private InputEventListener eventListener;

    // Timer for automatic down movement
    private Timeline timeLine;

    // Single source of truth for the current game state.
    private GameState gameState = GameState.PLAYING;

    // Extra flags kept for possible UI bindings later.
    private final BooleanProperty isPause = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver = new SimpleBooleanProperty(false);

    /**
     * Central helper for changing game state.
     * Keeps internal flags and overlays in sync.
     */
    private void setGameState(GameState newState) {
        gameState = newState;
        isPause.set(newState == GameState.PAUSED);
        isGameOver.set(newState == GameState.GAME_OVER);

        if (pauseOverlay != null) {
            pauseOverlay.setVisible(newState == GameState.PAUSED);
        }
        if (gameOverPanel != null) {
            gameOverPanel.setVisible(newState == GameState.GAME_OVER);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load custom digital font if available (used for score HUD etc.)
        URL fontUrl = getClass().getClassLoader().getResource("digital.ttf");
        if (fontUrl != null) {
            Font.loadFont(fontUrl.toExternalForm(), 38);
        }

        // Allow the game panel to receive keyboard focus and request it initially
        gamePanel.setFocusTraversable(true);
        gamePanel.requestFocus();

        // Handle keyboard input for movement, pause, and new game
        gamePanel.setOnKeyPressed(this::handleKeyPressed);

        // Use centralised state helper so overlays stay consistent.
        setGameState(GameState.PLAYING);
    }

    /**
     * Centralised key handler.
     * Handles pause (P) and new game (N) in addition to movement.
     */
    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();

        // P toggles pause/resume even when already paused
        if (code == KeyCode.P) {
            togglePause();
            event.consume();
            return;
        }

        // N starts a new game at any time
        if (code == KeyCode.N) {
            newGame(null);
            event.consume();
            return;
        }

        // Movement and rotation only allowed while playing
        if (!canHandleInput()) {
            return;
        }

        if (code == KeyCode.LEFT || code == KeyCode.A) {
            refreshBrick(eventListener.onLeftEvent(
                    new MoveEvent(EventType.LEFT, EventSource.USER)));
            event.consume();
        }

        if (code == KeyCode.RIGHT || code == KeyCode.D) {
            refreshBrick(eventListener.onRightEvent(
                    new MoveEvent(EventType.RIGHT, EventSource.USER)));
            event.consume();
        }

        if (code == KeyCode.UP || code == KeyCode.W) {
            refreshBrick(eventListener.onRotateEvent(
                    new MoveEvent(EventType.ROTATE, EventSource.USER)));
            event.consume();
        }

        if (code == KeyCode.DOWN || code == KeyCode.S) {
            moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
            event.consume();
        }
    }

    /**
     * We only handle movement input while the game is actively playing.
     */
    private boolean canHandleInput() {
        return gameState == GameState.PLAYING;
    }

    // --- Old initGameView kept as comment for marker reference ----------------
    // public void initGameView(int[][] boardMatrix, ViewData brick) { ... }

    /**
     * Called by the game logic to set up the initial board and piece view.
     * Split into smaller helpers for readability.
     */
    public void initGameView(int[][] boardMatrix, ViewData brick) {
        initBackgroundCells(boardMatrix);
        initFallingBrick(brick);
        startAutoDropTimer();
    }

    /**
     * Creates rectangles for the background board and adds them to the gamePanel.
     */
    private void initBackgroundCells(int[][] boardMatrix) {
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];
        for (int row = HIDDEN_TOP_ROWS; row < boardMatrix.length; row++) {
            for (int col = 0; col < boardMatrix[row].length; col++) {
                Rectangle cell = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                cell.setFill(Color.TRANSPARENT);
                displayMatrix[row][col] = cell;
                // Skip hidden top rows when adding to the visible grid
                gamePanel.add(cell, col, row - HIDDEN_TOP_ROWS);
            }
        }
    }

    /**
     * Creates rectangles for the current falling brick and positions the brickPanel.
     */
    private void initFallingBrick(ViewData brick) {
        int[][] brickData = brick.getBrickData();
        rectangles = new Rectangle[brickData.length][brickData[0].length];
        for (int row = 0; row < brickData.length; row++) {
            for (int col = 0; col < brickData[row].length; col++) {
                Rectangle cell = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                cell.setFill(getFillColor(brickData[row][col]));
                rectangles[row][col] = cell;
                brickPanel.add(cell, col, row);
            }
        }
        // Position the brickPanel according to the starting brick position
        updateBrickPanelPosition(brick);
    }

    /**
     * Creates and starts the automatic down-movement timer.
     */
    private void startAutoDropTimer() {
        timeLine = new Timeline(new KeyFrame(
                Duration.millis(FALL_INTERVAL_MS),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();
    }

    /**
     * Moves the brickPanel to match the brick's logical x/y position.
     */
    private void updateBrickPanelPosition(ViewData brick) {
        double x = gamePanel.getLayoutX()
                + brick.getxPosition() * brickPanel.getVgap()
                + brick.getxPosition() * BRICK_SIZE;

        double y = BRICK_PANEL_Y_OFFSET
                + gamePanel.getLayoutY()
                + brick.getyPosition() * brickPanel.getHgap()
                + brick.getyPosition() * BRICK_SIZE;

        brickPanel.setLayoutX(x);
        brickPanel.setLayoutY(y);
    }

    /**
     * Converts an integer code into a Color/Paint for rendering.
     */
    private Paint getFillColor(int value) {
        switch (value) {
            case 0:
                return Color.TRANSPARENT;
            case 1:
                return Color.AQUA;
            case 2:
                return Color.BLUEVIOLET;
            case 3:
                return Color.DARKGREEN;
            case 4:
                return Color.YELLOW;
            case 5:
                return Color.RED;
            case 6:
                return Color.BEIGE;
            case 7:
                return Color.BURLYWOOD;
            default:
                return Color.WHITE;
        }
    }

    /**
     * Refreshes the visual representation of the current brick.
     */
    private void refreshBrick(ViewData brick) {
        if (gameState == GameState.PLAYING) {
            updateBrickPanelPosition(brick);

            int[][] brickData = brick.getBrickData();
            for (int row = 0; row < brickData.length; row++) {
                for (int col = 0; col < brickData[row].length; col++) {
                    setRectangleData(brickData[row][col], rectangles[row][col]);
                }
            }
        }
    }

    /**
     * Refreshes the background board view from the board matrix.
     */
    public void refreshGameBackground(int[][] board) {
        for (int row = HIDDEN_TOP_ROWS; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                setRectangleData(board[row][col], displayMatrix[row][col]);
            }
        }
    }

    /**
     * Applies a colour and some rounded corners to a rectangle.
     */
    private void setRectangleData(int colorCode, Rectangle rectangle) {
        rectangle.setFill(getFillColor(colorCode));
        rectangle.setArcHeight(9);
        rectangle.setArcWidth(9);
    }

    /**
     * Called either by the timer (thread) or user soft drop.
     */
    private void moveDown(MoveEvent event) {
        if (gameState == GameState.PLAYING) {
            DownData downData = eventListener.onDownEvent(event);

            // If a row was cleared, show a score notification
            if (downData.getClearRow() != null &&
                    downData.getClearRow().getLinesRemoved() > 0) {

                String bonusText = "+" + downData.getClearRow().getScoreBonus();
                NotificationPanel notificationPanel = new NotificationPanel(bonusText);
                groupNotification.getChildren().add(notificationPanel);
                notificationPanel.showScore(groupNotification.getChildren());
            }

            // Update the brick's position/shape
            refreshBrick(downData.getViewData());
        }

        // Keep keyboard focus on the game panel
        gamePanel.requestFocus();
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    /**
     * Binds the score property from the model to the score text in the HUD.
     */
    public void bindScore(IntegerProperty scoreProperty) {
        if (scoreText != null) {
            scoreText.textProperty().bind(scoreProperty.asString("Score %d"));
        }
    }

    /**
     * Binds the level property from the model to the HUD and updates drop speed.
     */
    public void bindLevel(IntegerProperty levelProperty) {
        if (levelText != null) {
            levelText.textProperty().bind(levelProperty.asString("Level %d"));
        }

        // When level changes, adjust the drop speed.
        levelProperty.addListener((obs, oldLevel, newLevel) ->
                onLevelChanged(newLevel.intValue()));
    }

    /**
     * Binds the combo property from the model to the HUD.
     * Shows "Combo xN" so the player can see when a chain is building or broken.
     */
    public void bindCombo(IntegerProperty comboProperty) {
        if (comboText != null) {
            comboText.textProperty().bind(comboProperty.asString("Combo x%d"));
        }
    }

    /**
     * Adjusts the timeline speed when the level changes.
     * Higher level = faster drop.
     */
    private void onLevelChanged(int newLevel) {
        if (timeLine == null) {
            return;
        }

        // Simple curve: each level is roughly 15% faster than the previous one.
        double rate = 1.0 + (newLevel - 1) * 0.15;
        timeLine.setRate(rate);
    }

    /**
     * Game over handler.
     * Uses setGameState so flags and overlays stay consistent.
     */
    public void gameOver() {
        timeLine.stop();
        setGameState(GameState.GAME_OVER);
    }

    /**
     * Starts a new game from the UI.
     * Uses setGameState so flags and overlays stay consistent.
     */
    public void newGame(javafx.event.ActionEvent actionEvent) {
        timeLine.stop();
        eventListener.createNewGame();
        gamePanel.requestFocus();
        timeLine.play();

        setGameState(GameState.PLAYING);
    }

    /**
     * Core pause/resume behaviour.
     * PLAYING -> PAUSED pauses the timeline and shows overlay.
     * PAUSED  -> PLAYING resumes the timeline and hides overlay.
     */
    private void togglePause() {
        if (gameState == GameState.PLAYING) {
            timeLine.pause();
            setGameState(GameState.PAUSED);
        } else if (gameState == GameState.PAUSED) {
            timeLine.play();
            setGameState(GameState.PLAYING);
            gamePanel.requestFocus();
        }
        // In GAME_OVER or other states we ignore pause.
    }

    /**
     * Pause button handler in the toolbar/menu.
     * Simply calls the core togglePause() method.
     */
    public void pauseGame(javafx.event.ActionEvent actionEvent) {
        togglePause();
    }
}

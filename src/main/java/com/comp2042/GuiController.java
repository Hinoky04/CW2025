package com.comp2042;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
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

/**
 * JavaFX controller for the main game screen.
 * Handles keyboard input, HUD, pause/game-over overlays and drawing the board.
 */
public class GuiController implements Initializable {

    // Size of each cell (brick) in the grid, in pixels
    private static final int BRICK_SIZE = 20;

    // Number of hidden rows at the top of the board (spawn area)
    private static final int HIDDEN_TOP_ROWS = 2;

    // Y offset for the brickPanel so it lines up visually with the grid
    private static final int BRICK_PANEL_Y_OFFSET = -42;

    // === Configurable values (defaults tuned roughly for Classic mode) ===

    // Base fall interval (ms) before level scaling is applied.
    private int fallIntervalMs = 400;

    // How much faster each level becomes (e.g. 0.15 = +15% per level).
    private double levelSpeedFactor = 0.15;

    // How many top visible rows are considered "danger zone".
    private int dangerVisibleRows = 3;


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
    private BorderPane gameBoard;        // outer border of the board (for danger style)

    @FXML
    private Text scoreText;              // shows current score in the HUD

    @FXML
    private Text levelText;              // shows current level in the HUD

    @FXML
    private Text comboText;              // shows current combo multiplier

    @FXML
    private Text dangerText;             // warning text when stack is near the top

    // Pause overlay buttons (wired manually in initialize()).
    @FXML
    private Button resumeButton;

    @FXML
    private Button restartButton;

    @FXML
    private Button pauseMenuButton;

    // Background cells (for the board).
    private Rectangle[][] displayMatrix;

    // Current falling piece cells.
    private Rectangle[][] rectangles;

    // Listener that sends user input events to the game logic.
    private InputEventListener eventListener;

    // Timer for automatic down movement.
    private Timeline timeLine;

    // Single source of truth for the current game state.
    private GameState gameState = GameState.PLAYING;

    // Extra flags kept for possible UI bindings later.
    private final BooleanProperty isPause = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver = new SimpleBooleanProperty(false);

    // Tracks whether we are currently in the danger zone.
    private final BooleanProperty isDanger = new SimpleBooleanProperty(false);

    // Reference back to the Main app so the game screen can return to the main menu.
    private Main mainApp;

    // Current game mode for this run (Classic / Survival, etc.).
    private GameMode currentMode;

    /**
     * Called from Main.showGameScene() so this controller can access
     * navigation methods like showMainMenu().
     */
    void init(Main mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Called from GameController so the GUI knows which mode is running.
     * Used when restarting the same mode.
     */
    void setGameMode(GameMode mode) {
        // Fallback to CLASSIC so restartSameMode() always has a valid mode.
        this.currentMode = (mode != null) ? mode : GameMode.CLASSIC;
    }

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

        // Once the game is over, we hide the danger warning.
        if (newState == GameState.GAME_OVER) {
            setDanger(false);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load custom digital font if available (used for score HUD etc.).
        URL fontUrl = getClass().getClassLoader().getResource("digital.ttf");
        if (fontUrl != null) {
            Font.loadFont(fontUrl.toExternalForm(), 38);
        }

        // Let the game panel receive keyboard input.
        gamePanel.setFocusTraversable(true);
        gamePanel.requestFocus();
        gamePanel.setOnKeyPressed(this::handleKeyPressed);

        // Wire game-over panel buttons to restart / main menu.
        if (gameOverPanel != null) {
            gameOverPanel.setOnRestart(this::restartSameMode);
            gameOverPanel.setOnMainMenu(this::backToMainMenu);
        }

        // ===== Pause overlay buttons (with debug prints) =====
        if (resumeButton != null) {
            System.out.println("DEBUG: resumeButton injected");
            resumeButton.setOnAction(e -> {
                System.out.println("DEBUG: Resume clicked");
                togglePause();          // resume / pause
            });
        } else {
            System.out.println("DEBUG: resumeButton is NULL");
        }

        if (restartButton != null) {
            System.out.println("DEBUG: restartButton injected");
            restartButton.setOnAction(e -> {
                System.out.println("DEBUG: Restart clicked");
                restartSameMode();      // restart current mode
            });
        } else {
            System.out.println("DEBUG: restartButton is NULL");
        }

        if (pauseMenuButton != null) {
            System.out.println("DEBUG: pauseMenuButton injected");
            pauseMenuButton.setOnAction(e -> {
                System.out.println("DEBUG: Pause Main Menu clicked");
                backToMainMenu();       // back to main menu
            });
        } else {
            System.out.println("DEBUG: pauseMenuButton is NULL");
        }

        // VERY IMPORTANT: make sure pause overlay can receive mouse events.
        // If mouseTransparent is true, all clicks will pass through and buttons won't fire.
        if (pauseOverlay != null) {
            pauseOverlay.setMouseTransparent(false);
        }

        // Start in PLAYING state (no overlays visible).
        setGameState(GameState.PLAYING);

        // Danger HUD starts hidden.
        setDanger(false);
    }


    /**
     * Centralised key handler.
     * Handles pause (P / ESC), restart (N), and forwards movement only while playing.
     */
    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();

        // Special handling when the game has finished.
        if (gameState == GameState.GAME_OVER) {
            handleGameOverKey(event);
            return;
        }

        // P or ESC toggle pause/resume (only when not in GAME_OVER).
        if (code == KeyCode.P || code == KeyCode.ESCAPE) {
            togglePause();
            event.consume();
            return;
        }

        // N restarts the current mode from scratch.
        if (code == KeyCode.N) {
            restartSameMode();
            event.consume();
            return;
        }

        // Movement and rotation only allowed while playing.
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
     * Key handling when the game has finished.
     * R = restart same mode, M or ESC = back to main menu.
     */
    private void handleGameOverKey(KeyEvent event) {
        KeyCode code = event.getCode();

        if (code == KeyCode.R) {
            restartSameMode();
            event.consume();
        } else if (code == KeyCode.M || code == KeyCode.ESCAPE) {
            backToMainMenu();
            event.consume();
        }
    }

    /**
     * We only handle movement input while the game is actively playing.
     */
    private boolean canHandleInput() {
        return gameState == GameState.PLAYING;
    }

    /**
     * Called by the game logic to set up the initial board and piece view.
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
                // Skip hidden top rows when adding to the visible grid.
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
        // Position the brickPanel according to the starting brick position.
        updateBrickPanelPosition(brick);
    }

    /**
     * Creates and starts the automatic down-movement timer.
     */
    private void startAutoDropTimer() {
        timeLine = new Timeline(new KeyFrame(
                Duration.millis(fallIntervalMs),
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
     * Refreshes the background board view from the board matrix
     * and updates the danger state based on the top visible rows.
     */
    public void refreshGameBackground(int[][] board) {
        for (int row = HIDDEN_TOP_ROWS; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                setRectangleData(board[row][col], displayMatrix[row][col]);
            }
        }

        // After updating the visible board, recompute whether we are in danger.
        updateDangerFromBoard(board);
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

            // If a row was cleared, show a score notification.
            if (downData.getClearRow() != null &&
                    downData.getClearRow().getLinesRemoved() > 0) {

                String bonusText = "+" + downData.getClearRow().getScoreBonus();
                NotificationPanel notificationPanel = new NotificationPanel(bonusText);
                groupNotification.getChildren().add(notificationPanel);
                notificationPanel.showScore(groupNotification.getChildren());
            }

            // Update the brick's position/shape.
            refreshBrick(downData.getViewData());
        }

        // Keep keyboard focus on the game panel.
        gamePanel.requestFocus();
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    /**
     * Applies a GameConfig so the GUI uses mode-specific values.
     * Called once from GameController when a new game starts.
     */
    public void applyConfig(GameConfig config) {
        if (config == null) {
            return;
        }
        this.fallIntervalMs = config.getBaseFallIntervalMs();
        this.levelSpeedFactor = config.getLevelSpeedFactor();
        this.dangerVisibleRows = config.getDangerVisibleRows();
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

        // Curve is driven by GameConfig; each level multiplies speed by this factor.
        double rate = 1.0 + (newLevel - 1) * levelSpeedFactor;
        timeLine.setRate(rate);

    }

    /**
     * Game over handler.
     */
    public void gameOver() {
        if (timeLine != null) {
            timeLine.stop();
        }

        setGameState(GameState.GAME_OVER);

        // Once the game is over, we no longer need the falling brick visuals.
        if (brickPanel != null) {
            brickPanel.getChildren().clear();
        }
    }

    /**
     * Old "new game" button behaviour is now equivalent to restartSameMode().
     * Still保留这个方法，给 FXML 或其它地方复用。
     */
    public void newGame(javafx.event.ActionEvent actionEvent) {
        restartSameMode();
    }

    /**
     * Pause button handler in case有其它地方（工具栏）调用。
     */
    public void pauseGame(javafx.event.ActionEvent actionEvent) {
        togglePause();
    }

    /**
     * Core pause/resume behaviour.
     * PLAYING -> PAUSED pauses the timeline and shows overlay.
     * PAUSED  -> PLAYING resumes the timeline and hides overlay.
     */
    private void togglePause() {
        if (timeLine == null) {
            // Fallback: only toggle overlay if timeline is not ready.
            setGameState(gameState == GameState.PLAYING
                    ? GameState.PAUSED
                    : GameState.PLAYING);
            return;
        }

        if (gameState == GameState.PLAYING) {
            timeLine.pause();
            setGameState(GameState.PAUSED);
        } else if (gameState == GameState.PAUSED) {
            timeLine.play();
            setGameState(GameState.PLAYING);
            gamePanel.requestFocus();
        }
    }

    /**
     * Restart the current mode by reloading the whole game scene.
     * This guarantees the same spawn position as a fresh start.
     */
    private void restartSameMode() {
        if (mainApp == null || currentMode == null) {
            return;
        }

        if (timeLine != null) {
            timeLine.stop();
        }

        mainApp.showGameScene(currentMode);
    }

    /**
     * Common helper for leaving the game screen and returning to the main menu.
     */
    private void backToMainMenu() {
        if (timeLine != null) {
            timeLine.stop();
        }
        if (mainApp != null) {
            mainApp.showMainMenu();
        }
    }

    /**
     * Updates the danger state based on the contents of the board matrix.
     */
    private void updateDangerFromBoard(int[][] board) {
        boolean found = false;

        int visibleRows = board.length - HIDDEN_TOP_ROWS;
        int limit = Math.min(dangerVisibleRows, visibleRows);

        for (int row = HIDDEN_TOP_ROWS; row < HIDDEN_TOP_ROWS + limit; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] != 0) {
                    found = true;
                    break;
                }
            }
            if (found) {
                break;
            }
        }

        setDanger(found);
    }

    /**
     * Applies or removes danger visuals on the HUD and board.
     */
    private void setDanger(boolean value) {
        if (isDanger.get() == value) {
            return; // nothing to change
        }
        isDanger.set(value);

        if (dangerText != null) {
            dangerText.setVisible(value);
        }
        if (gameBoard != null) {
            if (value) {
                if (!gameBoard.getStyleClass().contains("dangerBoard")) {
                    gameBoard.getStyleClass().add("dangerBoard");
                }
            } else {
                gameBoard.getStyleClass().remove("dangerBoard");
            }
        }
    }
}

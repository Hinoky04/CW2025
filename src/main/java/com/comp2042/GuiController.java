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

    // Size of each cell (brick) in the grid, in pixels.
    private static final int BRICK_SIZE = 26;

    // Preview brick size for the NEXT / HOLD panels.
    private static final int NEXT_BRICK_SIZE = 20;

    // Number of hidden rows at the top of the board (spawn area).
    private static final int HIDDEN_TOP_ROWS = 2;

    // Y offset for the brickPanel so it lines up visually with the grid.
    private static final int BRICK_PANEL_Y_OFFSET = -42;

    // === Configurable values (defaults tuned roughly for Classic mode) ===

    // Base fall interval (ms) before level scaling is applied.
    private int fallIntervalMs = 400;

    // How much faster each level becomes (e.g. 0.15 = +15% per level).
    private double levelSpeedFactor = 0.15;

    // How many top visible rows are considered "danger zone".
    private int dangerVisibleRows = 3;

    // How much to dim landed blocks in the background (1.0 = normal brightness).
    private double backgroundDimFactor = 1.0;

    @FXML
    private GridPane gamePanel;          // main board grid

    @FXML
    private Group groupNotification;     // group for score popups

    @FXML
    private GridPane brickPanel;         // grid used to display current piece

    @FXML
    private GridPane nextBrickPanel;     // grid used to display NEXT preview

    @FXML
    private GridPane holdBrickPanel;     // grid used to display HOLD preview

    @FXML
    private GameOverPanel gameOverPanel; // overlay shown when the game ends

    @FXML
    private Pane pauseOverlay;           // overlay shown when the game is paused

    @FXML
    private BorderPane gameBoard;        // outer border of the board (for danger style)

    @FXML
    private Text modeText;               // shows current mode in the HUD

    @FXML
    private Text modeHintText;           // per-mode hint line in the HUD

    @FXML
    private Text scoreText;              // shows current score in the HUD

    @FXML
    private Text levelText;              // shows current level in the HUD

    @FXML
    private Text timerText;              // shows elapsed time in the HUD

    @FXML
    private Text progressText;           // generic progress line (Rush / Survival, etc.)

    @FXML
    private Text bestText;               // best score / time info

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

    // NEXT preview cells.
    private Rectangle[][] nextBrickRectangles;

    // HOLD preview cells.
    private Rectangle[][] holdBrickRectangles;

    // Listener that sends user input events to the game logic.
    private InputEventListener eventListener;

    // Timer for automatic down movement.
    private Timeline timeLine;

    // HUD timer for elapsed time.
    private Timeline hudTimer;

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

    // Timer configuration and state.
    private boolean timerEnabled;
    private long timerStartNanos;
    private long timerPauseStartNanos;
    private long timerPausedAccumNanos;
    private boolean timerRunning;

    // === Best records (static so they survive across runs) ===
    private static final int[] bestScores = new int[GameMode.values().length];
    private static final double[] bestRushTimes = new double[GameMode.values().length];

    static {
        for (int i = 0; i < bestRushTimes.length; i++) {
            bestRushTimes[i] = -1.0; // no record yet
        }
    }

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
        this.currentMode = (mode != null) ? mode : GameMode.CLASSIC;

        if (modeText != null && this.currentMode != null) {
            modeText.setText("Mode: " + this.currentMode.getDisplayName());
        }

        if (modeHintText != null && this.currentMode != null) {
            modeHintText.setText(buildModeHint(this.currentMode));
        }

        clearProgressText();
        refreshBestInfoForMode(this.currentMode);
    }

    /**
     * Builds a short per-mode hint shown under the mode label.
     */
    private String buildModeHint(GameMode mode) {
        if (mode == null) {
            return "";
        }
        switch (mode) {
            case CLASSIC:
                return "Classic: standard Tetris rules.";
            case SURVIVAL:
                return "Survival: clear 4 lines at once to gain a shield.";
            case HYPER:
                return "Hyper: faster with dimmed background.";
            case RUSH_40:
                return "Rush 40: clear 40 lines as fast as possible.";
            default:
                return "";
        }
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

        // Pause overlay buttons.
        if (resumeButton != null) {
            resumeButton.setOnAction(e -> togglePause());
        }
        if (restartButton != null) {
            restartButton.setOnAction(e -> restartSameMode());
        }
        if (pauseMenuButton != null) {
            pauseMenuButton.setOnAction(e -> backToMainMenu());
        }

        if (pauseOverlay != null) {
            pauseOverlay.setMouseTransparent(false);
        }

        setGameState(GameState.PLAYING);
        setDanger(false);

        if (timerText != null) {
            timerText.setText("");
        }
        clearProgressText();

        if (bestText != null) {
            bestText.setText("Best Score 0");
        }
        // modeHintText is set once GameController selects the mode.
    }

    /**
     * Centralised key handler.
     */
    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();

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

        if (!canHandleInput()) {
            return;
        }

        if (code == KeyCode.LEFT || code == KeyCode.A) {
            refreshBrick(eventListener.onLeftEvent(
                    new MoveEvent(EventType.LEFT, EventSource.USER)));
            SoundManager.playMove();
            event.consume();
        }

        if (code == KeyCode.RIGHT || code == KeyCode.D) {
            refreshBrick(eventListener.onRightEvent(
                    new MoveEvent(EventType.RIGHT, EventSource.USER)));
            SoundManager.playMove();
            event.consume();
        }

        if (code == KeyCode.UP || code == KeyCode.W) {
            refreshBrick(eventListener.onRotateEvent(
                    new MoveEvent(EventType.ROTATE, EventSource.USER)));
            SoundManager.playRotate();
            event.consume();
        }

        // HOLD: use C to hold/swap the current piece.
        if (code == KeyCode.C) {
            refreshBrick(eventListener.onHoldEvent(
                    new MoveEvent(EventType.DOWN, EventSource.USER)));
            SoundManager.playHold();
            event.consume();
        }

        if (code == KeyCode.DOWN || code == KeyCode.S) {
            moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
            // soft drop counts as movement; optional:
            SoundManager.playMove();
            event.consume();
        }
    }

    /**
     * Key handling when the game has finished.
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

    private boolean canHandleInput() {
        return gameState == GameState.PLAYING;
    }

    /**
     * Called by the game logic to set up the initial board and piece view.
     */
    public void initGameView(int[][] boardMatrix, ViewData brick) {
        initBackgroundCells(boardMatrix);
        initFallingBrick(brick);
        initNextBrick(brick);
        initHoldBrick(brick);
        startAutoDropTimer();
        startHudTimerIfNeeded();
        MusicPlayer.startBackgroundMusic();
    }

    private void initBackgroundCells(int[][] boardMatrix) {
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];
        for (int row = HIDDEN_TOP_ROWS; row < boardMatrix.length; row++) {
            for (int col = 0; col < boardMatrix[row].length; col++) {
                Rectangle cell = new Rectangle(BRICK_SIZE, BRICK_SIZE);

                // Transparent fill so the background image shows through,
                // but with a subtle grey stroke to make the grid obvious.
                cell.setFill(Color.TRANSPARENT);
                cell.setStroke(Color.rgb(55, 55, 55));   // grid line colour
                cell.setStrokeWidth(0.7);

                displayMatrix[row][col] = cell;
                gamePanel.add(cell, col, row - HIDDEN_TOP_ROWS);
            }
        }
    }


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
        updateBrickPanelPosition(brick);
    }

    private void initNextBrick(ViewData brick) {
        if (nextBrickPanel == null) {
            return;
        }
        int[][] nextData = brick.getNextBrickData();
        if (nextData == null || nextData.length == 0 || nextData[0].length == 0) {
            return;
        }

        nextBrickRectangles = new Rectangle[nextData.length][nextData[0].length];
        nextBrickPanel.getChildren().clear();

        for (int row = 0; row < nextData.length; row++) {
            for (int col = 0; col < nextData[row].length; col++) {
                Rectangle cell = new Rectangle(NEXT_BRICK_SIZE, NEXT_BRICK_SIZE);
                cell.setFill(getFillColor(nextData[row][col]));
                nextBrickRectangles[row][col] = cell;
                nextBrickPanel.add(cell, col, row);
            }
        }
    }

    private void initHoldBrick(ViewData brick) {
        if (holdBrickPanel == null) {
            return;
        }
        int[][] holdData = brick.getHoldBrickData();

        holdBrickPanel.getChildren().clear();

        if (holdData == null || holdData.length == 0 || holdData[0].length == 0) {
            holdBrickRectangles = null;
            return;
        }

        holdBrickRectangles = new Rectangle[holdData.length][holdData[0].length];

        for (int row = 0; row < holdData.length; row++) {
            for (int col = 0; col < holdData[row].length; col++) {
                Rectangle cell = new Rectangle(NEXT_BRICK_SIZE, NEXT_BRICK_SIZE);
                cell.setFill(getFillColor(holdData[row][col]));
                holdBrickRectangles[row][col] = cell;
                holdBrickPanel.add(cell, col, row);
            }
        }
    }

    private void refreshNextBrick(ViewData brick) {
        if (nextBrickPanel == null) {
            return;
        }
        int[][] nextData = brick.getNextBrickData();
        if (nextData == null || nextData.length == 0 || nextData[0].length == 0) {
            return;
        }

        if (nextBrickRectangles == null
                || nextBrickRectangles.length != nextData.length
                || nextBrickRectangles[0].length != nextData[0].length) {
            initNextBrick(brick);
            return;
        }

        for (int row = 0; row < nextData.length; row++) {
            for (int col = 0; col < nextData[row].length; col++) {
                Rectangle rect = nextBrickRectangles[row][col];
                if (rect != null) {
                    rect.setFill(getFillColor(nextData[row][col]));
                }
            }
        }
    }

    private void refreshHoldBrick(ViewData brick) {
        if (holdBrickPanel == null) {
            return;
        }
        int[][] holdData = brick.getHoldBrickData();

        if (holdData == null || holdData.length == 0 || holdData[0].length == 0) {
            holdBrickPanel.getChildren().clear();
            holdBrickRectangles = null;
            return;
        }

        if (holdBrickRectangles == null
                || holdBrickRectangles.length != holdData.length
                || holdBrickRectangles[0].length != holdData[0].length) {
            initHoldBrick(brick);
            return;
        }

        for (int row = 0; row < holdData.length; row++) {
            for (int col = 0; col < holdData[row].length; col++) {
                Rectangle rect = holdBrickRectangles[row][col];
                if (rect != null) {
                    rect.setFill(getFillColor(holdData[row][col]));
                }
            }
        }
    }

    private void startAutoDropTimer() {
        timeLine = new Timeline(new KeyFrame(
                Duration.millis(fallIntervalMs),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();
    }

    private void startHudTimerIfNeeded() {
        if (!timerEnabled || timerText == null) {
            return;
        }

        timerStartNanos = System.nanoTime();
        timerPauseStartNanos = 0L;
        timerPausedAccumNanos = 0L;
        timerRunning = true;

        if (hudTimer != null) {
            hudTimer.stop();
        }

        hudTimer = new Timeline(new KeyFrame(
                Duration.millis(200),
                ae -> updateTimerText()
        ));
        hudTimer.setCycleCount(Timeline.INDEFINITE);
        hudTimer.play();

        timerText.setText("Time 00:00");
    }

    private void updateTimerText() {
        if (!timerEnabled || timerText == null || timerStartNanos == 0L) {
            return;
        }

        long now = System.nanoTime();
        long effectiveNanos;

        if (timerRunning) {
            effectiveNanos = now - timerStartNanos - timerPausedAccumNanos;
        } else if (timerPauseStartNanos != 0L) {
            effectiveNanos = timerPauseStartNanos - timerStartNanos - timerPausedAccumNanos;
        } else {
            effectiveNanos = now - timerStartNanos - timerPausedAccumNanos;
        }

        if (effectiveNanos < 0L) {
            effectiveNanos = 0L;
        }

        long totalSeconds = effectiveNanos / 1_000_000_000L;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        timerText.setText(String.format("Time %02d:%02d", minutes, seconds));
    }

    /**
     * Moves the brickPanel so that the falling piece lines up exactly with
     * the background grid inside gamePanel. This uses the same cell size
     * and gaps as the background rectangles, so there is no wobble.
     */
    private void updateBrickPanelPosition(ViewData brick) {
        // Size of one cell including the 1px gap from the GridPane.
        double cellWidth = BRICK_SIZE + gamePanel.getHgap();
        double cellHeight = BRICK_SIZE + gamePanel.getVgap();

        // xPosition/yPosition are in board coordinates (0..columns/rows-1).
        double x = gamePanel.getLayoutX() + brick.getxPosition() * cellWidth;

        // Subtract hidden top rows so row 2 of the logical board appears
        // as row 0 in the visible grid.
        double y = gamePanel.getLayoutY()
                + (brick.getyPosition() - HIDDEN_TOP_ROWS) * cellHeight;

        brickPanel.setLayoutX(x);
        brickPanel.setLayoutY(y);
    }


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

    private Paint applyDimFactor(Paint base, double factor) {
        if (!(base instanceof Color)) {
            return base;
        }
        Color c = (Color) base;
        double r = c.getRed() * factor;
        double g = c.getGreen() * factor;
        double b = c.getBlue() * factor;
        return new Color(
                clamp01(r),
                clamp01(g),
                clamp01(b),
                c.getOpacity()
        );
    }

    private double clamp01(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }

    private Paint getBackgroundFillColor(int value) {
        Paint base = getFillColor(value);

        if (value == 0) {
            return base;
        }

        if (currentMode == GameMode.HYPER && backgroundDimFactor < 1.0) {
            return applyDimFactor(base, backgroundDimFactor);
        }
        return base;
    }

    private void refreshBrick(ViewData brick) {
        if (gameState == GameState.PLAYING) {
            updateBrickPanelPosition(brick);

            int[][] brickData = brick.getBrickData();
            for (int row = 0; row < brickData.length; row++) {
                for (int col = 0; col < brickData[row].length; col++) {
                    setRectangleData(brickData[row][col], rectangles[row][col]);
                }
            }

            refreshNextBrick(brick);
            refreshHoldBrick(brick);
        }
    }

    public void refreshGameBackground(int[][] board) {
        for (int row = HIDDEN_TOP_ROWS; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                setBackgroundRectangleData(board[row][col], displayMatrix[row][col]);
            }
        }
        updateDangerFromBoard(board);
    }

    private void setRectangleData(int colorCode, Rectangle rectangle) {
        rectangle.setFill(getFillColor(colorCode));
        rectangle.setArcHeight(9);
        rectangle.setArcWidth(9);
    }

    private void setBackgroundRectangleData(int colorCode, Rectangle rectangle) {
        rectangle.setFill(getBackgroundFillColor(colorCode));
        rectangle.setArcHeight(9);
        rectangle.setArcWidth(9);
    }

    private void moveDown(MoveEvent event) {
        if (gameState == GameState.PLAYING) {
            DownData downData = eventListener.onDownEvent(event);

            if (downData.getClearRow() != null &&
                    downData.getClearRow().getLinesRemoved() > 0) {

                String bonusText = "+" + downData.getClearRow().getScoreBonus();
                NotificationPanel notificationPanel = new NotificationPanel(bonusText);
                groupNotification.getChildren().add(notificationPanel);
                notificationPanel.showScore(groupNotification.getChildren());

                SoundManager.playLineClear();
            }

            refreshBrick(downData.getViewData());
        }

        gamePanel.requestFocus();
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void applyConfig(GameConfig config) {
        if (config == null) {
            return;
        }
        this.fallIntervalMs = config.getBaseFallIntervalMs();
        this.levelSpeedFactor = config.getLevelSpeedFactor();
        this.dangerVisibleRows = config.getDangerVisibleRows();
        this.backgroundDimFactor = config.getBackgroundDimFactor();
        this.timerEnabled = config.isShowTimer();

        if (!timerEnabled && timerText != null) {
            timerText.setText("");
        }
    }

    public void bindScore(IntegerProperty scoreProperty) {
        if (scoreText != null) {
            scoreText.textProperty().bind(scoreProperty.asString("Score %d"));
        }
    }

    public void bindLevel(IntegerProperty levelProperty) {
        if (levelText != null) {
            levelText.textProperty().bind(levelProperty.asString("Level %d"));
        }

        levelProperty.addListener((obs, oldLevel, newLevel) ->
                onLevelChanged(newLevel.intValue()));
    }

    public void bindCombo(IntegerProperty comboProperty) {
        if (comboText != null) {
            comboText.textProperty().bind(comboProperty.asString("Combo x%d"));
        }
    }

    private void onLevelChanged(int newLevel) {
        if (timeLine == null) {
            return;
        }

        double rate = 1.0 + (newLevel - 1) * levelSpeedFactor;
        timeLine.setRate(rate);
    }

    public void gameOver() {
        if (timeLine != null) {
            timeLine.stop();
        }

        if (hudTimer != null) {
            hudTimer.stop();
        }
        timerRunning = false;

        setGameState(GameState.GAME_OVER);

        if (brickPanel != null) {
            brickPanel.getChildren().clear();
        }

        SoundManager.playGameOver();
        MusicPlayer.stopBackgroundMusic();
    }

    public void newGame(javafx.event.ActionEvent actionEvent) {
        restartSameMode();
    }

    public void pauseGame(javafx.event.ActionEvent actionEvent) {
        togglePause();
    }

    private void togglePause() {
        if (timeLine == null) {
            setGameState(gameState == GameState.PLAYING
                    ? GameState.PAUSED
                    : GameState.PLAYING);
            return;
        }

        if (gameState == GameState.PLAYING) {
            timeLine.pause();
            setGameState(GameState.PAUSED);

            if (timerEnabled && timerRunning) {
                timerPauseStartNanos = System.nanoTime();
                timerRunning = false;
                if (hudTimer != null) {
                    hudTimer.pause();
                }
            }

            MusicPlayer.pauseBackgroundMusic();
        } else if (gameState == GameState.PAUSED) {
            timeLine.play();
            setGameState(GameState.PLAYING);
            gamePanel.requestFocus();

            if (timerEnabled && !timerRunning) {
                if (timerPauseStartNanos != 0L) {
                    long paused = System.nanoTime() - timerPauseStartNanos;
                    timerPausedAccumNanos += paused;
                    timerPauseStartNanos = 0L;
                }
                timerRunning = true;
                if (hudTimer != null) {
                    hudTimer.play();
                }
            }

            MusicPlayer.resumeBackgroundMusic();
        }
    }

    private void restartSameMode() {
        if (mainApp == null || currentMode == null) {
            return;
        }

        if (timeLine != null) {
            timeLine.stop();
        }
        if (hudTimer != null) {
            hudTimer.stop();
        }

        MusicPlayer.stopBackgroundMusic();

        mainApp.showGameScene(currentMode);
    }

    private void backToMainMenu() {
        if (timeLine != null) {
            timeLine.stop();
        }
        if (hudTimer != null) {
            hudTimer.stop();
        }

        MusicPlayer.stopBackgroundMusic();

        if (mainApp != null) {
            mainApp.showMainMenu();
        }
    }

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

    private void setDanger(boolean value) {
        if (isDanger.get() == value) {
            return;
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

    // === Progress HUD helpers ===

    public void clearProgressText() {
        if (progressText != null) {
            progressText.setText("");
        }
    }

    public void updateRushProgress(int linesCleared, int targetLines) {
        if (progressText == null) {
            return;
        }
        if (targetLines <= 0) {
            clearProgressText();
            return;
        }
        progressText.setText(String.format("Lines %d / %d", linesCleared, targetLines));
    }

    public void updateSurvivalStatus(int shields, int landingsUntilGarbage) {
        if (progressText == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Shields ").append(shields);

        if (landingsUntilGarbage >= 0) {
            sb.append(", Garbage in ").append(landingsUntilGarbage);
        }

        progressText.setText(sb.toString());
    }

    // === Best record HUD helpers ===

    /**
     * Updates best score and best Rush-40 time for the given mode.
     * Called from GameController when a run ends.
     */
    public void updateBestInfo(GameMode mode, int finalScore, double rushCompletionSeconds) {
        if (mode == null) {
            return;
        }
        int index = mode.ordinal();

        boolean newBestScore = false;
        boolean newBestTime = false;

        if (finalScore > bestScores[index]) {
            bestScores[index] = finalScore;
            newBestScore = true;
        }

        if (mode == GameMode.RUSH_40 && rushCompletionSeconds > 0.0) {
            double currentBest = bestRushTimes[index];
            if (currentBest < 0.0 || rushCompletionSeconds < currentBest) {
                bestRushTimes[index] = rushCompletionSeconds;
                newBestTime = true;
            }
        }

        refreshBestInfoForMode(mode, newBestScore, newBestTime);
    }

    /**
     * Refreshes best info for the given mode without highlighting.
     * Called when a new game starts so the player can see their targets.
     */
    public void refreshBestInfoForMode(GameMode mode) {
        refreshBestInfoForMode(mode, false, false);
    }

    private void refreshBestInfoForMode(GameMode mode, boolean highlightNewScore, boolean highlightNewTime) {
        if (bestText == null || mode == null) {
            return;
        }

        int index = mode.ordinal();
        int bestScore = bestScores[index];

        if (mode == GameMode.RUSH_40) {
            double bestTime = bestRushTimes[index];
            String timePart;
            if (bestTime > 0.0) {
                long totalSeconds = (long) bestTime;
                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;
                timePart = String.format("%02d:%02d", minutes, seconds);
            } else {
                timePart = "--:--";
            }

            StringBuilder text = new StringBuilder();
            text.append("Best Score ").append(bestScore);
            text.append(", Time ").append(timePart);

            if (highlightNewScore || highlightNewTime) {
                text.append(" (New!)");
            }
            bestText.setText(text.toString());
        } else {
            StringBuilder text = new StringBuilder();
            text.append("Best Score ").append(bestScore);
            if (highlightNewScore) {
                text.append(" (New!)");
            }
            bestText.setText(text.toString());
        }
    }

    // === Result screen helper (Phase 7.6) ===

    /**
     * Shows the final result panel and updates best records.
     *
     * @param mode              game mode that was played
     * @param finalScore        score at the end of the run
     * @param totalLinesCleared total lines cleared during the run
     * @param targetLines       Rush-40 target (0 for non-target modes)
     * @param timeSeconds       completion time in seconds (<=0 means "no time")
     * @param isWin             true if the player achieved the mode's win condition
     */
    public void showFinalResults(GameMode mode,
                                 int finalScore,
                                 int totalLinesCleared,
                                 int targetLines,
                                 double timeSeconds,
                                 boolean isWin) {

        // Update best score / best Rush-40 time.
        updateBestInfo(mode, finalScore, timeSeconds);

        // Populate result panel.
        if (gameOverPanel != null) {
            gameOverPanel.setResult(
                    mode,
                    finalScore,
                    totalLinesCleared,
                    targetLines,
                    timeSeconds,
                    isWin
            );
        }

        // Then run the normal game-over flow (stop timers, show overlay).
        gameOver();
    }
}

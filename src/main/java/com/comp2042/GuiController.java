package com.comp2042;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
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
    private static final int BRICK_SIZE = 30;

    // Preview brick size for the NEXT / HOLD panels.
    private static final int NEXT_BRICK_SIZE = 24;

    // Number of hidden rows at the top of the board (spawn area).
    private static final int HIDDEN_TOP_ROWS = 3;

    // === Configurable values (defaults tuned roughly for Classic mode) ===

    // Base fall interval (ms) before level scaling is applied.
    private int fallIntervalMs = 400;

    // How much faster each level becomes (e.g. 0.15 = +15% per level).
    private double levelSpeedFactor = 0.15;

    // How many top visible rows are considered "danger zone".
    private int dangerVisibleRows = 3;

    // How much to dim blocks in the background (1.0 = normal brightness).
    private double backgroundDimFactor = 1.0;

    // Optional extra nudge for previews (keep at 0 unless you want to tweak)
    private static final int PREVIEW_OFFSET_ROW = 0;
    private static final int PREVIEW_OFFSET_COL = 0;

    @FXML
    private GridPane gamePanel;          // main board grid

    @FXML
    private Group groupNotification;     // group for score popups

    @FXML
    private GridPane brickPanel;         // grid used to display current piece

    @FXML
    private GridPane ghostPanel;        // grid used to display shadow/ghost piece

    @FXML
    private GridPane holdBrickPanel;     // grid used to display HOLD preview

    // Three NEXT preview panels (top / middle / bottom of queue).
    @FXML
    private GridPane nextBrickPanelTop;
    @FXML
    private GridPane nextBrickPanelMid;
    @FXML
    private GridPane nextBrickPanelBottom;

    @FXML
    private GameOverPanel gameOverPanel; // overlay shown when the game ends

    @FXML
    private Pane pauseOverlay;           // overlay shown when the game is paused

    @FXML
    private BorderPane gameBoard;        // outer border of the board (for danger style)

    @FXML
    private Text modeText;               // shows current mode in the HUD

    @FXML
    private Text modeHintText;           // per-mode hint line (you可以保留逻辑，FXML不一定显示)

    @FXML
    private Text scoreText;              // shows current score in the HUD

    @FXML
    private Text levelText;              // shows current level in the HUD

    @FXML
    private Text linesText;              // shows total cleared lines in the HUD

    @FXML
    private Text timerText;              // shows elapsed time in the HUD (右下 INFO panel)

    @FXML
    private Text progressText;           // generic progress line (Rush / Survival, etc.)

    @FXML
    private Text bestText;               // best score / time info (右下 INFO panel)

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
    private Button pauseSettingsButton;

    @FXML
    private Button pauseMenuButton;

    // Background cells (for the board).
    private Rectangle[][] displayMatrix;

    // Current falling piece cells.
    private Rectangle[][] rectangles;

    // Shadow/ghost piece cells (landing position preview).
    private Rectangle[][] ghostRectangles;

    // NEXT preview cells (for 3 upcoming bricks).
    private Rectangle[][] nextBrickRectanglesTop;
    private Rectangle[][] nextBrickRectanglesMid;
    private Rectangle[][] nextBrickRectanglesBottom;

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

    // Game settings (key bindings)
    private GameSettings gameSettings;

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

    // === Board layout calibration (to keep bricks perfectly aligned and stop "shaking") ===
    private boolean boardLayoutCalibrated = false;
    private double boardOriginX;
    private double boardOriginY;
    private double boardCellWidth;
    private double boardCellHeight;

    // === GHOST PIECE SUPPORT (landing shadow driven by ViewData from the board) ===
    /** Last ViewData snapshot describing the current falling brick. */
    private ViewData lastViewData;

    /**
     * Called from Main.showGameScene() so this controller can access
     * navigation methods like showMainMenu().
     */
    void init(Main mainApp) {
        this.mainApp = mainApp;
        this.gameSettings = GameSettings.getInstance();
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

        // Snap to pixel to reduce blur.
        gamePanel.setSnapToPixel(true);
        if (gameBoard != null) {
            gameBoard.setSnapToPixel(true);
        }

        // The falling-brick overlay is positioned manually; don't let layout move it.
        if (brickPanel != null) {
            brickPanel.setVisible(false);          // hidden until layout is calibrated
            brickPanel.setManaged(false);          // excluded from parent layout
            brickPanel.setMouseTransparent(true);  // clicks go through
            brickPanel.setSnapToPixel(true);
        }

        // The ghost/shadow overlay is positioned manually; same setup as brickPanel.
        if (ghostPanel != null) {
            ghostPanel.setVisible(false);          // hidden until layout is calibrated
            ghostPanel.setManaged(false);          // excluded from parent layout
            ghostPanel.setMouseTransparent(true);  // clicks go through
            ghostPanel.setSnapToPixel(true);
        }

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
        if (pauseSettingsButton != null) {
            pauseSettingsButton.setOnAction(e -> goToSettings());
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

        // Pause toggle (uses settings - both P and ESC work)
        if (code == gameSettings.getPause() || code == gameSettings.getPauseAlt()) {
            togglePause();
            event.consume();
            return;
        }

        // Restart (uses settings)
        if (code == gameSettings.getRestart()) {
            restartSameMode();
            event.consume();
            return;
        }

        if (!canHandleInput()) {
            return;
        }

        // Move left (uses settings)
        if (code == gameSettings.getMoveLeft()) {
            refreshBrick(eventListener.onLeftEvent(
                    new MoveEvent(EventType.LEFT, EventSource.USER)));
            SoundManager.playMove();
            event.consume();
        }

        // Move right (uses settings)
        if (code == gameSettings.getMoveRight()) {
            refreshBrick(eventListener.onRightEvent(
                    new MoveEvent(EventType.RIGHT, EventSource.USER)));
            SoundManager.playMove();
            event.consume();
        }

        // Rotate (uses settings)
        if (code == gameSettings.getRotate()) {
            refreshBrick(eventListener.onRotateEvent(
                    new MoveEvent(EventType.ROTATE, EventSource.USER)));
            SoundManager.playRotate();
            event.consume();
        }

        // Hold (uses settings)
        if (code == gameSettings.getHold()) {
            refreshBrick(eventListener.onHoldEvent(
                    new MoveEvent(EventType.DOWN, EventSource.USER)));
            SoundManager.playHold();
            event.consume();
        }

        // Soft drop (uses settings)
        if (code == gameSettings.getSoftDrop()) {
            moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
            SoundManager.playMove();
            event.consume();
        }

        // Hard drop (uses settings)
        if (code == gameSettings.getHardDrop()) {
            DownData downData = eventListener.onHardDropEvent(
                    new MoveEvent(EventType.HARD_DROP, EventSource.USER));
            if (downData.getClearRow() != null &&
                    downData.getClearRow().getLinesRemoved() > 0) {

                String bonusText = "+" + downData.getClearRow().getScoreBonus();
                NotificationPanel notificationPanel = new NotificationPanel(bonusText);
                groupNotification.getChildren().add(notificationPanel);
                notificationPanel.showScore(groupNotification.getChildren());

                SoundManager.playLineClear();
            }
            refreshBrick(downData.getViewData());
            event.consume();
        }
    }

    /**
     * Shows a milestone notification for Rush 40 mode.
     * Called when the player reaches milestones (10, 20, 30, 40 lines).
     * Positioned higher than score bonuses to avoid overlap.
     *
     * @param message the milestone message to display (e.g., "10 Lines Cleared!")
     */
    public void showRushMilestone(String message) {
        if (groupNotification == null) {
            return;
        }
        NotificationPanel notificationPanel = new NotificationPanel(message);
        // Position milestone notifications higher (about 60 pixels above score bonuses)
        notificationPanel.setLayoutY(143.0); // Original is 203.0, so 203 - 60 = 143
        groupNotification.getChildren().add(notificationPanel);
        notificationPanel.showScore(groupNotification.getChildren());
        
        // Play a sound for milestone achievement (reuse line clear sound or could add a new one)
        SoundManager.playLineClear();
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
        initGhost(brick);
        initNextBrick(brick);
        initHoldBrick(brick);

        lastViewData = brick;
        boardLayoutCalibrated = false;

        Platform.runLater(() -> {
            calibrateBoardLayout();
            updateBrickPanelPosition(brick);
            refreshGhost(brick);
        });

        startAutoDropTimer();
        startHudTimerIfNeeded();
        MusicPlayer.startBackgroundMusic();
    }

    private int[][] copyBoardMatrix(int[][] src) {
        if (src == null || src.length == 0) {
            return null;
        }
        int[][] copy = new int[src.length][src[0].length];
        for (int i = 0; i < src.length; i++) {
            System.arraycopy(src[i], 0, copy[i], 0, src[i].length);
        }
        return copy;
    }

    /**
     * Initialise background cells: from the very beginning, use the final
     * rendering style (no "blur then become solid" jump).
     */
    private void initBackgroundCells(int[][] boardMatrix) {
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];

        for (int row = HIDDEN_TOP_ROWS; row < boardMatrix.length; row++) {
            for (int col = 0; col < boardMatrix[row].length; col++) {
                Rectangle cell = new Rectangle(BRICK_SIZE, BRICK_SIZE);

                // Use the same rendering path as runtime refresh:
                setBackgroundRectangleData(boardMatrix[row][col], cell);

                // Grey grid line on top of the fill.
                cell.setStroke(Color.rgb(55, 55, 55));
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
                setRectangleData(brickData[row][col], cell);
                rectangles[row][col] = cell;
                brickPanel.add(cell, col, row);
            }
        }
    }

    /**
     * Initialize the ghost/shadow piece overlay.
     * Creates rectangles for the shadow block at the landing position.
     */
    private void initGhost(ViewData brick) {
        if (ghostPanel == null) {
            return;
        }
        int[][] brickData = brick.getBrickData();
        ghostRectangles = new Rectangle[brickData.length][brickData[0].length];
        for (int row = 0; row < brickData.length; row++) {
            for (int col = 0; col < brickData[row].length; col++) {
                Rectangle cell = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                setGhostRectangleData(brickData[row][col], cell);
                ghostRectangles[row][col] = cell;
                ghostPanel.add(cell, col, row);
            }
        }
    }

    // === NEXT (3-queue) initialisation & refresh ===

    private void initNextBrick(ViewData brick) {
        int[][][] queue = brick.getNextQueue();

        if ((queue == null || queue.length == 0) && brick.getNextBrickData() != null) {
            queue = new int[][][]{brick.getNextBrickData()};
        }
        if (queue == null || queue.length == 0) {
            clearNextPanels();
            return;
        }

        nextBrickRectanglesTop = buildCenteredPreview(
                nextBrickPanelTop,
                queue.length > 0 ? queue[0] : null
        );
        nextBrickRectanglesMid = buildCenteredPreview(
                nextBrickPanelMid,
                queue.length > 1 ? queue[1] : null
        );
        nextBrickRectanglesBottom = buildCenteredPreview(
                nextBrickPanelBottom,
                queue.length > 2 ? queue[2] : null
        );
    }

    /**
     * Draws a brick shape into the given panel, centered inside a fixed 4x4
     * preview grid, so all pieces look nicely centered.
     */
    private Rectangle[][] buildCenteredPreview(GridPane panel, int[][] data) {
        if (panel == null) {
            return null;
        }
        panel.getChildren().clear();

        if (data == null || data.length == 0 || data[0].length == 0) {
            return null;
        }

        int rows = data.length;
        int cols = data[0].length;

        // bounding box of non-zero cells
        int minRow = rows, maxRow = -1, minCol = cols, maxCol = -1;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (data[r][c] != 0) {
                    if (r < minRow) minRow = r;
                    if (r > maxRow) maxRow = r;
                    if (c < minCol) minCol = c;
                    if (c > maxCol) maxCol = c;
                }
            }
        }

        if (maxRow == -1) {
            return null;
        }

        int shapeRows = maxRow - minRow + 1;
        int shapeCols = maxCol - minCol + 1;

        final int targetRows = 4;
        final int targetCols = 4;

        int offsetRow = (targetRows - shapeRows) / 2 + PREVIEW_OFFSET_ROW;
        int offsetCol = (targetCols - shapeCols) / 2 + PREVIEW_OFFSET_COL;

        Rectangle[][] rects = new Rectangle[targetRows][targetCols];

        for (int r = 0; r < shapeRows; r++) {
            for (int c = 0; c < shapeCols; c++) {
                int value = data[minRow + r][minCol + c];
                if (value == 0) {
                    continue;
                }

                Rectangle cell = new Rectangle(NEXT_BRICK_SIZE, NEXT_BRICK_SIZE);
                setRectangleData(value, cell);

                int gridRow = offsetRow + r;
                int gridCol = offsetCol + c;

                if (gridRow >= 0 && gridRow < targetRows
                        && gridCol >= 0 && gridCol < targetCols) {
                    rects[gridRow][gridCol] = cell;
                    panel.add(cell, gridCol, gridRow);
                }
            }
        }

        return rects;
    }

    private void clearNextPanels() {
        if (nextBrickPanelTop != null) {
            nextBrickPanelTop.getChildren().clear();
        }
        if (nextBrickPanelMid != null) {
            nextBrickPanelMid.getChildren().clear();
        }
        if (nextBrickPanelBottom != null) {
            nextBrickPanelBottom.getChildren().clear();
        }
        nextBrickRectanglesTop = null;
        nextBrickRectanglesMid = null;
        nextBrickRectanglesBottom = null;
    }

    private void refreshNextBrick(ViewData brick) {
        int[][][] queue = brick.getNextQueue();

        if ((queue == null || queue.length == 0) && brick.getNextBrickData() != null) {
            queue = new int[][][]{brick.getNextBrickData()};
        }
        if (queue == null || queue.length == 0) {
            clearNextPanels();
            return;
        }

        nextBrickRectanglesTop = buildCenteredPreview(
                nextBrickPanelTop,
                queue.length > 0 ? queue[0] : null
        );
        nextBrickRectanglesMid = buildCenteredPreview(
                nextBrickPanelMid,
                queue.length > 1 ? queue[1] : null
        );
        nextBrickRectanglesBottom = buildCenteredPreview(
                nextBrickPanelBottom,
                queue.length > 2 ? queue[2] : null
        );
    }

    // === HOLD initialisation & refresh ===

    private void initHoldBrick(ViewData brick) {
        if (holdBrickPanel == null) {
            return;
        }
        int[][] holdData = brick.getHoldBrickData();

        holdBrickRectangles = buildCenteredPreview(holdBrickPanel, holdData);
    }

    private void refreshHoldBrick(ViewData brick) {
        if (holdBrickPanel == null) {
            return;
        }
        initHoldBrick(brick);
    }

    // === GHOST / SHADOW initialisation & refresh ===

    /**
     * Refresh the ghost/shadow piece to show where the current brick will land.
     * Updates both position and visual representation.
     */
    private void refreshGhost(ViewData brick) {
        if (ghostPanel == null || brick == null || gameState != GameState.PLAYING) {
            return;
        }

        // Initialize ghost rectangles if not already created
        if (ghostRectangles == null) {
            initGhost(brick);
        }

        // Update position to landing location
        updateGhostPanelPosition(brick);

        // Update visual representation
        int[][] brickData = brick.getBrickData();
        if (ghostRectangles != null && ghostRectangles.length == brickData.length
                && ghostRectangles[0].length == brickData[0].length) {
            for (int row = 0; row < brickData.length; row++) {
                for (int col = 0; col < brickData[row].length; col++) {
                    if (ghostRectangles[row][col] != null) {
                        setGhostRectangleData(brickData[row][col], ghostRectangles[row][col]);
                    }
                }
            }
        } else {
            // If dimensions changed, reinitialize
            ghostPanel.getChildren().clear();
            initGhost(brick);
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
     * Compute the origin and cell size of the main board once, so the
     * falling brick layer can be aligned without "jitter".
     */
    private void calibrateBoardLayout() {
        if (boardLayoutCalibrated || gamePanel == null || brickPanel == null) {
            return;
        }

        if (displayMatrix == null
                || displayMatrix.length <= HIDDEN_TOP_ROWS
                || displayMatrix[HIDDEN_TOP_ROWS].length < 2
                || displayMatrix[HIDDEN_TOP_ROWS][0] == null
                || displayMatrix[HIDDEN_TOP_ROWS][1] == null) {
            return;
        }

        // Make sure CSS and layout are applied
        gamePanel.applyCss();
        gamePanel.layout();

        Rectangle originCell = displayMatrix[HIDDEN_TOP_ROWS][0];
        Rectangle nextCellInRow = displayMatrix[HIDDEN_TOP_ROWS][1];

        Bounds originScene = originCell.localToScene(originCell.getBoundsInLocal());
        Bounds originInParent = brickPanel.getParent().sceneToLocal(originScene);

        Bounds nextScene = nextCellInRow.localToScene(nextCellInRow.getBoundsInLocal());
        Bounds nextInParent = brickPanel.getParent().sceneToLocal(nextScene);

        double originX = originInParent.getMinX();
        double originY = originInParent.getMinY();

        double cellWidth = nextInParent.getMinX() - originX;

        double cellHeight;
        if (displayMatrix.length > HIDDEN_TOP_ROWS + 1
                && displayMatrix[HIDDEN_TOP_ROWS + 1][0] != null) {
            Rectangle belowCell = displayMatrix[HIDDEN_TOP_ROWS + 1][0];
            Bounds belowScene = belowCell.localToScene(belowCell.getBoundsInLocal());
            Bounds belowInParent = brickPanel.getParent().sceneToLocal(belowScene);
            cellHeight = belowInParent.getMinY() - originY;
        } else {
            cellHeight = BRICK_SIZE + gamePanel.getVgap();
        }

        boardOriginX = originX;
        boardOriginY = originY;
        boardCellWidth = cellWidth;
        boardCellHeight = cellHeight;

        boardLayoutCalibrated = true;

        // Now it's safe to show the falling-brick overlay and ghost overlay
        brickPanel.setVisible(true);
        if (ghostPanel != null) {
            ghostPanel.setVisible(true);
        }
    }

    /**
     * Moves the brickPanel so that the falling piece lines up exactly with
     * the background grid inside gamePanel.
     */
    private void updateBrickPanelPosition(ViewData brick) {
        if (!boardLayoutCalibrated || brickPanel == null || brick == null) {
            return;
        }

        double x = boardOriginX + brick.getxPosition() * boardCellWidth;
        double y = boardOriginY + (brick.getyPosition() - HIDDEN_TOP_ROWS) * boardCellHeight;

        brickPanel.setLayoutX(Math.round(x));
        brickPanel.setLayoutY(Math.round(y));
    }

    /**
     * Moves the ghostPanel so that the shadow piece lines up exactly with
     * the background grid at the landing position.
     */
    private void updateGhostPanelPosition(ViewData brick) {
        if (!boardLayoutCalibrated || ghostPanel == null || brick == null) {
            return;
        }

        double x = boardOriginX + brick.getGhostXPosition() * boardCellWidth;
        double y = boardOriginY + (brick.getGhostYPosition() - HIDDEN_TOP_ROWS) * boardCellHeight;

        ghostPanel.setLayoutX(Math.round(x));
        ghostPanel.setLayoutY(Math.round(y));
    }

    // === Colour logic: active brick, background bricks and ghost ===

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

    /** Colour for the currently falling brick (always full brightness). */
    private Paint getActiveBrickFillColor(int value) {
        if (value == 0) {
            return Color.TRANSPARENT;
        }
        return getFillColor(value);
    }

    /**
     * Colour for settled blocks in the background.
     *
     * Classic / Survival / Rush-40: landed blocks are drawn with normal colours.
     * Hyper: landed blocks are fully transparent; they still exist in the matrix for
     * collision / line clear, but visually "merge into" the empty background.
     */
    private Paint getBackgroundFillColor(int value) {
        if (value == 0) {
            return Color.TRANSPARENT;
        }

        Paint base = getFillColor(value);

        if (currentMode == GameMode.HYPER) {
            return Color.TRANSPARENT;
        }
        return base;
    }

    /** Draw a single cell of the active falling layer (no background). */
    private void setRectangleData(int colorCode, Rectangle rectangle) {
        rectangle.setFill(getActiveBrickFillColor(colorCode));
        rectangle.setArcHeight(9);
        rectangle.setArcWidth(9);
    }

    /** Draw a single cell of the background matrix. */
    private void setBackgroundRectangleData(int colorCode, Rectangle rectangle) {
        rectangle.setFill(getBackgroundFillColor(colorCode));
        rectangle.setArcHeight(9);
        rectangle.setArcWidth(9);
    }

    /**
     * Draw a single cell of the ghost/shadow layer.
     * Uses semi-transparent fill with an outline to make it visually distinct.
     */
    private void setGhostRectangleData(int colorCode, Rectangle rectangle) {
        if (colorCode == 0) {
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setStroke(null);
        } else {
            Paint baseColor = getFillColor(colorCode);
            // Make it semi-transparent (30% opacity)
            if (baseColor instanceof Color) {
                Color c = (Color) baseColor;
                Color ghostColor = new Color(
                        c.getRed(),
                        c.getGreen(),
                        c.getBlue(),
                        0.3  // 30% opacity
                );
                rectangle.setFill(ghostColor);
            } else {
                rectangle.setFill(baseColor);
            }
            // Add a subtle outline to make it more visible
            rectangle.setStroke(Color.WHITE);
            rectangle.setStrokeWidth(1.5);
            rectangle.setArcHeight(9);
            rectangle.setArcWidth(9);
        }
    }

    private void refreshBrick(ViewData brick) {
        if (gameState == GameState.PLAYING) {
            lastViewData = brick;

            updateBrickPanelPosition(brick);

            int[][] brickData = brick.getBrickData();
            for (int row = 0; row < brickData.length; row++) {
                for (int col = 0; col < brickData[row].length; col++) {
                    setRectangleData(brickData[row][col], rectangles[row][col]);
                }
            }

            refreshNextBrick(brick);
            refreshHoldBrick(brick);
            refreshGhost(brick);
        }
    }

    public void refreshGameBackground(int[][] board) {
        for (int row = HIDDEN_TOP_ROWS; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                setBackgroundRectangleData(board[row][col], displayMatrix[row][col]);
            }
        }
        updateDangerFromBoard(board);

        // After the background changes we must recompute and redraw the landing shadow.
        if (lastViewData != null) {
            refreshGhost(lastViewData);
        }
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

    public void bindLines(IntegerProperty linesProperty) {
        if (linesText != null) {
            linesText.textProperty().bind(linesProperty.asString("Lines %d"));
        }
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

        if (ghostPanel != null) {
            ghostPanel.getChildren().clear();
            ghostRectangles = null;
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

    private void goToSettings() {
        // Pause the game while in settings
        if (timeLine != null) {
            timeLine.pause();
        }
        if (hudTimer != null && timerEnabled && timerRunning) {
            timerPauseStartNanos = System.nanoTime();
            timerRunning = false;
            hudTimer.pause();
        }
        MusicPlayer.pauseBackgroundMusic();

        if (mainApp != null && currentMode != null) {
            // Pass the current game mode so we can return to it after settings
            mainApp.showSettingsScene(currentMode);
        }
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

    private void refreshBestInfoForMode(GameMode mode,
                                        boolean highlightNewScore,
                                        boolean highlightNewTime) {
        if (bestText == null || mode == null) {
            return;
        }

        int index = mode.ordinal();
        int bestScore = bestScores[index];

        if (mode == GameMode.RUSH_40) {
            double bestTime = bestRushTimes[index];

            StringBuilder text = new StringBuilder();
            if (bestTime > 0.0) {
                long totalSeconds = (long) bestTime;
                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;
                text.append("Best Time ")
                    .append(String.format("%02d:%02d", minutes, seconds));
            } else {
                text.append("Best Time --:--");
            }

            if (highlightNewTime) {
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

    // === Result screen helper ===

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

    // === GHOST / LANDING SHADOW RENDERING ===
    // Shadow rendering code removed - ready for fresh implementation
}

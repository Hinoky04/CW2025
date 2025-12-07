package com.comp2042.controllers;

import com.comp2042.Main;
import com.comp2042.models.DownData;
import com.comp2042.models.GameConfig;
import com.comp2042.models.GameMode;
import com.comp2042.models.GameSettings;
import com.comp2042.models.GameState;
import com.comp2042.models.MoveEvent;
import com.comp2042.models.ViewData;
import com.comp2042.helpers.*;
import com.comp2042.interfaces.InputEventListener;
import com.comp2042.ui.GameOverPanel;
import com.comp2042.audio.MusicPlayer;
import com.comp2042.audio.SoundManager;

import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * JavaFX controller for the main game screen.
 * Handles keyboard input, HUD, pause/game-over overlays and drawing the board.
 */
public class GuiController implements Initializable {

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

    // Rendering data moved to GuiRenderingHelper

    // Listener that sends user input events to the game logic.
    private InputEventListener eventListener;

    // Timer for automatic down movement (accessed via timerHelper).
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

    // Game settings (key bindings)
    private GameSettings gameSettings;

    // Timer configuration and state (moved to GuiTimerHelper).
    private boolean timerEnabled;

    // === GHOST PIECE SUPPORT (landing shadow driven by ViewData from the board) ===
    /** Last ViewData snapshot describing the current falling brick. */
    private ViewData lastViewData;
    
    // === Helper classes (extracted for smaller files) ===
    private GuiColorHelper colorHelper;
    private GuiRenderingHelper renderingHelper;
    private GuiTimerHelper timerHelper;
    private GuiLayoutHelper layoutHelper;
    private GuiHudHelper hudHelper;
    private GuiDangerHelper dangerHelper;
    private GuiInputHandler inputHandler;
    private GuiStateManager stateManager;
    private GuiNavigationHandler navigationHandler;
    private GuiNotificationHandler notificationHandler;

    /**
     * Called from Main.showGameScene() so this controller can access
     * navigation methods like showMainMenu().
     */
    public void init(Main mainApp) {
        this.mainApp = mainApp;
        this.gameSettings = GameSettings.getInstance();
        
        // Initialize helper classes
        colorHelper = new GuiColorHelper(currentMode != null ? currentMode : GameMode.CLASSIC);
        renderingHelper = new GuiRenderingHelper(
                gamePanel, brickPanel, ghostPanel,
                holdBrickPanel, nextBrickPanelTop, nextBrickPanelMid, nextBrickPanelBottom,
                colorHelper
        );
        timerHelper = new GuiTimerHelper(timerText, this::moveDown);
        layoutHelper = new GuiLayoutHelper(
                gamePanel, brickPanel, ghostPanel,
                () -> renderingHelper != null ? renderingHelper.getDisplayMatrix() : null
        );
        hudHelper = new GuiHudHelper(
                scoreText, levelText, linesText, comboText,
                timerText, progressText, bestText, modeText, modeHintText
        );
        dangerHelper = new GuiDangerHelper(dangerVisibleRows, dangerText, gameBoard, isDanger);
        
        // Initialize additional helper classes
        stateManager = new GuiStateManager(isPause, isGameOver, pauseOverlay, gameOverPanel, dangerHelper, colorHelper, currentMode != null ? currentMode : GameMode.CLASSIC);
        navigationHandler = new GuiNavigationHandler(mainApp, currentMode != null ? currentMode : GameMode.CLASSIC, timerHelper, stateManager);
        notificationHandler = new GuiNotificationHandler(groupNotification);
        inputHandler = new GuiInputHandler(
                gameSettings,
                gameState,
                null, // Will be set when eventListener is set
                this::refreshBrick,
                this::moveDown,
                this::handleHardDrop,
                this::togglePause,
                navigationHandler::restartSameMode,
                navigationHandler::backToMainMenu,
                () -> gameState
        );
    }

    /**
     * Called from GameController so the GUI knows which mode is running.
     * Used when restarting the same mode.
     */
    public void setGameMode(GameMode mode) {
        this.currentMode = (mode != null) ? mode : GameMode.CLASSIC;

        // Update color helper with new mode
        if (colorHelper != null) {
            colorHelper = new GuiColorHelper(this.currentMode);
            // Update rendering helper with new color helper
            if (renderingHelper != null) {
                renderingHelper = new GuiRenderingHelper(
                        gamePanel, brickPanel, ghostPanel,
                        holdBrickPanel, nextBrickPanelTop, nextBrickPanelMid, nextBrickPanelBottom,
                        colorHelper
                );
                renderingHelper.setGameState(gameState);
            }
        }
        
        // Update state manager with new mode
        if (stateManager != null) {
            stateManager = new GuiStateManager(isPause, isGameOver, pauseOverlay, gameOverPanel, dangerHelper, colorHelper, this.currentMode);
        }
        
        // Update navigation handler with new mode
        if (navigationHandler != null) {
            navigationHandler = new GuiNavigationHandler(mainApp, this.currentMode, timerHelper, stateManager);
        }

        // Use HUD helper for mode updates
        if (hudHelper != null) {
            hudHelper.setMode(this.currentMode);
            hudHelper.clearProgressText();
            hudHelper.refreshBestInfoForMode(this.currentMode);
        }
    }

    /**
     * Central helper for changing game state.
     * Keeps internal flags and overlays in sync.
     */
    private void setGameState(GameState newState) {
        if (stateManager != null) {
            stateManager.setGameState(newState);
            gameState = stateManager.getGameState();
        } else {
            gameState = newState;
        }
        
        // Update rendering helper with new game state
        if (renderingHelper != null) {
            renderingHelper.setGameState(newState);
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
        gamePanel.setOnKeyPressed(event -> {
            if (inputHandler != null) {
                inputHandler.handleKeyPressed(event);
            }
        });

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
        if (dangerHelper != null) {
            dangerHelper.setDanger(false);
        }

        if (hudHelper != null) {
            hudHelper.clearTimerText();
            hudHelper.clearProgressText();
            hudHelper.setBestTextDefault();
        }
    }

    // Input handling moved to GuiInputHandler

    /**
     * Shows a milestone notification for Rush 40 mode.
     * Called when the player reaches milestones (10, 20, 30, 40 lines).
     */
    public void showRushMilestone(String message) {
        if (notificationHandler != null) {
            notificationHandler.showRushMilestone(message);
        }
    }

    /**
     * Shows a congratulations message when Rush 40 mode is completed.
     */
    public void showRush40Congratulations() {
        if (notificationHandler != null) {
            notificationHandler.showRush40Congratulations();
        }
    }
    
    /**
     * Handles hard drop with notification.
     */
    private void handleHardDrop(DownData downData) {
        if (notificationHandler != null) {
            notificationHandler.showScoreBonus(downData);
        }
    }

    /**
     * Called by the game logic to set up the initial board and piece view.
     */
    public void initGameView(int[][] boardMatrix, ViewData brick) {
        // Reset game over state when starting a new game
        if (stateManager != null) {
            stateManager.resetGameOverState();
        }
        
        initBackgroundCells(boardMatrix);
        initFallingBrick(brick);
        initGhost(brick);
        initNextBrick(brick);
        initHoldBrick(brick);

        if (renderingHelper != null) {
            renderingHelper.setLastViewData(brick);
            lastViewData = brick;
        }
        if (layoutHelper != null) {
            layoutHelper.reset();
        }

        Platform.runLater(() -> {
            calibrateBoardLayout();
            if (layoutHelper != null && layoutHelper.isCalibrated()) {
                updateBrickPanelPosition(brick);
                refreshGhost(brick);
            }
        });

        startAutoDropTimer();
        startHudTimerIfNeeded();
        try {
        MusicPlayer.startBackgroundMusic();
        } catch (Exception | Error e) {
            System.err.println("Warning: Could not start background music: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Rendering methods moved to GuiRenderingHelper
    private void initBackgroundCells(int[][] boardMatrix) {
        if (renderingHelper != null) {
            renderingHelper.initBackgroundCells(boardMatrix);
            // Update layout helper with displayMatrix after initialization
            if (layoutHelper != null) {
                layoutHelper = new GuiLayoutHelper(
                        gamePanel, brickPanel, ghostPanel,
                        () -> renderingHelper.getDisplayMatrix()
                );
            }
        }
    }

    private void initFallingBrick(ViewData brick) {
        if (renderingHelper != null) {
            renderingHelper.initFallingBrick(brick);
        }
    }

    private void initGhost(ViewData brick) {
        if (renderingHelper != null) {
            renderingHelper.initGhost(brick);
        }
    }

    private void initNextBrick(ViewData brick) {
        if (renderingHelper != null) {
            renderingHelper.initNextBrick(brick);
        }
    }

    private void initHoldBrick(ViewData brick) {
        if (renderingHelper != null) {
            renderingHelper.initHoldBrick(brick);
        }
    }

    private void refreshGhost(ViewData brick) {
        if (renderingHelper != null && layoutHelper != null) {
            renderingHelper.refreshGhost(brick, layoutHelper::updateGhostPanelPosition);
        }
    }

    // Timer methods moved to GuiTimerHelper
    private void startAutoDropTimer() {
        if (timerHelper != null) {
            timerHelper.startAutoDropTimer();
            timeLine = timerHelper.getTimeLine();
        }
    }

    private void startHudTimerIfNeeded() {
        if (timerHelper != null) {
            timerHelper.startHudTimerIfNeeded();
        }
    }

    // Layout methods moved to GuiLayoutHelper
    private void calibrateBoardLayout() {
        if (layoutHelper != null) {
            layoutHelper.calibrateBoardLayout();
        }
    }

    private void updateBrickPanelPosition(ViewData brick) {
        if (layoutHelper != null) {
            layoutHelper.updateBrickPanelPosition(brick);
        }
    }

    // Color methods moved to GuiColorHelper
    // Rendering methods moved to GuiRenderingHelper
    
    private void refreshBrick(ViewData brick) {
        if (renderingHelper != null && layoutHelper != null) {
            renderingHelper.refreshBrick(brick, layoutHelper::updateBrickPanelPosition, layoutHelper::updateGhostPanelPosition);
            lastViewData = renderingHelper.getLastViewData();
        }
    }

    public void refreshGameBackground(int[][] board) {
        if (renderingHelper != null && dangerHelper != null && layoutHelper != null) {
            renderingHelper.refreshGameBackground(board, dangerHelper::updateDangerFromBoard, this::refreshGhost);
        }
    }

    private void moveDown(MoveEvent event) {
        if (gameState == GameState.PLAYING) {
            DownData downData = eventListener.onDownEvent(event);

            if (notificationHandler != null) {
                notificationHandler.showScoreBonus(downData);
            }

            refreshBrick(downData.getViewData());
        }

        gamePanel.requestFocus();
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
        // Update input handler with event listener
        if (inputHandler != null && eventListener != null) {
            inputHandler = new GuiInputHandler(
                    gameSettings,
                    gameState,
                    eventListener,
                    this::refreshBrick,
                    this::moveDown,
                    this::handleHardDrop,
                    this::togglePause,
                    navigationHandler != null ? navigationHandler::restartSameMode : this::restartSameMode,
                    navigationHandler != null ? navigationHandler::backToMainMenu : this::backToMainMenu,
                    () -> gameState
            );
        }
    }

    public void applyConfig(GameConfig config) {
        if (config == null) {
            return;
        }
        this.fallIntervalMs = config.getBaseFallIntervalMs();
        this.levelSpeedFactor = config.getLevelSpeedFactor();
        this.dangerVisibleRows = config.getDangerVisibleRows();
        this.timerEnabled = config.isShowTimer();

        // Update helper classes with config
        if (timerHelper != null) {
            timerHelper.setFallIntervalMs(fallIntervalMs);
            timerHelper.setLevelSpeedFactor(levelSpeedFactor);
            timerHelper.setTimerEnabled(timerEnabled);
        }
        if (dangerHelper != null) {
            dangerHelper = new GuiDangerHelper(dangerVisibleRows, dangerText, gameBoard, isDanger);
        }
        if (hudHelper != null && !timerEnabled) {
            hudHelper.clearTimerText();
        }
    }

    // HUD binding methods moved to GuiHudHelper
    public void bindScore(IntegerProperty scoreProperty) {
        if (hudHelper != null) {
            hudHelper.bindScore(scoreProperty);
        }
    }

    public void bindLevel(IntegerProperty levelProperty) {
        if (hudHelper != null) {
            hudHelper.bindLevel(levelProperty);
        }

        levelProperty.addListener((obs, oldLevel, newLevel) -> {
            if (timerHelper != null) {
                timerHelper.onLevelChanged(newLevel.intValue());
            }
        });
    }

    public void bindLines(IntegerProperty linesProperty) {
        if (hudHelper != null) {
            hudHelper.bindLines(linesProperty);
        }
    }

    public void bindCombo(IntegerProperty comboProperty) {
        if (hudHelper != null) {
            hudHelper.bindCombo(comboProperty);
        }
    }

    public void gameOver() {
        if (timerHelper != null) {
            timerHelper.stop();
            timeLine = timerHelper.getTimeLine();
        }

        if (stateManager != null) {
            stateManager.setGameOver();
            gameState = stateManager.getGameState();
        } else {
            setGameState(GameState.GAME_OVER);
        }

        if (renderingHelper != null) {
            renderingHelper.clearBrickPanel();
            renderingHelper.clearGhostPanel();
        }

        SoundManager.playGameOver();
        try {
            MusicPlayer.stopBackgroundMusic();
        } catch (Exception | Error e) {
            System.err.println("Warning: Could not stop background music: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void newGame(javafx.event.ActionEvent actionEvent) {
        restartSameMode();
    }

    public void pauseGame(javafx.event.ActionEvent actionEvent) {
        togglePause();
    }

    private void togglePause() {
        if (timerHelper == null || timerHelper.getTimeLine() == null) {
            setGameState(gameState == GameState.PLAYING
                    ? GameState.PAUSED
                    : GameState.PLAYING);
            return;
        }

        if (gameState == GameState.PLAYING) {
            timerHelper.pause();
            timeLine = timerHelper.getTimeLine();
            setGameState(GameState.PAUSED);
            try {
                MusicPlayer.pauseBackgroundMusic();
            } catch (Exception | Error e) {
                System.err.println("Warning: Could not pause background music: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (gameState == GameState.PAUSED) {
            timerHelper.resume();
            timeLine = timerHelper.getTimeLine();
            setGameState(GameState.PLAYING);
            gamePanel.requestFocus();
            try {
                MusicPlayer.resumeBackgroundMusic();
            } catch (Exception | Error e) {
                System.err.println("Warning: Could not resume background music: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Update input handler with new game state
        if (inputHandler != null && eventListener != null) {
            inputHandler = new GuiInputHandler(
                    gameSettings,
                    gameState,
                    eventListener,
                    this::refreshBrick,
                    this::moveDown,
                    this::handleHardDrop,
                    this::togglePause,
                    navigationHandler != null ? navigationHandler::restartSameMode : this::restartSameMode,
                    navigationHandler != null ? navigationHandler::backToMainMenu : this::backToMainMenu,
                    () -> gameState
            );
        }
    }

    // Navigation methods moved to GuiNavigationHandler
    private void restartSameMode() {
        if (navigationHandler != null) {
            navigationHandler.restartSameMode();
        }
    }

    private void goToSettings() {
        if (navigationHandler != null) {
            navigationHandler.goToSettings();
        }
    }

    private void backToMainMenu() {
        if (navigationHandler != null) {
            navigationHandler.backToMainMenu();
        }
    }

    // Danger zone methods moved to GuiDangerHelper

    // === Progress HUD helpers ===
    // Moved to GuiHudHelper

    public void clearProgressText() {
        if (hudHelper != null) {
            hudHelper.clearProgressText();
        }
    }

    public void updateRushProgress(int linesCleared, int targetLines) {
        if (hudHelper != null) {
            hudHelper.updateRushProgress(linesCleared, targetLines);
        }
    }

    public void updateSurvivalStatus(int shields, int landingsUntilGarbage) {
        if (hudHelper != null) {
            hudHelper.updateSurvivalStatus(shields, landingsUntilGarbage);
        }
    }

    // === Best record HUD helpers ===
    // Moved to GuiHudHelper

    /**
     * Updates best score and best Rush-40 time for the given mode.
     * Called from GameController when a run ends.
     */
    public void updateBestInfo(GameMode mode, int finalScore, double rushCompletionSeconds) {
        if (hudHelper != null) {
            hudHelper.updateBestInfo(mode, finalScore, rushCompletionSeconds);
        }
    }

    /**
     * Refreshes best info for the given mode without highlighting.
     * Called when a new game starts so the player can see their targets.
     */
    public void refreshBestInfoForMode(GameMode mode) {
        if (hudHelper != null) {
            hudHelper.refreshBestInfoForMode(mode);
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
     * @param timeSeconds       completion time in seconds ({@code <=0} means "no time")
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

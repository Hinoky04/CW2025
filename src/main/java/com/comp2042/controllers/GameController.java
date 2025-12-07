package com.comp2042.controllers;

import com.comp2042.models.Board;
import com.comp2042.models.ClearRow;
import com.comp2042.models.DownData;
import com.comp2042.models.EventSource;
import com.comp2042.models.GameConfig;
import com.comp2042.models.GameMode;
import com.comp2042.models.MoveEvent;
import com.comp2042.models.Score;
import com.comp2042.models.SimpleBoard;
import com.comp2042.models.ViewData;
import com.comp2042.interfaces.InputEventListener;
import com.comp2042.mode.RushModeHandler;
import com.comp2042.mode.SurvivalModeHandler;

/**
 * Connects the board (game logic) with the JavaFX GUI.
 * Receives input events from GuiController and updates the board.
 */
public class GameController implements InputEventListener {

    // Board size (easier to change here than using magic numbers everywhere).
    private static final int BOARD_ROWS = 25;
    private static final int BOARD_COLUMNS = 10;

    // Core game model and GUI controller.
    private final Board board;
    private final GuiController guiController;

    // Selected game mode for this run (Classic, Survival, Hyper, Rush-40).
    private final GameMode gameMode;

    // Immutable configuration derived from the chosen mode.
    private final GameConfig config;

    // Mode-specific handlers (only initialized for relevant modes)
    private SurvivalModeHandler survivalHandler;
    private RushModeHandler rushHandler;

    // Total lines cleared in this run (all modes).
    private int totalLinesCleared = 0;

    /**
     * Creates a new game controller and uses the default board size
     * for the selected mode. Behavior diverges via GameConfig values.
     *
     * @param guiController the GUI controller for rendering and input
     * @param gameMode the game mode to play (Classic, Survival, Hyper, or Rush 40)
     */
    public GameController(GuiController guiController, GameMode gameMode) {
        this.guiController = guiController;
        this.gameMode = gameMode;
        this.config = gameMode.getConfig();
        this.board = new SimpleBoard(BOARD_ROWS, BOARD_COLUMNS);

        // Initialize mode-specific handlers
        if (gameMode == GameMode.SURVIVAL) {
            this.survivalHandler = new SurvivalModeHandler(board, config);
        }
        
        if (gameMode == GameMode.RUSH_40) {
            int target = config.getTargetLinesToWin();
            if (target > 0) {
                this.rushHandler = new RushModeHandler(target, config);
            }
        }

        initialiseGame();
    }

    /**
     * One-time setup: create first brick, hook GUI listeners, bind HUD fields.
     */
    private void initialiseGame() {
        board.createNewBrick();
        guiController.setEventListener(this);

        guiController.setGameMode(gameMode);
        guiController.applyConfig(config);

        guiController.initGameView(board.getBoardMatrix(), board.getViewData());

        Score score = board.getScore();
        guiController.bindScore(score.scoreProperty());
        guiController.bindLevel(score.levelProperty());
        guiController.bindLines(score.totalLinesProperty());  // LINES counter on HUD
        guiController.bindCombo(score.comboProperty());

        totalLinesCleared = 0;

        if (rushHandler != null) {
            rushHandler.start();
        }

        initialiseProgressHud(score);
    }

    /**
     * Initializes the generic progress HUD line depending on the current mode.
     * Sets up mode-specific progress tracking (e.g., Rush 40 line count, Survival shields).
     *
     * @param score the Score object to track progress
     */
    private void initialiseProgressHud(Score score) {
        if (rushHandler != null) {
            guiController.updateRushProgress(rushHandler.getLinesCleared(), rushHandler.getTargetLines());
            return;
        }

        if (survivalHandler != null) {
            int baseThreshold = config.getMaxNoClearBeforeGarbage();
            if (baseThreshold > 0) {
                int landingsUntilGarbage = survivalHandler.getLandingsUntilGarbage(score, baseThreshold);
                guiController.updateSurvivalStatus(survivalHandler.getShields(), landingsUntilGarbage);
            } else {
                guiController.updateSurvivalStatus(survivalHandler.getShields(), -1);
            }
            return;
        }

        guiController.clearProgressText();
    }

    // ========================= INPUT HANDLERS =========================

    @Override
    public DownData onDownEvent(MoveEvent event) {
        boolean moved = board.moveBrickDown();
        ClearRow clearRow = null;

        if (!moved) {
            clearRow = handleBrickLanded();
        } else if (event.getEventSource() == EventSource.USER) {
            // Soft drop bonus per cell (only when user presses DOWN).
            board.getScore().add(1);
        }

        return new DownData(clearRow, board.getViewData());
    }

    /**
     * Handles hard drop (space bar) input.
     * Moves the current brick straight down until it lands, then processes
     * line clearing. Does not award points for the drop itself, only for clearing lines.
     *
     * @param event the move event
     * @return DownData containing any line clear results and updated view data
     */
    @Override
    public DownData onHardDropEvent(MoveEvent event) {
        // 1. Move down as far as possible in the current tick.
        int cellsDropped = 0;
        while (board.moveBrickDown()) {
            cellsDropped++;
        }

        // 2. Once we can no longer move, treat it as a landing.
        ClearRow clearRow = handleBrickLanded();

        // Award points for hard drop based on distance (2 points per cell)
        if (cellsDropped > 0) {
            board.getScore().addHardDropScore(cellsDropped);
        }

        return new DownData(clearRow, board.getViewData());
    }

    /**
     * Handles left movement input.
     *
     * @param event the move event
     * @return updated view data after moving left
     */
    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        board.moveBrickLeft();
        return board.getViewData();
    }

    /**
     * Handles right movement input.
     *
     * @param event the move event
     * @return updated view data after moving right
     */
    @Override
    public ViewData onRightEvent(MoveEvent event) {
        board.moveBrickRight();
        return board.getViewData();
    }

    /**
     * Handles rotation input.
     *
     * @param event the move event
     * @return updated view data after rotating
     */
    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        board.rotateLeftBrick();
        return board.getViewData();
    }

    /**
     * Handles hold/swap input.
     * Holds the current brick or swaps with the previously held brick.
     *
     * @param event the move event
     * @return updated view data after holding/swapping
     */
    @Override
    public ViewData onHoldEvent(MoveEvent event) {
        board.holdCurrentBrick();
        return board.getViewData();
    }

    // ========================= SURVIVAL / RUSH LOGIC =========================

    /**
     * Handles the logic when a falling brick can no longer move down.
     * Merges the brick into the board, clears lines, updates score,
     * and spawns a new brick. Handles mode-specific effects and game over conditions.
     *
     * @return ClearRow object if lines were cleared, null otherwise
     */
    private ClearRow handleBrickLanded() {
        // Lock brick into background.
        board.mergeBrickToBackground();

        ClearRow clearRow = board.clearRows();
        Score score = board.getScore();

        if (clearRow != null && clearRow.getLinesRemoved() > 0) {
            int lines = clearRow.getLinesRemoved();
            totalLinesCleared += lines;

            score.registerLinesCleared(
                    lines,
                    clearRow.getScoreBonus()
            );
        } else {
            score.registerLandingWithoutClear();
        }

        // Mode-specific effects.
        if (survivalHandler != null) {
            survivalHandler.handleBrickLanded(clearRow, score);
            int baseThreshold = config.getMaxNoClearBeforeGarbage();
            if (baseThreshold > 0) {
                int landingsUntilGarbage = survivalHandler.getLandingsUntilGarbage(score, baseThreshold);
                guiController.updateSurvivalStatus(survivalHandler.getShields(), landingsUntilGarbage);
            }
        }

        // Rush-40 goal logic.
        if (rushHandler != null && !rushHandler.isCompleted()) {
            boolean milestoneReached = rushHandler.handleLinesCleared(clearRow);
            
            if (milestoneReached) {
                String message = rushHandler.getMilestoneMessage();
                if (message != null) {
                    guiController.showRushMilestone(message);
                }
            }
            
            guiController.updateRushProgress(rushHandler.getLinesCleared(), rushHandler.getTargetLines());

            if (rushHandler.isCompleted()) {
                // Show congratulations message for completing Rush 40
                guiController.showRush40Congratulations();
                
                double completionSeconds = rushHandler.getCompletionTimeSeconds();
                int finalScore = score.scoreProperty().get();

                guiController.showFinalResults(
                        gameMode,
                        finalScore,
                        totalLinesCleared,
                        rushHandler.getTargetLines(),
                        completionSeconds,
                        true
                );

                guiController.refreshGameBackground(board.getBoardMatrix());
                return clearRow;
            }
        }

        // Normal spawn / game over.
        if (board.createNewBrick()) {
            double completionSeconds = (rushHandler != null) ? rushHandler.getCompletionTimeSeconds() : -1.0;
            int finalScore = score.scoreProperty().get();
            int targetLines = (rushHandler != null) ? rushHandler.getTargetLines() : 0;

            // Top-out is a loss even in Rush-40 if we did not hit targetLines.
            boolean isWin = (rushHandler != null) && rushHandler.isCompleted();

            guiController.showFinalResults(
                    gameMode,
                    finalScore,
                    totalLinesCleared,
                    targetLines,
                    completionSeconds,
                    isWin
            );
        }

        guiController.refreshGameBackground(board.getBoardMatrix());
        return clearRow;
    }

    // ========================= PUBLIC HELPERS =========================

    /**
     * Resets the current game without changing mode or config.
     * Clears the board, resets score, and starts a fresh game in the same mode.
     * Called from the main menu or GUI restart button.
     */
    public void createNewGame() {
        if (survivalHandler != null) {
            survivalHandler.reset();
        }
        
        if (rushHandler != null) {
            rushHandler.reset();
            rushHandler.start();
        }

        totalLinesCleared = 0;

        board.newGame();

        guiController.refreshGameBackground(board.getBoardMatrix());
        initialiseProgressHud(board.getScore());
    }

    /**
     * Returns the Rush-40 completion time in seconds.
     *
     * @return completion time in seconds, or -1.0 if the game has not finished
     *         or is not a Rush-40 game
     */
    public double getRushCompletionTimeSeconds() {
        if (rushHandler == null) {
            return -1.0;
        }
        return rushHandler.getCompletionTimeSeconds();
    }
}

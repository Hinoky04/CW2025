package com.comp2042;

/**
 * Connects the board (game logic) with the JavaFX GUI.
 * Receives input events from GuiController and updates the board.
 */
public class GameController implements InputEventListener {

    // Board size (easier to change here than using magic numbers everywhere).
    private static final int BOARD_ROWS = 25;
    private static final int BOARD_COLUMNS = 10;

    // Upper bound on how many survival shields can be stored.
    private static final int SURVIVAL_MAX_SHIELDS = 3;

    // Core game model and GUI controller.
    private final Board board;
    private final GuiController guiController;

    // Selected game mode for this run (Classic, Survival, Hyper, Rush-40).
    private final GameMode gameMode;

    // Immutable configuration derived from the chosen mode.
    private final GameConfig config;

    // === Survival-mode state (only used when gameMode == SURVIVAL) ===

    // Counts consecutive landings with no cleared lines.
    private int survivalNoClearLandingCount = 0;

    // Shields that block scheduled garbage rows. Capped at SURVIVAL_MAX_SHIELDS.
    private int survivalShields = 0;

    // === Rush-40 state (only used when gameMode == RUSH_40) ===

    // True when this controller is running a Rush-40 game with a valid target.
    private final boolean rushModeActive;

    // Target number of lines to clear in Rush-40 (e.g. 40).
    private final int rushTargetLines;

    // Lines cleared so far in this Rush-40 run.
    private int rushLinesCleared = 0;

    // Marks whether the Rush-40 goal has been completed.
    private boolean rushCompleted = false;

    // Timing for Rush-40: start and finish timestamps in nanoseconds.
    private long rushStartNanos = 0L;
    private long rushEndNanos = 0L;

    /**
     * Create a new game controller and use the default board size
     * for the selected mode. Behaviour diverges via GameConfig values.
     */
    public GameController(GuiController guiController, GameMode gameMode) {
        this.guiController = guiController;
        this.gameMode = gameMode;
        this.config = gameMode.getConfig();
        this.board = new SimpleBoard(BOARD_ROWS, BOARD_COLUMNS);

        boolean isRush = (gameMode == GameMode.RUSH_40);
        int target = isRush ? config.getTargetLinesToWin() : 0;

        if (isRush && target > 0) {
            this.rushModeActive = true;
            this.rushTargetLines = target;
        } else {
            this.rushModeActive = false;
            this.rushTargetLines = 0;
        }

        initialiseGame();
    }

    /**
     * One-time setup: create first brick, hook GUI listeners, bind HUD fields.
     */
    private void initialiseGame() {
        board.createNewBrick();
        guiController.setEventListener(this);

        // Tell GUI which mode we are running so restart uses the same mode.
        guiController.setGameMode(gameMode);

        // Apply mode-specific config (speed curve, danger rows, etc.).
        guiController.applyConfig(config);

        guiController.initGameView(board.getBoardMatrix(), board.getViewData());

        // Bind score/level/combo from the model to the HUD.
        Score score = board.getScore();
        guiController.bindScore(score.scoreProperty());
        guiController.bindLevel(score.levelProperty());
        guiController.bindCombo(score.comboProperty());

        if (rushModeActive) {
            // Start timing for Rush-40 as soon as the game begins.
            rushStartNanos = System.nanoTime();
            rushEndNanos = 0L;
            rushLinesCleared = 0;
            rushCompleted = false;
        }
    }

    @Override
    public DownData onDownEvent(MoveEvent event) {
        boolean moved = board.moveBrickDown();
        ClearRow clearRow = null;

        if (!moved) {
            // Brick has landed: merge, clear rows, maybe Survival / Rush-40 effects.
            clearRow = handleBrickLanded();
        } else if (event.getEventSource() == EventSource.USER) {
            // User soft drop gives a small score bonus.
            board.getScore().add(1);
        }

        // GUI only needs cleared-row info + new brick view data.
        return new DownData(clearRow, board.getViewData());
    }

    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        board.moveBrickLeft();
        return board.getViewData();
    }

    @Override
    public ViewData onRightEvent(MoveEvent event) {
        board.moveBrickRight();
        return board.getViewData();
    }

    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        board.rotateLeftBrick();
        return board.getViewData();
    }

    /**
     * Triggered when the player requests a hold action.
     * Delegates to the board and applies normal game-over handling
     * if the new active brick immediately collides.
     */
    @Override
    public ViewData onHoldEvent(MoveEvent event) {
        boolean collision = board.holdCurrentBrick();
        if (collision) {
            guiController.gameOver();
            guiController.refreshGameBackground(board.getBoardMatrix());
        }
        return board.getViewData();
    }

    @Override
    public void createNewGame() {
        // Reset survival-specific state.
        survivalNoClearLandingCount = 0;
        survivalShields = 0;

        // Reset Rush-40 state. Only meaningful if rushModeActive is true.
        rushLinesCleared = 0;
        rushCompleted = false;
        rushEndNanos = 0L;
        rushStartNanos = rushModeActive ? System.nanoTime() : 0L;

        // Reset the board state for completeness.
        // Restart from menu now reloads the whole scene via GuiController,
        // but this is kept for the 'N' shortcut inside the game.
        board.newGame();

        // Update background in case someone calls this in the future.
        guiController.refreshGameBackground(board.getBoardMatrix());
    }

    /**
     * Survival-mode behaviour: dynamic garbage pressure and shields.
     * - Garbage rows appear after several non-clearing landings.
     * - Clearing four lines at once grants a shield that blocks
     *   the next garbage event.
     * Other modes ignore this logic.
     */
    private void handleSurvivalEffects(ClearRow clearRow, Score score) {
        if (gameMode != GameMode.SURVIVAL) {
            return;
        }

        int baseThreshold = config.getMaxNoClearBeforeGarbage();
        if (baseThreshold <= 0) {
            return; // feature disabled for this config
        }

        int linesRemoved = (clearRow != null) ? clearRow.getLinesRemoved() : 0;

        // Reward big plays: a four-line clear grants a shield,
        // capped so shields do not accumulate without limit.
        if (linesRemoved >= 4) {
            survivalShields++;
            if (survivalShields > SURVIVAL_MAX_SHIELDS) {
                survivalShields = SURVIVAL_MAX_SHIELDS;
            }
        }

        int threshold = computeSurvivalGarbageThreshold(score, baseThreshold);

        if (linesRemoved > 0) {
            // Any clear breaks the no-clear streak and relieves pressure.
            survivalNoClearLandingCount = 0;
        } else {
            // Landing without clearing increases pressure.
            survivalNoClearLandingCount++;
        }

        if (survivalNoClearLandingCount >= threshold) {
            if (survivalShields > 0) {
                // Shield absorbs this scheduled garbage row.
                survivalShields--;
            } else {
                // Inject a garbage row at the bottom of the board.
                board.addGarbageRow();
            }
            // After triggering (or blocking) a garbage event, restart the counter.
            survivalNoClearLandingCount = 0;
        }
    }

    /**
     * Computes the current survival garbage threshold based on the base config
     * and the player's level. As level increases, the threshold decreases so
     * garbage appears more frequently, down to a minimum of one landing.
     */
    private int computeSurvivalGarbageThreshold(Score score, int baseThreshold) {
        int level = score.getLevel();
        // Every three levels, reduce the threshold by one.
        int reduction = (level - 1) / 3;
        int threshold = baseThreshold - reduction;

        if (threshold < 1) {
            threshold = 1;
        }
        if (threshold > baseThreshold) {
            threshold = baseThreshold;
        }
        return threshold;
    }

    /**
     * Runs when the falling brick can no longer move down.
     * - merges brick into the background
     * - clears full rows and updates score/combo/level
     * - applies Survival-specific logic (garbage and shields)
     * - applies Rush-40 goal logic if enabled
     * - spawns the next brick or ends the game if there is no space
     */
    private ClearRow handleBrickLanded() {
        board.mergeBrickToBackground();

        ClearRow clearRow = board.clearRows();
        Score score = board.getScore();

        if (clearRow != null && clearRow.getLinesRemoved() > 0) {
            // Landing with a clear: update combo + score + level.
            score.registerLinesCleared(
                    clearRow.getLinesRemoved(),
                    clearRow.getScoreBonus()
            );
        } else {
            // Landing with no clear: break the combo chain.
            score.registerLandingWithoutClear();
        }

        // Apply Survival-specific effects (garbage pressure and shields).
        handleSurvivalEffects(clearRow, score);

        // Apply Rush-40 goal, if enabled.
        if (rushModeActive && !rushCompleted && clearRow != null && clearRow.getLinesRemoved() > 0) {
            rushLinesCleared += clearRow.getLinesRemoved();
            if (rushLinesCleared >= rushTargetLines) {
                // Goal reached: stop the run and record completion time.
                rushCompleted = true;
                rushEndNanos = System.nanoTime();

                // For now we reuse the standard game-over overlay.
                // Phase 7 can provide a dedicated "You Win" message.
                guiController.gameOver();
                guiController.refreshGameBackground(board.getBoardMatrix());
                return clearRow;
            }
        }

        // Standard behaviour: spawn a new brick and check for normal game over.
        if (board.createNewBrick()) {
            guiController.gameOver();
        }

        guiController.refreshGameBackground(board.getBoardMatrix());
        return clearRow;
    }

    /**
     * Returns the Rush-40 completion time in seconds, or a negative value
     * if the current run has not finished or is not a Rush-40 game.
     * This is intended for future HUD / result screen use.
     */
    public double getRushCompletionTimeSeconds() {
        if (!rushModeActive || !rushCompleted || rushStartNanos == 0L || rushEndNanos == 0L) {
            return -1.0;
        }
        long durationNanos = rushEndNanos - rushStartNanos;
        return durationNanos / 1_000_000_000.0;
    }
}

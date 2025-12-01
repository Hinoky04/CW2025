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

    // Total lines cleared in this run (all modes).
    private int totalLinesCleared = 0;

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

        guiController.setGameMode(gameMode);
        guiController.applyConfig(config);

        guiController.initGameView(board.getBoardMatrix(), board.getViewData());

        Score score = board.getScore();
        guiController.bindScore(score.scoreProperty());
        guiController.bindLevel(score.levelProperty());
        guiController.bindCombo(score.comboProperty());

        totalLinesCleared = 0;

        if (rushModeActive) {
            rushStartNanos = System.nanoTime();
            rushEndNanos = 0L;
            rushLinesCleared = 0;
            rushCompleted = false;
        }

        initialiseProgressHud(score);
    }

    /**
     * Initialises the generic progress HUD line depending on the current mode.
     */
    private void initialiseProgressHud(Score score) {
        if (rushModeActive && rushTargetLines > 0) {
            guiController.updateRushProgress(rushLinesCleared, rushTargetLines);
            return;
        }

        if (gameMode == GameMode.SURVIVAL) {
            int baseThreshold = config.getMaxNoClearBeforeGarbage();
            if (baseThreshold > 0) {
                updateSurvivalHud(score, baseThreshold);
            } else {
                guiController.updateSurvivalStatus(survivalShields, -1);
            }
            return;
        }

        guiController.clearProgressText();
    }

    @Override
    public DownData onDownEvent(MoveEvent event) {
        boolean moved = board.moveBrickDown();
        ClearRow clearRow = null;

        if (!moved) {
            clearRow = handleBrickLanded();
        } else if (event.getEventSource() == EventSource.USER) {
            board.getScore().add(1);
        }

        return new DownData(clearRow, board.getViewData());
    }

    /**
     * Survival-mode behaviour: dynamic garbage pressure and shields.
     */
    private void handleSurvivalEffects(ClearRow clearRow, Score score) {
        if (gameMode != GameMode.SURVIVAL) {
            return;
        }

        int baseThreshold = config.getMaxNoClearBeforeGarbage();
        if (baseThreshold <= 0) {
            return;
        }

        int linesRemoved = (clearRow != null) ? clearRow.getLinesRemoved() : 0;

        if (linesRemoved >= 4) {
            survivalShields++;
            if (survivalShields > SURVIVAL_MAX_SHIELDS) {
                survivalShields = SURVIVAL_MAX_SHIELDS;
            }
        }

        int threshold = computeSurvivalGarbageThreshold(score, baseThreshold);

        if (linesRemoved > 0) {
            survivalNoClearLandingCount = 0;
        } else {
            survivalNoClearLandingCount++;
        }

        if (survivalNoClearLandingCount >= threshold) {
            if (survivalShields > 0) {
                survivalShields--;
            } else {
                board.addGarbageRow();
            }
            survivalNoClearLandingCount = 0;
        }

        updateSurvivalHud(score, baseThreshold);
    }

    /**
     * Computes the current survival garbage threshold based on the base config
     * and the player's level.
     */
    private int computeSurvivalGarbageThreshold(Score score, int baseThreshold) {
        int level = score.getLevel();
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
     * Updates the Survival progress HUD, e.g. "Shields 2, Garbage in 1".
     */
    private void updateSurvivalHud(Score score, int baseThreshold) {
        int threshold = computeSurvivalGarbageThreshold(score, baseThreshold);
        int landingsUntilGarbage = threshold - survivalNoClearLandingCount;
        if (landingsUntilGarbage < 0) {
            landingsUntilGarbage = 0;
        }
        guiController.updateSurvivalStatus(survivalShields, landingsUntilGarbage);
    }

    /**
     * Runs when the falling brick can no longer move down.
     */
    private ClearRow handleBrickLanded() {
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

        handleSurvivalEffects(clearRow, score);

        // Rush-40 goal logic.
        if (rushModeActive && !rushCompleted && clearRow != null && clearRow.getLinesRemoved() > 0) {
            rushLinesCleared += clearRow.getLinesRemoved();

            guiController.updateRushProgress(rushLinesCleared, rushTargetLines);

            if (rushLinesCleared >= rushTargetLines) {
                rushCompleted = true;
                rushEndNanos = System.nanoTime();

                double completionSeconds = getRushCompletionTimeSeconds();
                int finalScore = score.scoreProperty().get();

                guiController.showFinalResults(
                        gameMode,
                        finalScore,
                        totalLinesCleared,
                        rushTargetLines,
                        completionSeconds,
                        true
                );

                guiController.refreshGameBackground(board.getBoardMatrix());
                return clearRow;
            }
        }

        // Normal spawn / game over.
        if (board.createNewBrick()) {
            double completionSeconds = getRushCompletionTimeSeconds();
            int finalScore = score.scoreProperty().get();
            int targetLines = rushModeActive ? rushTargetLines : 0;

            // Top-out is a loss even in Rush-40 if we did not hit targetLines.
            boolean isWin = rushModeActive && rushCompleted;

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
     * HOLD input handler.
     */
    @Override
    public ViewData onHoldEvent(MoveEvent event) {
        board.holdCurrentBrick();
        return board.getViewData();
    }

    @Override
    public void createNewGame() {
        survivalNoClearLandingCount = 0;
        survivalShields = 0;

        rushLinesCleared = 0;
        rushCompleted = false;
        rushEndNanos = 0L;
        rushStartNanos = rushModeActive ? System.nanoTime() : 0L;

        totalLinesCleared = 0;

        board.newGame();

        guiController.refreshGameBackground(board.getBoardMatrix());
        initialiseProgressHud(board.getScore());
    }

    /**
     * Returns the Rush-40 completion time in seconds, or a negative value
     * if the current run has not finished or is not a Rush-40 game.
     */
    public double getRushCompletionTimeSeconds() {
        if (!rushModeActive || !rushCompleted || rushStartNanos == 0L || rushEndNanos == 0L) {
            return -1.0;
        }
        long durationNanos = rushEndNanos - rushStartNanos;
        return durationNanos / 1_000_000_000.0;
    }
}

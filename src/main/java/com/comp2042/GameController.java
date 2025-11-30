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

    // Selected game mode for this run (Classic, Survival, etc.).
    private final GameMode gameMode;

    // Immutable configuration derived from the chosen mode.
    private final GameConfig config;

    // Survival-mode state: only used in GameMode.SURVIVAL.
    // Counts consecutive landings with no cleared lines and how many
    // garbage shields the player has earned.
    private int survivalNoClearLandingCount = 0;
    private int survivalShields = 0;

    /**
     * Create a new game controller and use the default board size
     * for the selected mode. Behaviour diverges via GameConfig values.
     */
    public GameController(GuiController guiController, GameMode gameMode) {
        this.guiController = guiController;
        this.gameMode = gameMode;
        this.config = gameMode.getConfig();
        this.board = new SimpleBoard(BOARD_ROWS, BOARD_COLUMNS);
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
    }

    @Override
    public DownData onDownEvent(MoveEvent event) {
        boolean moved = board.moveBrickDown();
        ClearRow clearRow = null;

        if (!moved) {
            // Brick has landed: merge, clear rows, maybe game over.
            clearRow = handleBrickLanded();
        } else if (event.getEventSource() == EventSource.USER) {
            // User soft drop gives a small score bonus.
            board.getScore().add(1);
        }

        // GUI only needs cleared-row info + new brick view data.
        return new DownData(clearRow, board.getViewData());
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

        // true means new brick could not be placed → game over.
        if (board.createNewBrick()) {
            guiController.gameOver();
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

    @Override
    public void createNewGame() {
        // Reset survival-specific state.
        survivalNoClearLandingCount = 0;
        survivalShields = 0;

        // Reset the board state for completeness.
        // Restart from menu now reloads the whole scene via GuiController,
        // but this is kept for the 'N' shortcut inside the game.
        board.newGame();

        // Update background in case someone calls this in the future.
        guiController.refreshGameBackground(board.getBoardMatrix());
    }
}

package com.comp2042;

/**
 * Connects the board (game logic) with the JavaFX GUI.
 * Receives input events from GuiController and updates the board.
 */
public class GameController implements InputEventListener {

    // Board size (easier to change here than using 25/10 everywhere)
    private static final int BOARD_ROWS = 25;
    private static final int BOARD_COLUMNS = 10;

    // Core game model and GUI controller
    private final Board board;
    private final GuiController guiController;

    /**
     * Create a new game controller and use the default board size.
     */
    public GameController(GuiController guiController) {
        this.guiController = guiController;
        this.board = new SimpleBoard(BOARD_ROWS, BOARD_COLUMNS);
        initialiseGame();
    }

    /**
     * One-time setup: create first brick, hook GUI listeners, bind score.
     */
    private void initialiseGame() {
        board.createNewBrick();
        guiController.setEventListener(this);
        guiController.initGameView(board.getBoardMatrix(), board.getViewData());
        guiController.bindScore(board.getScore().scoreProperty());
    }

    @Override
    public DownData onDownEvent(MoveEvent event) {
        boolean moved = board.moveBrickDown();
        ClearRow clearRow = null;

        if (!moved) {
            // Brick has landed: merge, clear rows, maybe game over
            clearRow = handleBrickLanded();
        } else if (event.getEventSource() == EventSource.USER) {
            // User soft drop gives a small score bonus
            board.getScore().add(1);
        }

        // GUI only needs cleared-row info + new brick view data
        return new DownData(clearRow, board.getViewData());
    }

    /**
     * Runs when the falling brick can no longer move down.
     * - merges brick into the background
     * - clears full rows and updates score
     * - spawns the next brick or ends the game if there is no space
     */
    private ClearRow handleBrickLanded() {
        board.mergeBrickToBackground();

        ClearRow clearRow = board.clearRows();
        if (clearRow != null && clearRow.getLinesRemoved() > 0) {
            board.getScore().add(clearRow.getScoreBonus());
        }

        // true means new brick could not be placed → game over
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
        // Reset the model and redraw the background; GUI stays the same
        board.newGame();
        guiController.refreshGameBackground(board.getBoardMatrix());
    }
}

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
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
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

    // Background cells (for the board)
    private Rectangle[][] displayMatrix;

    // Current falling piece cells
    private Rectangle[][] rectangles;

    // Listener that sends user input events to the game logic
    private InputEventListener eventListener;

    // Timer for automatic down movement
    private Timeline timeLine;

    // --- OLD flags (replaced by GameState) ---------------------------------
    // private final BooleanProperty isPause = new SimpleBooleanProperty();
    // private final BooleanProperty isGameOver = new SimpleBooleanProperty();

    // NEW: single source of truth for game state.
    private GameState gameState = GameState.PLAYING;

    // Still kept for potential bindings, but now driven by gameState.
    private final BooleanProperty isPause = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver = new SimpleBooleanProperty(false);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load custom digital font if available (used for score etc.)
        URL fontUrl = getClass().getClassLoader().getResource("digital.ttf");
        if (fontUrl != null) {
            Font.loadFont(fontUrl.toExternalForm(), 38);
        }

        // Allow the game panel to receive keyboard focus and request it initially
        gamePanel.setFocusTraversable(true);
        gamePanel.requestFocus();

        // Handle keyboard input for movement and new game
        gamePanel.setOnKeyPressed(this::handleKeyPressed);

        // Game over panel is hidden at the start
        gameOverPanel.setVisible(false);
    }

    /**
     * Centralised key handler so initialize() stays cleaner.
     */
    private void handleKeyPressed(KeyEvent event) {
        // Only handle movement input if not paused and not game over
        if (canHandleInput()) {
            KeyCode code = event.getCode();

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

        // Press 'N' at any time to start a new game
        if (event.getCode() == KeyCode.N) {
            newGame(null);
            event.consume();
        }
    }

    /**
     * Small helper to describe when we are allowed to handle movement.
     * 只有在 PLAYING 状态下才允许响应输入。
     */
    private boolean canHandleInput() {
        return gameState == GameState.PLAYING;
    }

    // --- Old initGameView kept as comment for marker reference ----------------
    // public void initGameView(int[][] boardMatrix, ViewData brick) { ... }

    /**
     * Called by the game logic to set up the initial board and piece view.
     * NEW version: split into smaller helpers for readability and maintenance.
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
                // We skip the hidden top rows when adding to the visible grid
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
        // 只有在 PLAYING 状态才更新砖块视图
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
     * TODO: Bind score property to a label in the UI (to be implemented later).
     */
    public void bindScore(IntegerProperty integerProperty) {
        // Implementation will be added when score label is wired up
    }

    public void gameOver() {
        timeLine.stop();
        gameOverPanel.setVisible(true);

        // 更新状态：游戏结束
        gameState = GameState.GAME_OVER;
        isGameOver.set(true);
        isPause.set(false);
    }

    public void newGame(javafx.event.ActionEvent actionEvent) {
        timeLine.stop();
        gameOverPanel.setVisible(false);
        eventListener.createNewGame();
        gamePanel.requestFocus();
        timeLine.play();

        // 回到 PLAYING 状态
        gameState = GameState.PLAYING;
        isPause.set(false);
        isGameOver.set(false);
    }

    /**
     * TODO: Implement pause/resume logic in a later feature.
     * 这里可以在 PLAYING / PAUSED 之间切换，先留 TODO。
     */
    public void pauseGame(javafx.event.ActionEvent actionEvent) {
        // Simple toggle for future: PLAYING <-> PAUSED
        // if (gameState == GameState.PLAYING) {
        //     gameState = GameState.PAUSED;
        //     isPause.set(true);
        // } else if (gameState == GameState.PLAYING) {
        //     gameState = GameState.PLAYING;
        //     isPause.set(false);
        // }

        gamePanel.requestFocus();
    }
}

# Bug & Code Smell Log

Short internal log to support README and demo video.  
Status tags: **[FIXED]**, **[TODO]**, **[PARTIALLY FIXED]**, **[WONTFIX]**.

---

## 1. Behaviour / Bug observations

### BUG-01 – Pieces spawn too low (middle instead of near top) **[FIXED – Phase 3.5]**

- Symptom: New bricks appeared around the middle of the visible board instead of near the top, reducing reaction time.
- Root cause: `SimpleBoard.createNewBrick()` used `(SPAWN_X, SPAWN_Y)` with `SPAWN_Y = 10` on a 25-row board.
- Fix: Changed `SPAWN_Y` from `10` to `1` so new bricks start near the top/hidden rows.
- Regression test: `SimpleBoardTest.createNewBrick_spawnsNearTop()`:
  - `createNewBrick()` does not collide on an empty board.
  - `spawnY <= 2`.

---

### BUG-02 – Piece cannot rotate at the left/right wall **[FIXED – Phase 3.5 bugfix]**

- Symptom: When a piece was tight against the left or right border, rotation was blocked even when it should fit.
- Root cause: `SimpleBoard.rotateLeftBrick()` only tried rotation in place; any out-of-bounds cell was treated as a collision.
- Fix: Added a simple “wall-kick” in `rotateLeftBrick()`:
  - Try rotation in place, then at `x-1` and `x+1`.
  - Allows rotation next to a wall while still within the board.

---

### BUG-03 – Extra falling brick visible after game over **[FIXED – Phase 5.2]**

- Symptom: After game over, an extra copy of the last brick appeared on top of the final board state.
- Root cause: `GameController.handleBrickLanded()` merged the brick into the background and then called `board.createNewBrick()`.  
  When `createNewBrick()` reported game over, the GUI still kept drawing the “current” falling brick in `brickPanel`.
- Fix: In `GuiController.gameOver()`:
  - Stop the `Timeline` safely.
  - Clear `brickPanel.getChildren()` so the falling brick visuals are removed once the brick is merged into the background.
- Result: Final board state is shown correctly with no duplicate brick after game over.

---

### BUG-04 – Pause menu buttons do not respond **[FIXED – Phase 5.2]**

- Symptom: Pressing **P** showed the pause overlay, but clicking **Resume**, **Restart**, or **Main Menu** did nothing.
- Root cause:
  - In `gameLayout.fxml` the `pauseOverlay` pane was declared **under** `groupNotification`, so the notification group was always drawn on top.
  - `pauseOverlay` also had `mouseTransparent="true"`, meaning it and its children did not receive mouse events.
- Fix:
  - Moved `pauseOverlay` to the end of the root `<Pane>` so it is rendered on top of the board/notifications.
  - In `GuiController.initialize(...)` explicitly called `pauseOverlay.setMouseTransparent(false)` and wired the three buttons with `setOnAction(...)`.
- Result: Pause menu is now fully interactive:
  - **Resume** toggles back to `PLAYING`.
  - **Restart** restarts the current game mode via `restartSameMode()`.
  - **Main Menu** returns to the main menu via `backToMainMenu()`.

---

### BUG-05 – Falling piece not aligned with grid while moving **[FIXED – Phase 8.1]**

- Symptom: The falling tetromino looked slightly shifted compared to the background grid while moving, and only became perfectly aligned when it reached the bottom or locked in place.
- Root cause: `GuiController.updateBrickPanelPosition(...)` computed the brick position using `gamePanel` layout coordinates plus a hard-coded cell size (`BRICK_SIZE + gap`). After the new UI and CSS changes, the actual cell spacing of the background grid no longer matched this calculation.
- Fix:
  - Rewrote `updateBrickPanelPosition(...)` to use the real background cells in `displayMatrix` as the source of truth:
    - Take the scene coordinates of the first visible cell (`displayMatrix[HIDDEN_TOP_ROWS][0]`) and the next cell in the same row/next row to measure `cellWidth` and `cellHeight`.
    - Convert those scene coordinates back into the parent coordinate system of `brickPanel`.
    - Compute `x / y` from the measured cell size and the board position `(xPosition, yPosition - HIDDEN_TOP_ROWS)`.
  - Kept a safe fallback path using `BRICK_SIZE + hgap/vgap` if `displayMatrix` is not yet initialised.
- Result: The falling piece now stays perfectly aligned with the grid at all times, even if the UI layout or CSS is adjusted.

---

### TEST-01 – Wrong assumption about line clearing **[FIXED]**

- Area: `SimpleBoard.clearRows()` and `SimpleBoardTest.clearRows_removesSingleFullRow`.
- Initial test assumption: After clearing a full row, the bottom row should become all zeros.
- Reality: Implementation lets rows above fall down, so blocks from the row above can land in the bottom row.
- Fix: Updated the test to expect falling behaviour; left the old assertion commented as documentation.

---

## 2. Code smells / refactoring notes

### SMELL-01 – Incomplete pause feature in `GuiController` **[FIXED – replaced by GameState pause]**

- Original issue: `pauseGame(ActionEvent)` only called `gamePanel.requestFocus()` and did not change `isPause` / `Timeline` state.
- Impact: Pause button existed in UI but did not pause the game.
- Current state: Replaced by proper pause/resume using `GameState` and a pause overlay.  
  Phase 5 added a pause menu (Resume / Restart / Main Menu) which reuses the same `GameState` logic.

---

### SMELL-02 – Unused score binding in `GuiController.bindScore(IntegerProperty)` **[FIXED – HUD binding]**

- Original issue: `bindScore(...)` was empty, suggesting an unfinished feature.
- Impact: Unclear how score should be displayed/updated from the model.
- Fix: `GuiController.bindScore(IntegerProperty)` now binds the model’s `scoreProperty` to the HUD `scoreText`:
  - `scoreText.textProperty().bind(scoreProperty.asString("Score %d"));`
- Result: Score is always in sync with the model and updated automatically in the HUD.

---

### SMELL-03 – `SimpleBoard` ignores constructor parameters **[PARTIALLY FIXED – Phase 5.x]**

- Original evidence: Constructor took `(int width, int height)` but always created `new int[ROWS][COLUMNS]`, effectively hard-coding the board size.
- Impact: Parameters were misleading and hurt reuse.
- Current state:
  - `newGame()` now uses stored `rows / columns` fields (`boardMatrix = new int[rows][columns];`) so the reset path respects the constructor size.
  - Initial allocation still relies on the static `ROWS` / `COLUMNS` constants.
- Plan (later phase):
  - Fully unify allocation to always use the instance fields, or
  - Remove the constructor parameters if dynamic sizing is not required for this coursework.

---

### SMELL-04 – Tight coupling between `GameController` and `SimpleBoard` **[TODO – future refactor]**

- Evidence: `GameController` directly constructs `new SimpleBoard(BOARD_ROWS, BOARD_COLUMNS)` instead of working purely with the `Board` interface.
- Impact: Harder to:
  - Swap in another `Board` implementation (e.g. for different game modes), or
  - Use a fake `Board` in unit tests.
- Plan (later phase):
  - Introduce a `Board` factory or inject a `Board` via the constructor (`GameController(Board board, GuiController gui)`).
  - Keep `GameController` depending only on the `Board` interface so it is easier to test and extend.

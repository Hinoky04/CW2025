# Bug & Code Smell Log

Short internal log to support README and demo video.  
Status tags: **[FIXED]**, **[TODO]**, **[WONTFIX]**.

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
- Root cause: `GameController.handleBrickLanded()` merged the brick and then called `board.createNewBrick()`.  
  When `createNewBrick()` reported game over, the GUI still kept drawing the “current” falling brick in `brickPanel`.
- Fix: In `GuiController.gameOver()`:
  - Stop the Timeline safely.
  - Clear `brickPanel.getChildren()` so the falling brick visuals are removed once the brick is merged into the background.
- Result: Final board state is shown correctly with no duplicate brick after game over.

---

### TEST-01 – Wrong assumption about line clearing **[FIXED]**

- Area: `SimpleBoard.clearRows()` and `SimpleBoardTest.clearRows_removesSingleFullRow`.
- Initial test assumption: After clearing a full row, the bottom row should become all zeros.
- Reality: Implementation lets rows above fall down, so blocks from the row above can land in the bottom row.
- Fix: Updated the test to expect falling behaviour; left the old assertion commented as documentation.

---

## 2. Code smells / refactoring notes

### SMELL-01 – Incomplete pause feature in `GuiController` **[FIXED – replaced by GameState pause]**

- Original issue: `pauseGame(ActionEvent)` only called `gamePanel.requestFocus()` and did not change `isPause` / Timeline state.
- Impact: Pause button existed in UI but did not pause the game.
- Current state: Replaced by proper pause/resume using `GameState` and a pause overlay (handled in Phase 4 and extended in Phase 5 with a pause menu).

---

### SMELL-02 – Unused score binding in `GuiController.bindScore(IntegerProperty)` **[FIXED – HUD binding]**

- Original issue: `bindScore(...)` was empty, suggesting an unfinished feature.
- Impact: Unclear how score should be displayed/updated from the model.
- Fix: `GuiController.bindScore(IntegerProperty)` now binds the model’s `scoreProperty` to the HUD `scoreText`:
  - `scoreText.textProperty().bind(scoreProperty.asString("Score %d"));`
- Result: Score is always in sync with the model and updated automatically in the HUD.

---

### SMELL-03 – `SimpleBoard` ignores constructor parameters **[TODO – future refactor]**

- Evidence: Constructor takes `(int width, int height)` but always creates `new int[ROWS][COLUMNS]`.
- Impact: Board size is effectively hard-coded; parameters are misleading and hurt reuse.
- Plan (later phase):
  - Store `width` / `height` in fields.
  - Allocate the backing array using those values or remove the parameters if dynamic sizing is not needed.

---

### SMELL-04 – Tight coupling between `GameController` and `SimpleBoard` **[TOD]()**

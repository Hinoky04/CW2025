# Initial Bug & Code Smell List (Phase 1)

## Behaviour / Bug observations

- BUG-01: Pieces spawn too low (middle instead of near top)
  - Description: New bricks appear around the middle of the visible board instead of near the top, reducing reaction time.
  - Steps: Start a new game and observe the initial spawn position.
  - Suspected cause: SimpleBoard.createNewBrick() uses a fixed offset (SPAWN_X, SPAWN_Y) with a large SPAWN_Y plus hidden top rows in the GUI.

- BUG-02: Piece cannot rotate at the left/right wall
  - Description: When a piece is tight against the left or right border, rotation is often blocked even if it would still fit.
  - Steps: Move a piece fully to the wall and press rotate.
  - Suspected cause: SimpleBoard.rotateLeftBrick() uses MatrixOperations.intersect() and any out-of-bounds cell is treated as a collision. No wall-kick behaviour is implemented.

## Code smells (from reading the code)

- SMELL-01: Incomplete pause feature in `GuiController`
  - Evidence: `pauseGame(ActionEvent)` only calls `gamePanel.requestFocus()` and does not change isPause / timeLine state.
  - Impact: Pause button exists in UI but does not actually pause the game; confusing for users and maintainers.

- SMELL-02: Unused/empty score binding in `GuiController.bindScore(IntegerProperty)`
  - Evidence: Method body is empty.
  - Impact: Suggests an unfinished feature; unclear how score should be displayed or updated from the model.

- SMELL-03: `SimpleBoard` ignores constructor parameters
  - Evidence: Constructor takes `(int width, int height)` but internally always creates `new int[ROWS][COLUMNS]`.
  - Impact: Board size is effectively hard-coded; parameters are misleading and make the class harder to reuse.

- SMELL-04: Tight coupling between `GameController` and `SimpleBoard`
  - Evidence: `GameController` directly constructs `new SimpleBoard(ROWS, COLUMNS)` instead of using the `Board` interface.
  - Impact: Makes it harder to swap in another `Board` implementation or write unit tests with a fake board.

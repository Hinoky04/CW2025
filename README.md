# TetrisJFX - Coursework Documentation

## GitHub

**Repository Link:** https://github.com/Hinoky04/CW2025.git

---

## Compilation Instructions

### Prerequisites
- Java 23 or higher
- Maven 3.6+ (or use the included Maven wrapper)

### Step-by-Step Compilation

1. **Navigate to the project directory:**
   Navigate to the directory of this project folder. For examples :

   ```bash
   cd CW2025-MASTER # or the root of this folder 
   ```

2. **Clean and compile the project:**
   ```bash
   .\mvnw.cmd clean compile
   ```
   (On Windows, use `.\mvnw.cmd`. On Linux/Mac, use `./mvnw`)

3. **Run the application:**
   ```bash
   .\mvnw.cmd javafx:run
   ```

### Alternative: Using IDE
- Import the project as a Maven project in IntelliJ IDEA or Eclipse
- Ensure Java 23 is configured as the project SDK
- Run the `Main` class located at `src/main/java/com/comp2042/Main.java`

### Dependencies
All dependencies are managed by Maven and will be automatically downloaded:
- JavaFX Controls 21.0.6
- JavaFX FXML 21.0.6
- JavaFX Media 21.0.6
- JUnit 5.12.1 (for testing)

---

## Implemented and Working Properly

### Core Game Features
- **Classic Mode**: Traditional Tetris gameplay with standard speed progression
- **Survival Mode**: Endless mode with increasing difficulty and garbage row pressure
- **Invisible Mode**: Faster gameplay with dimmed/transparent landed blocks
- **Rush 40 Mode**: Clear 40 lines as fast as possible with milestone tracking

### Game Mechanics
- ✅ Piece movement (left, right, down, hard drop)
- ✅ Piece rotation with wall kick support
- ✅ Line clearing with score calculation
- ✅ Hold piece functionality
- ✅ Next piece preview
- ✅ Ghost piece visualization (with some known issues)
- ✅ Score system with bonuses for multiple line clears
- ✅ Level progression
- ✅ Game over detection and handling

### UI Features
- ✅ Main menu with game mode selection
- ✅ Settings screen with customizable key bindings
- ✅ Pause menu with Resume, Restart, and Main Menu options
- ✅ Game over screen with final statistics
- ✅ HUD displaying score, level, lines cleared, and next piece
- ✅ Notification system for score bonuses and milestones
- ✅ Tutorial/help panel accessible from main menu
- ✅ Audio system with background music and sound effects

### Code Quality Improvements
- ✅ Refactored `GuiController` by extracting helper classes for better maintainability
- ✅ Separated mode-specific logic into dedicated handler classes
- ✅ Improved code organization with clear package structure
- ✅ Added comprehensive Javadoc documentation
- ✅ Fixed Javadoc warnings (HTML tag issues in documentation)

---



## New Java Classes

### Helper Classes (Package: `com.comp2042.helpers`)
These classes were extracted from `GuiController` to improve code organization and maintainability:

1. **`GuiColorHelper.java`**
   - **Location:** `src/main/java/com/comp2042/helpers/GuiColorHelper.java`
   - **Purpose:** Manages color calculations and rendering for game pieces, including danger zone highlighting and game over effects.

2. **`GuiDangerHelper.java`**
   - **Location:** `src/main/java/com/comp2042/helpers/GuiDangerHelper.java`
   - **Purpose:** Handles danger zone visualization when pieces are near the top of the board.

3. **`GuiHudHelper.java`**
   - **Location:** `src/main/java/com/comp2042/helpers/GuiHudHelper.java`
   - **Purpose:** Manages HUD (Heads-Up Display) updates including score, level, lines cleared, and next piece display.

4. **`GuiInputHandler.java`**
   - **Location:** `src/main/java/com/comp2042/helpers/GuiInputHandler.java`
   - **Purpose:** Processes keyboard input events and converts them to game actions.

5. **`GuiLayoutHelper.java`**
   - **Location:** `src/main/java/com/comp2042/helpers/GuiLayoutHelper.java`
   - **Purpose:** Handles layout calculations and positioning of UI elements.

6. **`GuiNavigationHandler.java`**
   - **Location:** `src/main/java/com/comp2042/helpers/GuiNavigationHandler.java`
   - **Purpose:** Manages navigation actions like restart, settings, and main menu transitions.

7. **`GuiNotificationHandler.java`**
   - **Location:** `src/main/java/com/comp2042/helpers/GuiNotificationHandler.java`
   - **Purpose:** Handles displaying notifications for score bonuses, milestones, and congratulations messages.

8. **`GuiRenderingHelper.java`**
   - **Location:** `src/main/java/com/comp2042/helpers/GuiRenderingHelper.java`
   - **Purpose:** Manages rendering of game board, pieces, and visual elements.

9. **`GuiStateManager.java`**
   - **Location:** `src/main/java/com/comp2042/helpers/GuiStateManager.java`
   - **Purpose:** Manages game state transitions (playing, paused, game over).

10. **`GuiTimerHelper.java`**
    - **Location:** `src/main/java/com/comp2042/helpers/GuiTimerHelper.java`
    - **Purpose:** Manages game timer and automatic piece dropping.

### Mode Handler Classes (Package: `com.comp2042.mode`)

11. **`RushModeHandler.java`**
    - **Location:** `src/main/java/com/comp2042/mode/RushModeHandler.java`
    - **Purpose:** Handles Rush-40 mode-specific logic including line count tracking, milestone detection (10, 20, 30, 40 lines), and completion time calculation.

12. **`SurvivalModeHandler.java`**
    - **Location:** `src/main/java/com/comp2042/mode/SurvivalModeHandler.java`
    - **Purpose:** Handles Survival mode-specific logic including garbage row pressure (adding rows when no lines are cleared) and shield system (grants shields for clearing 4 lines at once).

### Model Classes

13. **`GameConfig.java`**
    - **Location:** `src/main/java/com/comp2042/models/GameConfig.java`
    - **Purpose:** Immutable configuration class that defines mode-specific settings such as drop speed, ghost visibility, grid visibility, and landed block visibility. Prepared for future expansion.

---

## Modified Java Classes

### Controllers

1. **`GuiController.java`**
   - **Location:** `src/main/java/com/comp2042/controllers/GuiController.java`
   - **Changes:**
     - Extracted functionality into helper classes to reduce complexity
     - Improved separation of concerns
     - Better organization of rendering, input handling, and state management
   - **Rationale:** The original `GuiController` was too large and violated the Single Responsibility Principle. Breaking it into focused helper classes improves maintainability and testability.

2. **`GameController.java`**
   - **Location:** `src/main/java/com/comp2042/controllers/GameController.java`
   - **Changes:**
     - Integrated mode-specific handlers (`RushModeHandler`, `SurvivalModeHandler`)
     - Added support for mode-specific logic execution
     - Improved game mode configuration handling
   - **Rationale:** To support different game modes with their unique mechanics, mode-specific handlers were integrated to keep the controller clean while allowing mode-specific behavior.

3. **`MainMenuController.java`**
   - **Location:** `src/main/java/com/comp2042/controllers/MainMenuController.java`
   - **Changes:**
     - Updated to support Invisible Mode (renamed from Hyper Mode)
     - Improved UI consistency
   - **Rationale:** To reflect the renamed game mode and improve user experience.

### Models

4. **`SimpleBoard.java`**
   - **Location:** `src/main/java/com/comp2042/models/SimpleBoard.java`
   - **Changes:**
     - Improved piece spawning position (moved to top of board)
     - Enhanced rotation logic with wall kick support
     - Better line clearing behavior
     - Improved grid alignment
   - **Rationale:** Based on bug fixes documented in `BUGS_AND_SMELLS.md`, these changes address gameplay issues and improve player experience.

5. **`GameMode.java`**
   - **Location:** `src/main/java/com/comp2042/models/GameMode.java`
   - **Changes:**
     - Added support for Invisible Mode (renamed from Hyper Mode)
     - Integrated with `GameConfig` for mode-specific settings
   - **Rationale:** To support the renamed game mode and prepare for mode-specific configurations.

6. **`GameOverPanel.java`**
   - **Location:** `src/main/java/com/comp2042/ui/GameOverPanel.java`
   - **Changes:**
     - Redesigned to match pause menu visual style
     - Improved layout and consistency
   - **Rationale:** To create a more cohesive UI experience across all game screens.

### Logic Classes

7. **`GhostPieceCalculator.java`**
   - **Location:** `src/main/java/com/comp2042/logic/GhostPieceCalculator.java`
   - **Changes:**
     - Simplified ghost piece calculation (temporary fix)
     - Added mode-aware visibility checks
   - **Rationale:** To address known issues with ghost piece rendering. A complete redesign is planned for future versions.

8. **`BrickRotator.java`**
   - **Location:** `src/main/java/com/comp2042/logic/BrickRotator.java`
   - **Changes:**
     - Improved rotation near walls (wall kick support)
   - Better collision detection during rotation
   - **Rationale:** To fix rotation issues when pieces are near board edges, improving gameplay feel.

### UI Classes

9. **`BoardRenderer.java`**
   - **Location:** `src/main/java/com/comp2042/ui/BoardRenderer.java`
   - **Changes:**
     - Improved grid alignment rendering
     - Better visual clarity during fast gameplay
   - **Rationale:** To address visual alignment issues and improve player experience.

10. **`HudManager.java`**
    - **Location:** `src/main/java/com/comp2042/ui/HudManager.java`
    - **Changes:**
      - Automatic score display updates
      - Better synchronization with game state
    - **Rationale:** To fix score display update issues and ensure HUD stays in sync with game state.

### Configuration

11. **`pom.xml`**
    - **Location:** `pom.xml` (project root)
    - **Changes:**
      - Updated Maven Javadoc plugin configuration
      - Fixed Javadoc generation settings
      - Added `sourceFileIncludes` configuration
    - **Rationale:** To fix Javadoc generation issues and ensure all packages are properly documented.

---

## Unexpected Problems

### 1. Javadoc Package Recognition Issue
**Problem:** Initially, Javadoc was only showing 2 packages instead of all 10 packages in the project.

**Symptoms:**
- Only `com.comp2042` and `com.comp2042.logic.bricks` appeared in the package index
- Classes from other packages were incorrectly documented under the root package

**Root Cause:** The Maven Javadoc plugin configuration needed adjustment. The `sourcepath` and `subpackages` settings were interfering with automatic package detection.

**Solution:**
- Removed the `sourcepath` and `subpackages` configuration from `pom.xml`
- Added `sourceFileIncludes` to ensure all Java files are included
- Regenerated Javadoc after cleaning the project

**Location:** `pom.xml` lines 99-111

### 2. Javadoc HTML Tag Warnings
**Problem:** Javadoc generation produced warnings about invalid HTML input (`<` character in documentation).

**Symptoms:**
- Warnings like: `warning: invalid input: '<'` in `@param` documentation

**Root Cause:** The `<=0` expression in Javadoc comments was being interpreted as an HTML tag.

**Solution:**
- Wrapped the expression in `{@code <=0}` to properly format it as code
- Fixed in `GameOverPanel.java` (line 132) and `GuiController.java` (line 751)

### 3. IDE Import Resolution Issues
**Problem:** IDE (IntelliJ/Eclipse) showed import errors for classes that clearly existed and compiled successfully.

**Symptoms:**
- Red squiggly lines under imports like `com.comp2042.models.GameMode`
- "Cannot be resolved" errors despite successful Maven compilation

**Root Cause:** IDE project structure was out of sync after `pom.xml` changes.

**Solution:**
- Reloaded Maven project in IDE
- Refreshed project structure
- The code was correct; the IDE just needed to refresh its understanding of the project

### 4. Ghost Piece Rendering Issues
**Problem:** Ghost piece had multiple rendering problems that were difficult to isolate.

**Symptoms:**
- Incorrect height calculation
- Cell misalignment
- Visibility in Invisible Mode when it shouldn't be visible

**Attempted Solutions:**
- Simplified the ghost piece calculation logic
- Added mode-aware checks
- Temporarily reduced functionality while designing a proper fix

**Current Status:** Documented as a known issue. A complete redesign of the ghost piece system is planned for future versions.

**Location:** `src/main/java/com/comp2042/logic/GhostPieceCalculator.java`

### 5. Maven Wrapper Command Recognition
**Problem:** On Windows, the `mvn` command was not recognized.

**Solution:** Used the Maven wrapper (`.\mvnw.cmd`) instead of requiring Maven to be installed globally. This ensures consistent build behavior across different environments.

---

## Additional Notes

### Documentation
- Comprehensive Javadoc documentation has been generated and is available at: `target/site/apidocs/Javadoc/index.html`
- A maintenance log is available in `BUGS_AND_SMELLS.md` documenting all updates and bug fixes

### Testing
- Unit tests are located in `src/test/java/com/comp2042/`
- Tests cover core game logic including board operations, brick rotation, collision detection, and mode-specific handlers
- Run tests with: `.\mvnw.cmd test`

### Build Artifacts
- Compiled classes: `target/classes/`
- Javadoc: `target/site/apidocs/Javadoc/`
- Test reports: `target/surefire-reports/`

---

## Conclusion

This coursework involved significant refactoring to improve code maintainability, implementation of multiple game modes with unique mechanics, and resolution of various bugs and issues. The codebase has been restructured to follow better software engineering principles while maintaining all core functionality. The helper class extraction from `GuiController` represents a major architectural improvement that makes the codebase more maintainable and testable.

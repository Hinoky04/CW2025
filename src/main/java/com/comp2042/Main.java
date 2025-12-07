package com.comp2042;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

import com.comp2042.controllers.GameController;
import com.comp2042.controllers.GuiController;
import com.comp2042.controllers.MainMenuController;
import com.comp2042.controllers.SettingsController;
import com.comp2042.models.GameMode;

/**
 * Main application class for TetrisJFX.
 * Manages the JavaFX application lifecycle, scene switching, and window configuration.
 * Handles navigation between main menu, game, and settings scenes.
 */
public class Main extends Application {

    // Window title shown in the title bar.
    private static final String WINDOW_TITLE = "TetrisJFX";

    // Initial logical size used before the window is maximized.
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 900;

    // Minimum window size to prevent the UI from being squashed too far.
    private static final int MIN_WINDOW_WIDTH = 600;
    private static final int MIN_WINDOW_HEIGHT = 800;

    // FXML paths in resources.
    private static final String GAME_FXML = "gameLayout.fxml";
    private static final String MAIN_MENU_FXML = "MainMenu.fxml";
    private static final String SETTINGS_FXML = "Settings.fxml";

    // Primary stage is kept so we can swap scenes (menu <-> game).
    private Stage primaryStage;

    /**
     * Initializes and starts the JavaFX application.
     * Sets up the primary stage, configures fullscreen mode, and displays the main menu.
     *
     * @param primaryStage the primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(WINDOW_TITLE);

        // Allow the user (and our code) to resize the window.
        this.primaryStage.setResizable(true);
        this.primaryStage.setMinWidth(MIN_WINDOW_WIDTH);
        this.primaryStage.setMinHeight(MIN_WINDOW_HEIGHT);

        // Set an initial logical size before maximising.
        this.primaryStage.setWidth(WINDOW_WIDTH);
        this.primaryStage.setHeight(WINDOW_HEIGHT);

        // Configure fullscreen behaviour:
        // - no hint text
        // - ESC does NOT exit fullscreen automatically (we use F11 instead),
        //   so ESC can be reserved for in-game actions like pause/menu.
        this.primaryStage.setFullScreenExitHint("");
        this.primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        // Start application on the main menu instead of directly in the game.
        showMainMenu();

        // Always start in fullscreen mode.
        this.primaryStage.setFullScreen(true);
    }

    /**
     * Loads and shows the main menu scene.
     * Displays the game mode selection screen with options for Classic, Survival, Hyper, and Rush 40 modes.
     */
    public void showMainMenu() {
        URL location = getClass().getClassLoader().getResource(MAIN_MENU_FXML);
        if (location == null) {
            throw new IllegalStateException("Cannot find FXML file " + MAIN_MENU_FXML);
        }

        try {
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();

            MainMenuController controller = loader.getController();
            controller.init(this); // allow controller to switch scenes via Main

            // Let the root layout grow with the stage size.
            bindRootToStageSize(root);

            // Do NOT force a new window size here; keep whatever the stage has
            // (maximized / fullscreen / normal).
            Scene scene = new Scene(root);
            attachFullscreenToggle(scene);

            primaryStage.setScene(scene);
            primaryStage.show();
            
            // Ensure fullscreen mode is maintained
            if (!primaryStage.isFullScreen()) {
                primaryStage.setFullScreen(true);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load main menu", e);
        }
    }

    /**
     * Loads and shows the settings scene.
     * Allows users to customize key bindings for game controls.
     *
     * @param returnToGameMode if not null, return to this game mode after saving/canceling;
     *                         if null, return to main menu
     */
    public void showSettingsScene(GameMode returnToGameMode) {
        URL location = getClass().getClassLoader().getResource(SETTINGS_FXML);
        if (location == null) {
            throw new IllegalStateException("Cannot find FXML file " + SETTINGS_FXML);
        }

        try {
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();

            SettingsController controller = loader.getController();
            controller.init(this, returnToGameMode);

            bindRootToStageSize(root);

            Scene scene = new Scene(root);
            attachFullscreenToggle(scene);
            
            // Make sure the scene can receive key events
            scene.setOnKeyPressed(null); // Clear any existing handlers

            primaryStage.setScene(scene);
            primaryStage.show();
            
            // Ensure fullscreen mode is maintained
            if (!primaryStage.isFullScreen()) {
                primaryStage.setFullScreen(true);
            }
            
            // Request focus on the root so it can capture key events
            root.requestFocus();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load settings scene", e);
        }
    }
    
    /**
     * Overloaded method for backward compatibility (from main menu).
     * Shows settings scene and returns to main menu after closing.
     */
    public void showSettingsScene() {
        showSettingsScene(null);
    }

    /**
     * Loads and shows the main game scene for the given mode.
     * Initializes the game board, GUI controller, and starts the game logic.
     * This reuses the existing gameLayout.fxml + controllers.
     *
     * @param mode the game mode to start (Classic, Survival, Hyper, or Rush 40)
     */
    public void showGameScene(GameMode mode) {
        System.out.println("showGameScene called with mode: " + mode);
        URL location = getClass().getClassLoader().getResource(GAME_FXML);
        if (location == null) {
            System.err.println("ERROR: Cannot find FXML file " + GAME_FXML);
            throw new IllegalStateException("Cannot find FXML file " + GAME_FXML);
        }
        System.out.println("Found FXML file at: " + location);

        try {
            FXMLLoader loader = new FXMLLoader(location);
            System.out.println("Loading FXML...");
            Parent root = loader.load();
            System.out.println("FXML loaded successfully");
            
            GuiController guiController = loader.getController();
            if (guiController == null) {
                System.err.println("ERROR: GuiController is null after loading FXML");
                throw new IllegalStateException("GuiController is null");
            }
            System.out.println("GuiController obtained: " + guiController);

            // Let GUI go back to menu and know which mode we are in.
            System.out.println("Initializing GuiController...");
            guiController.init(this);
            System.out.println("Setting game mode to: " + mode);
            guiController.setGameMode(mode);

            // Let the root layout grow with the stage size.
            bindRootToStageSize(root);

            // Again, do NOT force a fixed size scene.
            Scene scene = new Scene(root);
            attachFullscreenToggle(scene);

            System.out.println("Setting scene on stage...");
            primaryStage.setScene(scene);
            primaryStage.show();
            System.out.println("Scene set and stage shown");

            // Ensure fullscreen mode is maintained
            if (!primaryStage.isFullScreen()) {
                primaryStage.setFullScreen(true);
            }

            // Start game logic.
            System.out.println("Creating GameController...");
            try {
            new GameController(guiController, mode);
                System.out.println("GameController created successfully");
            } catch (Exception e) {
                System.err.println("ERROR: Failed to create GameController: " + e.getMessage());
                e.printStackTrace();
                // Don't throw - let the scene show even if GameController fails
            }
        } catch (IOException e) {
            System.err.println("ERROR: Failed to load game scene: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Failed to load game scene", e);
        } catch (Exception e) {
            System.err.println("ERROR: Unexpected exception in showGameScene: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Failed to load game scene", e);
        }
    }

    /**
     * If the root is a Region (BorderPane, AnchorPane, etc.), bind its preferred
     * size to the stage size so the layout always fills the window.
     *
     * @param root the root node of the scene to bind to stage size
     */
    private void bindRootToStageSize(Parent root) {
        if (root instanceof Region) {
            Region region = (Region) root;
            region.prefWidthProperty().bind(primaryStage.widthProperty());
            region.prefHeightProperty().bind(primaryStage.heightProperty());
        }
    }

    /**
     * Adds a global key handler to the given scene so F11 toggles fullscreen.
     * This works for both the main menu and the game scene.
     *
     * @param scene the scene to attach the fullscreen toggle handler to
     */
    private void attachFullscreenToggle(Scene scene) {
        if (scene == null) {
            return;
        }
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F11) {
                primaryStage.setFullScreen(!primaryStage.isFullScreen());
                event.consume();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}

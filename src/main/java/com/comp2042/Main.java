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

    // Primary stage is kept so we can swap scenes (menu <-> game).
    private Stage primaryStage;

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

        // Start maximized to give a fullscreen-like experience by default.
        // Players can still toggle real fullscreen with F11.
        this.primaryStage.setMaximized(true);
    }

    /**
     * Loads and shows the main menu scene.
     */
    void showMainMenu() {
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
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load main menu", e);
        }
    }

    /**
     * Loads and shows the main game scene for the given mode.
     * This reuses the existing gameLayout.fxml + controllers.
     */
    void showGameScene(GameMode mode) {
        URL location = getClass().getClassLoader().getResource(GAME_FXML);
        if (location == null) {
            throw new IllegalStateException("Cannot find FXML file " + GAME_FXML);
        }

        try {
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();
            GuiController guiController = loader.getController();

            // Let GUI go back to menu and know which mode we are in.
            guiController.init(this);
            guiController.setGameMode(mode);

            // Let the root layout grow with the stage size.
            bindRootToStageSize(root);

            // Again, do NOT force a fixed size scene.
            Scene scene = new Scene(root);
            attachFullscreenToggle(scene);

            primaryStage.setScene(scene);
            primaryStage.show();

            // Do not touch maximized/fullscreen here; keep whatever the user chose.

            // Start game logic.
            new GameController(guiController, mode);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load game scene", e);
        }
    }

    /**
     * If the root is a Region (BorderPane, AnchorPane, etc.), bind its preferred
     * size to the stage size so the layout always fills the window.
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

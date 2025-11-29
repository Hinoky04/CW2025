package com.comp2042;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    // === Window configuration ===
    // Title shown in the window title bar.
    private static final String WINDOW_TITLE = "TetrisJFX";

    // Fixed window size for a stable layout.
    private static final int WINDOW_WIDTH = 300;
    private static final int WINDOW_HEIGHT = 510;

    // === FXML paths in resources ===
    private static final String GAME_FXML = "gameLayout.fxml";
    private static final String MAIN_MENU_FXML = "MainMenu.fxml";

    // Primary stage is kept so we can swap scenes (menu <-> game).
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(WINDOW_TITLE);
        this.primaryStage.setResizable(false); // lock size to avoid layout issues

        // Entry point for the app: show the main menu first,
        // the menu will decide which game mode to start.
        showMainMenu();
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

            // Inject Main so the menu controller can call back into showGameScene(...).
            MainMenuController controller = loader.getController();
            controller.init(this);

            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load main menu", e);
        }
    }

    /**
     * Convenience overload used by older code paths.
     * Defaults to CLASSIC mode so existing behaviour stays the same.
     * Once all calls are updated to pass a GameMode, this can be removed.
     */
    void showGameScene() {
        showGameScene(GameMode.CLASSIC);
    }

    /**
     * Loads and shows the main game scene for the given mode.
     * The mode is passed down to GameController so the rules/config
     * can be customised per mode (Classic, Survival, etc.).
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

            // Allow GUI to navigate back to the main menu.
            guiController.init(this);

            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            primaryStage.setScene(scene);
            primaryStage.show();

            // Start the game logic for the selected mode.
            // GameController will later use 'mode' to choose different configs.
            new GameController(guiController, mode);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load game scene", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

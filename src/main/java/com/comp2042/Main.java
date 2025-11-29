package com.comp2042;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    // Window title shown in the title bar
    private static final String WINDOW_TITLE = "TetrisJFX";

    // Initial window size (scene size). The stage will be maximized anyway,
    // but these values are still used as the "logical" game area.
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 900;

    // FXML paths in resources
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

        // Start application on the main menu instead of directly in the game.
        showMainMenu();

        // Safe "fullscreen-like" behaviour: start maximized on whatever screen.
        this.primaryStage.setMaximized(true);
        // If you really want real fullscreen with no window border, use:
        // this.primaryStage.setFullScreen(true);
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

            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            primaryStage.setScene(scene);
            primaryStage.show();

            // Keep window maximized when returning to the menu.
            primaryStage.setMaximized(true);
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

            // Let GUI go back to menu and know which mode we are in
            guiController.init(this);
            guiController.setGameMode(mode);

            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            primaryStage.setScene(scene);
            primaryStage.show();

            // 🔥 make sure the game scene is also fullscreen / maximized
            primaryStage.setMaximized(true);
            // or: primaryStage.setFullScreen(true);

            // Start game logic
            new GameController(guiController, mode);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load game scene", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

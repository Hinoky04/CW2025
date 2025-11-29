package com.comp2042;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Entry point of the JavaFX application.
 * Shows the main menu first, then switches to the game scene
 * for the selected GameMode.
 */
public class Main extends Application {

    // Window title and fixed size.
    private static final String WINDOW_TITLE = "TetrisJFX";
    private static final int WINDOW_WIDTH = 300;
    private static final int WINDOW_HEIGHT = 510;

    // FXML paths in resources.
    private static final String GAME_FXML = "gameLayout.fxml";
    private static final String MAIN_MENU_FXML = "MainMenu.fxml";

    // We keep a reference to the primary stage so we can swap scenes.
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(WINDOW_TITLE);

        // Start on the main menu instead of directly in the game.
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

            // Give the controller a reference back to this Main class.
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
     * Loads and shows the main game scene for the given GameMode.
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

            // Allow the GUI to navigate back to the main menu.
            guiController.init(this);

            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            primaryStage.setScene(scene);
            primaryStage.show();

            // Start the game logic for the selected mode.
            new GameController(guiController, mode);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load game scene", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

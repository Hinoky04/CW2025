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
    private static final String WINDOW_TITLE = "TetrisJFX";
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

        // Start application on the main menu instead of directly in the game.
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

            MainMenuController controller = loader.getController();
            controller.init(this); // allow controller to switch scenes via Main

            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load main menu", e);
        }
    }

    /**
     * Loads and shows the main game scene.
     * This reuses the existing gameLayout.fxml + controllers.
     */
    void showGameScene() {
        URL location = getClass().getClassLoader().getResource(GAME_FXML);
        if (location == null) {
            throw new IllegalStateException("Cannot find FXML file " + GAME_FXML);
        }

        try {
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();
            GuiController guiController = loader.getController();

            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            primaryStage.setScene(scene);
            primaryStage.show();

            // Start the game logic. The instance is not needed elsewhere for now.
            new GameController(guiController);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load game scene", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

package com.comp2042;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {
    // ===Configuration constants===
    // Window title shown in the title bar 
    private static final String WINDOW_TITLE = "TetrisJFX";

    // Window size in pixels
    private static final int WINDOW_WIDTH = 300;
    private static final int WINDOW_HEIGHT = 510;

    // Path to the FXML file in resources
    private static final String FXML_PATH = "gameLayout.fxml";

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Locate the FXML file
        URL location = getClass().getClassLoader().getResource(FXML_PATH);
        if(location == null){
            // CLearer error message if the file is missing or the path is wrong
            throw new IllegalStateException("Cannot find FXML file " + FXML_PATH);
        }

        // Load the layout and controller
        FXMLLoader loader = new FXMLLoader(location);
        Parent root = loader.load();
        GuiController guiController = loader.getController();

        // Set up and show the main window
        primaryStage.setTitle(WINDOW_TITLE);
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();

        //Start the game logic 
        GameController gameController = new GameController(guiController);
        // gameController is kept in case in case we need it later
    }


    public static void main(String[] args) {
        launch(args);
    }
}

package com.comp2042.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.comp2042.Main;
import com.comp2042.models.GameMode;

/**
 * Controller for the main menu screen.
 * Responsible only for menu actions (choose mode, quit).
 */
public class MainMenuController implements Initializable {

    // Reference to the Main application so we can switch scenes.
    private Main mainApp;

    /**
     * Called by Main after loading the FXML.
     * We do not own the Stage, we just ask Main to switch scenes.
     */
    public void init(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private Label titleLabel;

    @FXML
    private Button classicButton;

    @FXML
    private Button survivalButton;

    @FXML
    private Button hyperButton;

    @FXML
    private Button rushButton;

    @FXML
    private Button settingsButton;

    @FXML
    private Button quitButton;

    @FXML
    private StackPane tutorialOverlay;

    @FXML
    private javafx.scene.layout.Region tutorialBackground;

    @FXML
    private Button tutorialCloseButton;

    @FXML
    private Button tutorialButton;

    @FXML
    private Button maintenanceButton;

    @FXML
    private StackPane maintenanceOverlay;

    @FXML
    private Button maintenanceCloseButton;

    @FXML
    private VBox maintenanceContent;

    // Track if tutorial has been shown (static so it persists across menu visits)
    private static boolean tutorialShown = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Ensure overlays are properly initialized
        if (tutorialOverlay != null) {
            tutorialOverlay.setVisible(false);
            tutorialOverlay.setMouseTransparent(true);
        }
        if (maintenanceOverlay != null) {
            maintenanceOverlay.setVisible(false);
            maintenanceOverlay.setMouseTransparent(true);
        }
        
        // Show tutorial on first visit
        if (!tutorialShown && tutorialOverlay != null) {
            Platform.runLater(() -> {
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(300));
                delay.setOnFinished(e -> {
                    tutorialOverlay.setVisible(true);
                    tutorialOverlay.toFront();
                    // Make overlay block clicks, but background region allows clicks through
                    tutorialOverlay.setMouseTransparent(false);
                    if (tutorialBackground != null) {
                        tutorialBackground.setMouseTransparent(true);
                    }
                });
                delay.play();
            });
        }

        // Wire tutorial close button
        if (tutorialCloseButton != null) {
            tutorialCloseButton.setOnAction(e -> closeTutorial());
        }

        // Wire maintenance button
        if (maintenanceCloseButton != null) {
            maintenanceCloseButton.setOnAction(e -> closeMaintenance());
        }

        // Wire tutorial button (bottom left icon)
        if (tutorialButton != null) {
            tutorialButton.setOnAction(e -> showTutorial());
        }

        // Load maintenance button icon
        if (maintenanceButton != null) {
            try {
                Image iconImage = new Image(getClass().getResourceAsStream("/maintenance_icon.png"));
                ImageView iconView = new ImageView(iconImage);
                iconView.setFitWidth(24);
                iconView.setFitHeight(24);
                iconView.setPreserveRatio(true);
                
                // Make the icon white
                ColorAdjust colorAdjust = new ColorAdjust();
                colorAdjust.setBrightness(1.0); // Maximum brightness (white)
                colorAdjust.setSaturation(-1.0); // Remove all color (grayscale)
                iconView.setEffect(colorAdjust);
                
                maintenanceButton.setGraphic(iconView);
            } catch (Exception e) {
                // If image fails to load, button will just show without icon
                System.err.println("Failed to load maintenance icon: " + e.getMessage());
            }
        }

        // Load maintenance content
        loadMaintenanceContent();
    }

    /**
     * Closes the tutorial overlay.
     */
    private void closeTutorial() {
        if (tutorialOverlay != null) {
            tutorialOverlay.setVisible(false);
            tutorialOverlay.setMouseTransparent(true);
            tutorialShown = true;
        }
    }

    /**
     * Shows the tutorial overlay.
     */
    @FXML
    private void handleTutorial(ActionEvent event) {
        showTutorial();
    }

    /**
     * Shows the tutorial overlay.
     */
    private void showTutorial() {
        if (tutorialOverlay != null) {
            tutorialOverlay.setVisible(true);
            tutorialOverlay.toFront();
            // Make overlay block clicks, but background region allows clicks through
            tutorialOverlay.setMouseTransparent(false);
            if (tutorialBackground != null) {
                tutorialBackground.setMouseTransparent(true);
            }
        }
        // Ensure maintenance overlay is hidden and doesn't block
        if (maintenanceOverlay != null) {
            maintenanceOverlay.setVisible(false);
            maintenanceOverlay.setMouseTransparent(true);
        }
    }

    /**
     * Opens the maintenance notification overlay.
     */
    @FXML
    private void handleMaintenance(ActionEvent event) {
        if (maintenanceOverlay != null) {
            maintenanceOverlay.setVisible(true);
            maintenanceOverlay.toFront();
            maintenanceOverlay.setMouseTransparent(false);
        }
        // Ensure tutorial overlay is hidden and doesn't block
        if (tutorialOverlay != null) {
            tutorialOverlay.setVisible(false);
            tutorialOverlay.setMouseTransparent(true);
        }
    }

    /**
     * Closes the maintenance overlay.
     */
    private void closeMaintenance() {
        if (maintenanceOverlay != null) {
            maintenanceOverlay.setVisible(false);
            maintenanceOverlay.setMouseTransparent(true);
        }
    }

    /**
     * Loads the content from BUGS_AND_SMELLS.md and populates the maintenance panel.
     */
    private void loadMaintenanceContent() {
        if (maintenanceContent == null) {
            return;
        }

        try {
            // Read BUGS_AND_SMELLS.md from file system (it's in the project root)
            InputStream inputStream = null;
            try {
                java.nio.file.Path path = java.nio.file.Paths.get("BUGS_AND_SMELLS.md");
                if (java.nio.file.Files.exists(path)) {
                    inputStream = java.nio.file.Files.newInputStream(path);
                } else {
                    // Try from resources as fallback
                    inputStream = getClass().getResourceAsStream("/BUGS_AND_SMELLS.md");
                }
            } catch (Exception e) {
                // Try from resources as fallback
                inputStream = getClass().getResourceAsStream("/BUGS_AND_SMELLS.md");
            }
            
            if (inputStream == null) {
                maintenanceContent.getChildren().add(new Label("Could not load maintenance notes."));
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String content = reader.lines()
                        .collect(Collectors.joining("\n"));

                // Parse and format the markdown content
                String[] lines = content.split("\n");
                boolean skipFirstHeading = true; // Skip the main "Bug & Code Smell Log" heading
                String previousLineType = null; // Track previous line type to reduce spacing
                
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    String nextLine = (i + 1 < lines.length) ? lines[i + 1] : "";
                    
                    // Skip empty lines that come right before or after headings
                    if (line.trim().isEmpty()) {
                        // Only add spacing if next line is not a heading and previous was not a heading
                        if (previousLineType != null && !previousLineType.equals("heading") && 
                            !nextLine.startsWith("#") && !nextLine.startsWith("##") && !nextLine.startsWith("###")) {
                            Label label = new Label(" ");
                            label.setMinHeight(4);
                            maintenanceContent.getChildren().add(label);
                        }
                        continue;
                    }
                    
                    Label label = new Label(line);
                    
                    // Style based on markdown formatting
                    if (line.startsWith("# ")) {
                        // Main title - skip the first one since we already have "MAINTENANCE LOG" as the overlay title
                        if (skipFirstHeading) {
                            skipFirstHeading = false;
                            previousLineType = null;
                            continue;
                        }
                        label.getStyleClass().add("tutorial-title");
                        label.setText(line.substring(2));
                        previousLineType = "heading";
                    } else if (line.startsWith("## ")) {
                        // Section header
                        label.getStyleClass().add("tutorial-section");
                        label.setText(line.substring(3));
                        previousLineType = "heading";
                    } else if (line.startsWith("### ")) {
                        // Subsection header - make it more compact
                        label.getStyleClass().add("tutorial-section");
                        label.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 4 0 2 0;");
                        label.setText(line.substring(4));
                        previousLineType = "heading";
                    } else if (line.startsWith("- ") || line.startsWith("  - ")) {
                        // List item
                        label.getStyleClass().add("tutorial-text");
                        label.setWrapText(true);
                        label.setText(line.replaceFirst("^\\s*-\\s*", "• "));
                        previousLineType = "list";
                    } else if (line.startsWith("---")) {
                        // Horizontal rule - skip
                        previousLineType = null;
                        continue;
                    } else {
                        // Regular text
                        label.getStyleClass().add("tutorial-text");
                        label.setWrapText(true);
                        previousLineType = "text";
                    }
                    
                    maintenanceContent.getChildren().add(label);
                }
            }
        } catch (Exception e) {
            maintenanceContent.getChildren().add(new Label("Error loading maintenance notes: " + e.getMessage()));
        }
    }

    // --- Button handlers ---

    @FXML
    private void handleClassic(ActionEvent event) {
        System.out.println("handleClassic called");
        // Close any open overlays first
        closeTutorial();
        closeMaintenance();
        
        if (mainApp != null) {
            System.out.println("Calling mainApp.showGameScene(CLASSIC)");
            mainApp.showGameScene(GameMode.CLASSIC);
        } else {
            System.err.println("Error: mainApp is null in handleClassic");
        }
    }

    @FXML
    private void handleSurvival(ActionEvent event) {
        System.out.println("handleSurvival called");
        // Close any open overlays first
        closeTutorial();
        closeMaintenance();
        
        if (mainApp != null) {
            System.out.println("Calling mainApp.showGameScene(SURVIVAL)");
            mainApp.showGameScene(GameMode.SURVIVAL);
        } else {
            System.err.println("Error: mainApp is null in handleSurvival");
        }
    }

    @FXML
    private void handleHyper(ActionEvent event) {
        System.out.println("handleHyper called");
        // Close any open overlays first
        closeTutorial();
        closeMaintenance();
        
        if (mainApp != null) {
            System.out.println("Calling mainApp.showGameScene(HYPER)");
            mainApp.showGameScene(GameMode.HYPER);
        } else {
            System.err.println("Error: mainApp is null in handleHyper");
        }
    }

    @FXML
    private void handleRush(ActionEvent event) {
        System.out.println("handleRush called");
        // Close any open overlays first
        closeTutorial();
        closeMaintenance();
        
        if (mainApp != null) {
            System.out.println("Calling mainApp.showGameScene(RUSH_40)");
            mainApp.showGameScene(GameMode.RUSH_40);
        } else {
            System.err.println("Error: mainApp is null in handleRush");
        }
    }

    @FXML
    private void handleSettings(ActionEvent event) {
        if (mainApp != null) {
            mainApp.showSettingsScene();
        }
    }

    @FXML
    private void handleQuit(ActionEvent event) {
        Platform.exit();
    }
}

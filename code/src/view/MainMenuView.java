package src.view;

import java.util.Locale;


import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.common.ResourceManager;
import src.controller.MainMenuController;
import javafx.scene.control.Label;

public class MainMenuView extends VBox {
    MainMenuController controller;

    public MainMenuView(MainMenuController controller, Stage primaryStage){
        super(10);
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("main-region");


        Scene scene = new Scene(this, ResourceManager.resolutionWidth, ResourceManager.resolutionHeight);
        scene.getStylesheets().add(getClass().getResource("/css/steampunk.css").toString());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();


        this.controller = controller;

        Button game = new Button(ResourceManager.getString("start_game"));
        game.setOnAction(e -> {this.controller.startGame(primaryStage);});

        Button editor = new Button(ResourceManager.getString("start_editor"));
        editor.setOnAction(e -> {this.controller.startEditor(primaryStage);});



        // --- CONFIGURATIONS ---
        Label configLabel = new Label("—————————— " + ResourceManager.getString("configTitle") + " ——————————");
        configLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        Button showKeysButton = new Button(ResourceManager.getString("show_keys_title"));
        showKeysButton.setOnAction(e -> showKeysWindow());


        Label resolutionLabel = new Label(ResourceManager.getString("resolution_title"));
        ComboBox<String> resolutionCombo = new ComboBox<>();
        resolutionCombo.getItems().addAll("1920x1080", "1280x720");
        resolutionCombo.setValue(ResourceManager.resolutionWidth + "x" + ResourceManager.resolutionHeight); 
        resolutionCombo.setOnAction(e -> {this.controller.handleResolutionChange(resolutionCombo.getValue());});


        Label languageLabel = new Label(ResourceManager.getString("select_language"));
        ComboBox<String> languageSelection = new ComboBox<>();
        languageSelection.getItems().addAll("Français", "English");
        if (Locale.ENGLISH.equals(ResourceManager.getLocale())) {
            languageSelection.setValue("English");
        } else if (Locale.FRENCH.equals(ResourceManager.getLocale())) {
            languageSelection.setValue("Français");
        } else {
            languageSelection.setValue("English");
        }
        
        languageSelection.setOnAction(event -> { this.controller.handleLanguageChange(languageSelection.getValue());});

        VBox configsVBox = new VBox(15);
        configsVBox.setAlignment(Pos.CENTER);
        configsVBox.setStyle("-fx-padding: 20");

        configsVBox.getChildren().addAll(
                configLabel,
                showKeysButton,
                resolutionLabel,
                resolutionCombo,
                languageLabel,
                languageSelection
        );




        // --- AUTEURS ---
        Label authorTitle = new Label("—————————— " + ResourceManager.getString("authorTitle") + " ——————————");
        Label nom1 = new Label("Matthieu PETIT");
        Label nom2 = new Label("Zack HÉBERT");
        Label nom3 = new Label("Erwann BRICET");

        // Mise en page verticale
        VBox authorVBox = new VBox(10); // 10 pixels d'espacement
        authorVBox.setAlignment(Pos.CENTER);
        authorVBox.getChildren().addAll(authorTitle, nom1, nom2, nom3);



        this.getChildren().addAll(game, editor, configsVBox, authorVBox);


    }


    private void showKeysWindow() {
        Stage keysStage = new Stage();
        keysStage.initModality(Modality.APPLICATION_MODAL);
        VBox layout = new VBox(10);
        layout.getStyleClass().add("main-region");
        layout.setStyle("-fx-padding: 10");
        layout.getChildren().add(new Label(ResourceManager.getString("keys_list")));
        Scene scene = new Scene(layout, 600, 600);
        scene.getStylesheets().add(getClass().getResource("/css/steampunk.css").toString());
        keysStage.setTitle(ResourceManager.getString("keys_list_title"));
        keysStage.setScene(scene);
        keysStage.showAndWait();
    }
}

package src.view;

import java.util.Locale;


import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import src.common.ResourceManager;
import src.controller.MainMenuController;
import javafx.scene.control.Label;
import java.util.Locale;

public class MainMenuView extends VBox {
    private final int SCENE_WIDTH = 800, SCENE_HEIGHT = 600; 
    MainMenuController controller;

    public MainMenuView(MainMenuController controller, Stage primaryStage){
        super(10);
        this.setAlignment(Pos.CENTER);


        Scene scene = new Scene(this, SCENE_WIDTH, SCENE_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/css/steampunk.css").toString());
        primaryStage.setScene(scene);


        this.controller = controller;

        Button game = new Button(ResourceManager.getString("start_game"));
        game.setOnAction(e -> {this.controller.startGame(primaryStage);});

        Button editor = new Button(ResourceManager.getString("start_editor"));
        editor.setOnAction(e -> {this.controller.startEditor(primaryStage);});

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

        this.getChildren().addAll(game, editor, languageLabel, languageSelection);


    }
}

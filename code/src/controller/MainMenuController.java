package src.controller;

import java.util.Locale;

import javafx.stage.Stage;
import src.common.ResourceManager;
import src.controller.editor.GameEditorController;
import src.controller.game.GameController;
import src.view.MainMenuView;

public class MainMenuController {
    MainMenuView view;
    Stage stage;

    public MainMenuController(Stage primaryStage){
        this.stage = primaryStage;
        this.view = new MainMenuView(this, primaryStage);
    }

    public void startGame(Stage primaryStage){
        new GameController(primaryStage);
    }


    public void startEditor(Stage primaryStage){
        new GameEditorController(primaryStage);
    }


    public void handleLanguageChange(String selected){
            switch (selected) {
                case "Français":
                    ResourceManager.setLocale(Locale.FRENCH);
                    this.view = new MainMenuView(this, this.stage);
                    break;
                case "English":
                default:
                    ResourceManager.setLocale(Locale.ENGLISH);
                    break;
            }
            //Stage tempStage = new Stage();
            this.view = new MainMenuView(this, this.stage);
            //tempStage.showAndWait();
            
            //refreshTexts();
    } 
}

package src.controller;

import javafx.stage.Stage;
import src.controller.editor.GameEditorController;
import src.controller.game.GameController;
import src.view.MainMenuView;

public class MainMenuController {
    MainMenuView view;

    public MainMenuController(Stage primaryStage){
        this.view = new MainMenuView(this, primaryStage);
    }

    public void startGame(Stage primaryStage){
        new GameController(primaryStage);
    }


    public void startEditor(Stage primaryStage){
        new GameEditorController(primaryStage);
    }
}

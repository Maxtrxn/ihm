package src.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import src.controller.MainMenuController;

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

        Button game = new Button("Lancer le jeu");
        game.setOnAction(e -> {this.controller.startGame(primaryStage);});

        Button editor = new Button("Lancer l'éditeur de niveau");
        editor.setOnAction(e -> {this.controller.startEditor(primaryStage);});

        this.getChildren().addAll(game, editor);


    }
}

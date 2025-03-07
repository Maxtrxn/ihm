package src;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import src.view.GameEditorView;
import src.model.GameEditor;
import src.controller.GameEditorController;
import javafx.scene.input.MouseEvent;

public class Main extends Application {
    private static final int ROWS = 10;
    private static final int COLS = 100;
    private static final int CELL_SIZE = 64;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        GameEditorController controller = startEditorMode();


        primaryStage.setMaximized(true);
        primaryStage.setScene(new Scene(controller.getView().getBorderPane(), 100, 100));
        primaryStage.setTitle("Editor");
        primaryStage.show();
    }


    public GameEditorController startEditorMode(){
        GameEditorController controller = new GameEditorController(ROWS, COLS, CELL_SIZE);
        
        return controller;
    }

    public void startGame(){
        return;
    }
}
package src.view.editor;

import src.controller.editor.GameEditorController;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.Scene;

import src.common.ResourceManager;


public class GameEditorView extends BorderPane{
    private final GameEditorController controller;
    private Stage stage;


    public GameEditorView(GameEditorController controller, Stage stage) {
        super();
        this.controller = controller;
        this.stage = stage;
        
        Scene scene = new Scene(this, ResourceManager.resolutionWidth, ResourceManager.resolutionHeight);
        ResourceManager.setCurrStyleSheetToScene(scene);
        stage.setScene(scene); 
        stage.setResizable(true);
    }

    public GameEditorController getController() {return this.controller;}
    public Stage getStage() {return this.stage;}
}

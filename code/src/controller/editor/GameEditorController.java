package src.controller.editor;


import javafx.scene.image.Image;
import javafx.stage.Stage;
import src.model.editor.GameEditorModel;
import src.view.editor.GameEditorView;


public class GameEditorController{
    private GameEditorModel model = null;
    private GameEditorView view = null;
    

    public GameEditorController(Stage stage) {
        this.model = new GameEditorModel(this);
        this.view = new GameEditorView(this, stage);
    }

    public void setModel(GameEditorModel model){
        this.model = model;
    }

    public void setView(GameEditorView view){
        this.view = view;
    }

    public GameEditorModel getModel(){
        return this.model;
    }

    public GameEditorView getView(){
        return this.view;
    }


    public void updateSelectedPlatform(String platformName){
        this.model.setSelectedPlatformName(platformName);
    }

    public void initLevel(String levelName, double levelWidth, double levelHeight){
        this.model.initLevel(levelWidth, levelHeight);
        this.model.setLevelName(levelName);
    }

    public void updateBackground(Image image){
        this.model.setLevelBackground(image);
    }

    public void saveLevel(){
        this.model.saveLevel();
    }

    public void addPlatform(double x, double y){
        this.model.addPlatform(x, y);
    }
}
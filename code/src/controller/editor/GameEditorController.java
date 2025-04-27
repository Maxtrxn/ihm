package src.controller.editor;


import javafx.scene.image.Image;
import javafx.stage.Stage;
import src.model.editor.GameEditorModel;
import src.model.game.Level;
import src.view.editor.GameEditorView;


public class GameEditorController{
    private GameEditorModel model = null;
    private GameEditorView view = null;
    

    public GameEditorController(Stage stage) {
        this.model = new GameEditorModel(this);
        this.view = new GameEditorView(this, stage);
    }

    public void setModel(GameEditorModel model){this.model = model;}
    public void setView(GameEditorView view){this.view = view;}
    public GameEditorModel getModel(){return this.model;}
    public GameEditorView getView(){return this.view;}


    public void updateSelectedLevelObjectName(String name) {this.model.setSelectedLevelObjectName(name);}

    public void initLevel(String levelName, double levelWidth, double levelHeight){
        this.model.initLevel(levelWidth, levelHeight);
        this.model.setLevelName(levelName);
    }

    public void updateBackground(Image image){this.model.setLevelBackground(image);}
    public void saveLevel(boolean overwrite){this.model.saveLevel(overwrite);}
    public void addPlatform(double x, double y){this.model.addPlatform(x, y);}
    public void addDecoration(double x, double y, boolean foreground){this.model.addDecoration(x, y, foreground);}
    public void addEnemy(double x, double y, double leftBound, double rightBound, double speed){return;}//A FAIRE -------------------
    public Level getLevel(){return this.model.getLevel();}
}
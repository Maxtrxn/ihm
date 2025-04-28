package src.controller.editor;


import javafx.scene.image.Image;
import javafx.stage.Stage;
import src.model.editor.GameEditorModel;
import src.model.game.Level;
import src.model.game.LevelObject;
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

    public void initLevel(double levelWidth, double levelHeight){
        this.model.initLevel(levelWidth, levelHeight);
    }

    public void clickSelectLevelObject(double mouseClickPosX, double mouseClickPosY){this.model.clickSelectLevelObject(mouseClickPosX, mouseClickPosY);}
    public void updateLevelName(String levelName){this.model.setLevelName(levelName);}
    public void updateBackground(Image image){this.model.setLevelBackground(image);}
    public void saveLevel(boolean overwrite){this.model.saveLevel(overwrite);}
    public void deleteLevel(String levelName){this.model.deleteLevel(levelName);}
    public void addPlatform(double x, double y){this.model.addPlatform(x, y);}
    public void addDecoration(double x, double y, boolean foreground){this.model.addDecoration(x, y, foreground);}
    public void addEnemy(double x, double y, double leftBound, double rightBound, double speed){this.model.addEnemy(x, y, leftBound, rightBound, speed);}
    public Level getLevel(){return this.model.getLevel();}
    public LevelObject getClickSelectedLevelObject(){return this.model.getClickSelectedLevelObject();}
    public void loadLevel(String levelName){
        this.model.loadLevel(levelName);
        this.view.initLevel(getLevel(), levelName);
    }


}
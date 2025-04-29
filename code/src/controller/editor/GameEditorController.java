package src.controller.editor;


import java.beans.PropertyChangeEvent;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import src.controller.game.GameController;
import src.model.editor.GameEditorModel;
import src.model.game.Level;
import src.model.game.LevelObject;
import src.view.editor.GameEditorView;


public class GameEditorController{
    public enum LevelObjectType {
        PLATFORM, FRAGILE_PLATFORM, DECORATION, ENEMY, BOSS;
    }
    private GameEditorModel model = null;
    private GameEditorView view = null;
    

    public GameEditorController(Stage stage) {
        this.model = new GameEditorModel(this);
        this.view = new GameEditorView(this, stage);

        EditorMenuBarController subController = new EditorMenuBarController(this.model);
        this.view.setTop(subController.getEditorMenuBarView());

        this.model.addPropertyChangeListener("changeLevelName", e -> {stage.setTitle("Steampunk Adventure - Éditeur de niveau" + " - " + e.getNewValue());});
        this.model.addPropertyChangeListener("initLevel", e -> handleInitLevel(e));
    }

    public void setModel(GameEditorModel model){this.model = model;}
    public void setView(GameEditorView view){this.view = view;}
    public GameEditorModel getModel(){return this.model;}
    public GameEditorView getView(){return this.view;}


    public void updateSelectedLevelObjectName(String name) {this.model.setSelectedLevelObjectName(name);}

    public void updateLevelName(String levelName){this.model.setLevelName(levelName);}
    public void updateBackground(Image image){this.model.setLevelBackground(image);}
    public void saveLevel(boolean overwrite){this.model.saveLevel(overwrite);}
    public void deleteLevel(String levelName){this.model.deleteLevel(levelName);}
    public void addPlatform(double x, double y){this.model.addPlatform(x, y);}
    public void addDecoration(double x, double y, boolean foreground){this.model.addDecoration(x, y, foreground);}
    public void addEnemy(double x, double y, double leftBound, double rightBound, double speed){this.model.addEnemy(x, y, leftBound, rightBound, speed);}
    public Level getLevel(){return this.model.getLevel();}
    public void loadLevel(String levelName){
        this.model.loadLevel(levelName);
        this.view.initLevel(getLevel(), levelName);
    }

    public void testLevel(Stage gameStage){
        new GameController(gameStage, this.model.getLevelName());
    }

    public void handleInitLevel(PropertyChangeEvent e){
        MapEditorController subController = new MapEditorController(model, (Level)e.getNewValue());
        this.view.setCenter(subController.getMapEditorScrollPane());
    }
}
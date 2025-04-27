package src.model.editor;


import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import src.common.JsonReader;
import src.common.ResourcesPaths;
import src.controller.editor.GameEditorController;
import src.model.game.Level;

import java.io.File;

import org.json.JSONObject;


public class GameEditorModel{
    private final GameEditorController controller;
    private Level level = null;
    private String levelName = null;
    private String selectedLevelObjectName = null;


    public GameEditorModel(GameEditorController controller){
        this.controller = controller;
        this.controller.setModel(this);
    }

    public void setLevelName(String levelName){this.levelName = levelName;}
    public void setSelectedLevelObjectName(String name){this.selectedLevelObjectName = name;}
    public void initLevel(double levelWidth, double levelHeight){this.level = new Level(levelWidth, levelHeight);}
    public void setLevelBackground(Image bg){this.level.setBackgroundImage(bg);}
    public Level getLevel(){return this.level;}
    

    public void addPlatform(double x, double y){
        this.level.addPlatform(x, y, selectedLevelObjectName);
    }
    

    //Booléen foreground pour savoir si on ajoute la décoration au premier au à l'arrière plan
    public void addDecoration(double x, double y, boolean foreground){
        if(foreground){
            this.level.addDecoration(x, y, selectedLevelObjectName);
        }else{
            this.level.addDecoration(x, y, selectedLevelObjectName);
        }
    }

    
    public void saveLevel(boolean overwrite){
        JSONObject levelJSON = level.toJSONObject();

        JsonReader.saveJsonObject(levelJSON, ResourcesPaths.LEVELS_FOLDER + levelName + ".json", overwrite);
    }

    public void deleteLevel(String levelName){
        File levelFile = new File(ResourcesPaths.LEVELS_FOLDER + levelName + ".json");

        if (levelFile.exists()) levelFile.delete();
    }

    public void loadLevel(String levelName){
        this.level = new Level(null, levelName);
    }
}

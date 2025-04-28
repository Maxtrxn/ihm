package src.model.editor;


import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import src.common.JsonReader;
import src.common.ResourcesPaths;
import src.controller.editor.GameEditorController;
import src.model.game.Decoration;
import src.model.game.Enemy;
import src.model.game.Level;
import src.model.game.LevelObject;
import src.model.game.Platform;

import java.io.File;

import org.json.JSONObject;


public class GameEditorModel{
    private final GameEditorController controller;
    private Level level = null;
    private String levelName = null;
    private String selectedLevelObjectName = null;
    private LevelObject clickSelectedLevelObject = null;


    public GameEditorModel(GameEditorController controller){
        this.controller = controller;
        this.controller.setModel(this);
    }

    public void setLevelName(String levelName){this.levelName = levelName;}
    public void setSelectedLevelObjectName(String name){this.selectedLevelObjectName = name;}
    public void initLevel(double levelWidth, double levelHeight){this.level = new Level(levelWidth, levelHeight);}
    public void setLevelBackground(Image bg){this.level.setBackgroundImage(bg);}
    public Level getLevel(){return this.level;}
    public LevelObject getClickSelectedLevelObject(){return this.clickSelectedLevelObject;}


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

    public void addEnemy(double x, double y, double leftBound, double rightBound, double speed){
        this.level.addEnemy(x, y, speed, leftBound, rightBound, this.selectedLevelObjectName);
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


    public void clickSelectLevelObject(double mouseClickPosX, double mouseClickPosY){
        for(LevelObject levelObject : this.level.getLevelObjects()){
            if(mouseClickPosX >= levelObject.getX() && mouseClickPosX <= levelObject.getX() + levelObject.getWidth() &&
            mouseClickPosY >= levelObject.getY() && mouseClickPosY <= levelObject.getY() + levelObject.getHeight()){
                if(this.clickSelectedLevelObject == levelObject){
                    if(levelObject instanceof Platform){
                        this.level.getPlatforms().remove(levelObject);
                    }else if(levelObject instanceof Enemy){
                        this.level.getEnemies().remove(levelObject);
                    }else if(levelObject instanceof Decoration){
                        this.level.getDecorations().remove(levelObject);
                    }
                    this.clickSelectedLevelObject = null;
                }else{
                    this.clickSelectedLevelObject = levelObject;
                }
                return;
            }
        }
        this.clickSelectedLevelObject = null;
    }
}

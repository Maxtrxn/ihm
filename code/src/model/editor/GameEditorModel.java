package src.model.editor;


import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import src.common.JsonReader;
import src.controller.editor.GameEditorController;
import src.model.game.Level;
import org.json.JSONObject;

public class GameEditorModel{
    private final GameEditorController controller;
    private Level level = null;
    private String levelName = null;
    private String selectedPlatformName = null;


    public GameEditorModel(GameEditorController controller){
        this.controller = controller;
        this.controller.setModel(this);
    }

    public void setLevelName(String levelName){
        this.levelName = levelName;
    }

    public void setSelectedPlatformName(String name){
        this.selectedPlatformName = name;
    }

    public void initLevel(double levelWidth, double levelHeight){
        this.level = new Level(levelWidth, levelHeight);
    }

    public void addPlatform(double x, double y){
        this.level.addPlatform(x, y, selectedPlatformName);
    }

    public void setLevelBackground(Image bg){
        this.level.setBackgroundImage(bg);
    }


    public void saveLevel(){
        JSONObject levelJSON = level.toJSONObject();

        JsonReader.saveJsonObject(levelJSON, "levels/" + levelName + ".json");
    }
}

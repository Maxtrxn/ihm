package src.model.game;

import javafx.scene.image.Image;
import src.common.JsonReader;
import src.common.ResourceManager;

import org.json.JSONObject;


public class Decoration extends LevelObject{
    public Decoration(double x, double y, String name) {
        super(x, y, name, ResourceManager.DECORATIONS_JSON, ResourceManager.DECORATIONS_FOLDER);
    }
}

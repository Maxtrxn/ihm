package src.model.game;

import javafx.scene.image.Image;
import src.common.JsonReader;
import src.common.ResourcesPaths;

import org.json.JSONObject;


public class Decoration extends LevelObject{
    private static JSONObject decorationsJson = JsonReader.getJsonObjectContent(ResourcesPaths.RESOURCE_FOLDER + "decorations.json");


    public Decoration(double x, double y, String name) {
        super(x, y, name, decorationsJson, "decorations");
    }
}

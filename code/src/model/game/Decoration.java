package src.model.game;

import javafx.scene.image.Image;
import src.common.JsonReader;
import org.json.JSONObject;


public class Decoration extends LevelObject{
    private static JSONObject decorationsJson = JsonReader.getJsonObjectContent("decorations.json");


    public Decoration(double x, double y, String name) {
        super(x, y, name, decorationsJson, "decorations");
    }
}

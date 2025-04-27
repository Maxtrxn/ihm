package src.model.game;

import javafx.scene.image.Image;
import src.common.JsonReader;
import org.json.JSONObject;

public class Platform extends LevelObject{
    protected static JSONObject platformsJson = JsonReader.getJsonObjectContent("platforms.json");


    public Platform(double x, double y, String name) {
        super(x, y, name, platformsJson, "platforms");
    }
}

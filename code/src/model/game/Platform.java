package src.model.game;

import javafx.scene.image.Image;
import src.common.JsonReader;
import src.common.ResourcesPaths;

import java.util.ResourceBundle;

import org.json.JSONObject;

public class Platform extends LevelObject{
    protected static JSONObject platformsJson = JsonReader.getJsonObjectContent(ResourcesPaths.RESOURCE_FOLDER + "platforms.json");


    public Platform(double x, double y, String name) {
        super(x, y, name, platformsJson, ResourcesPaths.PLATFORMS_FOLDER);
    }
}

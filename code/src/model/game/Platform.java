package src.model.game;

import javafx.scene.image.Image;
import src.common.JsonReader;
import src.common.ResourceManager;

import java.util.ResourceBundle;

import org.json.JSONObject;

public class Platform extends LevelObject{
    public Platform(double x, double y, String name) {
        super(x, y, name, ResourceManager.PLATFORMS_JSON, ResourceManager.PLATFORMS_FOLDER);
    }
}

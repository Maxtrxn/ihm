package src.common;

import org.json.JSONObject;

import javafx.scene.image.Image;

public class ResourceManager {
    public static final String RESOURCE_FOLDER = "../resources/";
    public static final String LEVELS_FOLDER = RESOURCE_FOLDER + "levels/";
    public static final String STYLESHEET_FOLDER = RESOURCE_FOLDER + "css/";
    public static final String TEXTURES_FOLDER = RESOURCE_FOLDER + "textures/";
    public static final String PLATFORMS_FOLDER = TEXTURES_FOLDER + "platforms/";
    public static final String DECORATIONS_FOLDER = TEXTURES_FOLDER + "decorations/";
    public static final String ENEMIES_FOLDER = TEXTURES_FOLDER + "enemies/";
    public static final String BACKGROUNDS_FOLDER = TEXTURES_FOLDER + "backgrounds/";
    

    public static final JSONObject DECORATIONS_JSON = JsonReader.getJsonObjectContent(ResourceManager.RESOURCE_FOLDER + "decorations.json");
    public static final JSONObject ENEMIES_JSON = JsonReader.getJsonObjectContent(ResourceManager.RESOURCE_FOLDER + "enemies.json");
    public static final JSONObject PLATFORMS_JSON = JsonReader.getJsonObjectContent(ResourceManager.RESOURCE_FOLDER + "platforms.json");

    public static final String DEFAULT_TEXTURE = TEXTURES_FOLDER + "default.png";
}

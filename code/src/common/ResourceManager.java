package src.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.json.JSONObject;

import javafx.scene.Scene;
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
    public static final String PROJECTILES_FOLDER = TEXTURES_FOLDER + "misc/";


    public static int resolutionWidth = 1280;
    public static int resolutionHeight = 720;


    public static final JSONObject DECORATIONS_JSON = JsonReader.getJsonObjectContent(ResourceManager.RESOURCE_FOLDER + "decorations.json");
    public static final JSONObject ENEMIES_JSON = JsonReader.getJsonObjectContent(ResourceManager.RESOURCE_FOLDER + "enemies.json");
    public static final JSONObject PLATFORMS_JSON = JsonReader.getJsonObjectContent(ResourceManager.RESOURCE_FOLDER + "platforms.json");

    public static final String DEFAULT_TEXTURE = TEXTURES_FOLDER + "default.png";
    public static final String AUDIO_FOLDER = "../resources/audio/";



    private static String currStyleSheetName = "steampunk.css";

    public static void setCurrStyleSheet(String styleSheetName){
        currStyleSheetName = styleSheetName;
    }

    public static void setCurrStyleSheetToScene(Scene scene){
        scene.getStylesheets().clear();
        scene.getStylesheets().add(STYLESHEET_FOLDER + currStyleSheetName);
    }




    private static Locale currentLocale;
    private static ResourceBundle bundle;

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        loadBundleFromFile(locale);
    }
    public static Locale getLocale() {return currentLocale;}
    public static String getString(String key) {
        if (bundle != null && bundle.containsKey(key)) {
            return bundle.getString(key);
        } else {
            return "!" + key + "!";
        }
    }

    private static void loadBundleFromFile(Locale locale) {
        String langCode = locale.getLanguage();
        String filePath = "../resources/text/text_" + langCode + ".properties";

        try (FileInputStream fis = new FileInputStream(filePath);
             InputStreamReader reader = new InputStreamReader(fis, "UTF-8")) {
            bundle = new PropertyResourceBundle(reader);
        } catch (IOException e) {
            System.err.println("Impossible de charger les fichiers de langue : " + filePath);
            bundle = null;
        }
    }
}

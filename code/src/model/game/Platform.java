package src.model.game;

import javafx.scene.image.Image;
import src.common.JsonReader;

import org.json.JSONObject;

public class Platform {
    protected static JSONObject platformsJson = JsonReader.getJsonObjectContent("platforms.json");
    protected double x, y, width, height;
    protected Image texture;

    public Platform(double x, double y, Image texture) {
        this.x = x;
        this.y = y;
        this.texture = texture;

        // Dimensions initiales basées sur la taille d'origine de la texture
        this.width = texture.getWidth();
        this.height = texture.getHeight();
    }


    public Platform(double x, double y, String platformName) {
        JSONObject platformJson = platformsJson.getJSONObject(platformName);
        this.x = x;
        this.y = y;
        this.texture = new Image("file:../resources/textures/" + platformJson.getString("textureFileName"));

        // Dimensions initiales basées sur la taille d'origine de la texture
        double scaleFactor = platformJson.getDouble("scaleFactor");
        this.width = texture.getWidth() * scaleFactor;
        this.height = texture.getHeight() * scaleFactor;
    }

    public double getX()      { return x; }
    public double getY()      { return y; }
    public double getWidth()  { return width; }
    public double getHeight() { return height; }
    public Image getTexture() { return texture; }
}

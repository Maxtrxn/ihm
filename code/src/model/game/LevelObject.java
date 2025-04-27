package src.model.game;

import javafx.scene.image.Image;
import org.json.JSONObject;

public abstract class LevelObject {
    protected double x, y, width, height;
    protected Image texture;
    protected String name;

    protected LevelObject(double x, double y, String name, JSONObject levelObjectsJson, String levelObjectFolderName) {
        this.x = x;
        this.y = y;

        JSONObject levelObjectJson = levelObjectsJson.getJSONObject(name);
        this.name = name;
        this.texture = new Image("file:../resources/textures/" + levelObjectFolderName + "/" + levelObjectJson.getString("textureFileName"));

        // Dimensions initiales basées sur la taille d'origine de la texture
        double scaleFactor = levelObjectJson.getDouble("scaleFactor");
        this.width = texture.getWidth() * scaleFactor;
        this.height = texture.getHeight() * scaleFactor;
    }

    public double getX()      { return x; }
    public double getY()      { return y; }
    public double getWidth()  { return width; }
    public double getHeight() { return height; }
    public Image getTexture() { return texture; }
    public String getName() { return name; }

    public JSONObject toJSONObject(){
        JSONObject obj = new JSONObject();
        obj.put("name", this.name);
        obj.put("x", this.x);
        obj.put("y", this.y);
        return obj;
    }
}

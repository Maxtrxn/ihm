package src.model.game;

import javafx.scene.image.Image;
import src.common.ResourceManager;

import java.nio.channels.Pipe.SourceChannel;

import org.json.JSONObject;

public abstract class LevelObject {
    protected double x, y, width, height;
    protected Image texture;
    protected String name;
    protected int nbFrame;

    protected LevelObject(double x, double y, String name, JSONObject levelObjectsJson, String pathToLevelObjectFolder) {
        this.x = x;
        this.y = y;
        
        JSONObject levelObjectJson = levelObjectsJson.getJSONObject(name);
        this.name = name;
        double scaleFactor;
        if(name == null){
            this.texture = new Image("file:" + ResourceManager.DEFAULT_TEXTURE);
            scaleFactor = 1;
        }else{
            this.texture = new Image("file:" + pathToLevelObjectFolder + levelObjectJson.getString("textureFileName"));
            scaleFactor = levelObjectJson.getDouble("scaleFactor");
            if (levelObjectJson.has("frames")){
                nbFrame = levelObjectJson.getInt("frames");
            }else{
                nbFrame = 1;
            }
            
        }

        this.width = texture.getWidth() * scaleFactor;
        this.height = texture.getHeight() * scaleFactor;
    }

    public double getX()      { return x; }
    public double getY()      { return y; }
    public double getWidth()  { return width; }
    public double getHeight() { return height; }
    public Image getTexture() { return texture;}
    public String getName() { return name; }
    public int getNbFrame(){return this.nbFrame;}

    public JSONObject toJSONObject(){
        JSONObject obj = new JSONObject();
        obj.put("name", this.name);
        obj.put("x", this.x);
        obj.put("y", this.y);
        
        return obj;
    }
}

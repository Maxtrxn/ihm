// ./src/levels/Level.java
package src.levels;

import src.model.Player;
import src.model.Platform;
import src.model.Enemy;
import src.common.JsonReader;
import src.model.Decoration;

import javafx.scene.image.Image;
import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONArray;

public abstract class Level {
    protected List<Platform> platforms;
    protected List<Enemy> enemies;
    protected List<Decoration> decorations;
    protected Player player;
    protected Image backgroundImage;

    // dimensions logiques du niveau
    protected double levelWidth;
    protected double levelHeight;

    public Level(Player player, String levelName) {
        this.player      = player;
        this.platforms   = new ArrayList<>();
        this.enemies     = new ArrayList<>();
        this.decorations = new ArrayList<>();
        initialize();
    }

    /** À implémenter : remplir platforms, enemies, decorations, background, et dimensions. */
    protected void initialize(){
        /* 
        JsonReader jsonReader = new JsonReader();

        JSONObject levelJson = jsonReader.getJsonObjectContent("/levels/level1.json");

        JSONArray platformsJson = levelJson.getJSONArray("platforms");
        JSONArray ennemiesJson = levelJson.getJSONArray("ennemiesJson");
        String backgroundFilePath = levelJson.getString("backgroundImage");
        this.levelWidth = levelJson.getInt("levelWidth");
        this.levelHeight = levelJson.getInt("levelHeight");
        */

    }

    // getters pour le contrôleur / la vue
    public List<Platform> getPlatforms()   { return platforms;    }
    public List<Enemy>    getEnemies()     { return enemies;      }
    public List<Decoration> getDecorations(){ return decorations; }
    public Image         getBackgroundImage() { return backgroundImage; }
    public double        getLevelWidth()   { return levelWidth;   }
    public double        getLevelHeight()  { return levelHeight;  }

    /** Pour charger l’image de fond. */
    protected void setBackgroundImage(String imagePath) {
        this.backgroundImage = new Image(imagePath);
    }

    /** À appeler dans initialize() pour définir largeur/hauteur logiques. */
    protected void setLevelDimensions(double width, double height) {
        this.levelWidth  = width;
        this.levelHeight = height;
    }
}

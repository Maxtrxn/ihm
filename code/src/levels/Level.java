// src/levels/Level.java
package src.levels;

import javafx.scene.image.Image;
import src.common.JsonReader;
import src.model.Platform;
import src.model.Enemy;
import src.model.Decoration;
import src.model.Player;
import src.model.platforms.FragilePlatform;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.ArrayList;

/**
 * Classe de base pour un niveau, désormais unique :
 * - Si on appelle new Level(player, "level1") => initialise depuis JSON.
 * - Si on appelle new Level(player)      => initialize() vide (mode codé en dur).
 */
public class Level {
    protected List<Platform>   platforms;
    protected List<Enemy>      enemies;
    protected List<Decoration> decorations;
    protected Player           player;
    protected Image            backgroundImage;
    protected double           levelWidth;
    protected double           levelHeight;

    /** Constructeur « code en dur » (si jamais utilisé) */
    public Level(Player player) {
        this.player      = player;
        this.platforms   = new ArrayList<>();
        this.enemies     = new ArrayList<>();
        this.decorations = new ArrayList<>();
        initialize();
    }

    /** Constructeur JSON : charge "levels/{levelName}.json" */
    public Level(Player player, String levelName) {
        this.player      = player;
        this.platforms   = new ArrayList<>();
        this.enemies     = new ArrayList<>();
        this.decorations = new ArrayList<>();
        initialize(levelName);
    }

    /** À surcharger si on veut un niveau codé en dur */
    protected void initialize() {
        // vide par défaut
    }

    /** Initialise depuis JSON */
    protected void initialize(String levelName) {
        JSONObject L = JsonReader.getJsonObjectContent("levels/" + levelName + ".json");

        // 1) background + dimensions
        setBackgroundImage(L.getString("backgroundImage"));
        setLevelDimensions(L.getDouble("levelWidth"), L.getDouble("levelHeight"));

        // 2) plateformes
        JSONArray plats = L.getJSONArray("platforms");
        for (int i = 0; i < plats.length(); i++) {
            JSONObject p = plats.getJSONObject(i);
            String name = p.getString("name");
            double x     = p.getDouble("x");
            double y     = p.getDouble("y");
            if ("fragile".equals(name)) {
                platforms.add(new FragilePlatform(x, y));
            } else {
                // constructeur Platform(x,y,name) lira texture + scaleFactor depuis platforms.json
                platforms.add(new Platform(x, y, name));
            }
        }

        // 3) ennemis
        JSONArray ens = L.getJSONArray("enemies");
        for (int i = 0; i < ens.length(); i++) {
            JSONObject e = ens.getJSONObject(i);
            enemies.add(new Enemy(
                e.getDouble("x"),
                e.getDouble("y"),
                e.getDouble("width"),
                e.getDouble("height"),
                e.getDouble("speed"),
                e.getDouble("patrolStart"),
                e.getDouble("patrolEnd")
            ));
        }

        // 4) décorations (facultatif)
        if (L.has("decorations")) {
            for (Object o : L.getJSONArray("decorations")) {
                JSONObject d = (JSONObject) o;
                Image tex = new Image(d.getString("image"));
                decorations.add(new Decoration(d.getDouble("x"), d.getDouble("y"), tex));
            }
        }
    }

    // ——————————— Getters pour GameController / GameView ———————————

    public List<Platform>   getPlatforms()       { return platforms;    }
    public List<Enemy>      getEnemies()         { return enemies;      }
    public List<Decoration> getDecorations()     { return decorations;  }
    public Image            getBackgroundImage() { return backgroundImage; }
    public double           getLevelWidth()      { return levelWidth;   }
    public double           getLevelHeight()     { return levelHeight;  }

    // ——————————— Helpers ———————————

    protected void setBackgroundImage(String path) {
        this.backgroundImage = new Image(path);
    }

    protected void setLevelDimensions(double w, double h) {
        this.levelWidth  = w;
        this.levelHeight = h;
    }
}

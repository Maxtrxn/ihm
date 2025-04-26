// src/levels/Level.java
package src.levels;

import javafx.scene.image.Image;
import src.common.JsonReader;
import src.model.Platform;
import src.model.Enemy;
import src.model.Boss;
import src.model.Decoration;
import src.model.Player;
import src.model.platforms.FragilePlatform;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.ArrayList;

public class Level {
    protected List<Platform>   platforms;
    protected List<Enemy>      enemies;
    protected List<Decoration> decorations;
    protected Player           player;
    protected Image            backgroundImage;
    protected double           levelWidth;
    protected double           levelHeight;

    /** Constructeur codé en dur */
    public Level(Player player) {
        this.player      = player;
        this.platforms   = new ArrayList<>();
        this.enemies     = new ArrayList<>();
        this.decorations = new ArrayList<>();
        initialize();
    }

    /** Constructeur JSON */
    public Level(Player player, String levelName) {
        this.player      = player;
        this.platforms   = new ArrayList<>();
        this.enemies     = new ArrayList<>();
        this.decorations = new ArrayList<>();
        initialize(levelName);
    }

    /** À surcharger si code en dur */
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
                platforms.add(new Platform(x, y, name));
            }
        }

        // 3) ennemis + boss
        JSONArray ens = L.getJSONArray("enemies");
        for (int i = 0; i < ens.length(); i++) {
            JSONObject e = ens.getJSONObject(i);
            double x           = e.getDouble("x");
            double y           = e.getDouble("y");
            double width       = e.getDouble("width");
            double height      = e.getDouble("height");
            double speed       = e.getDouble("speed");
            double patrolStart = e.getDouble("patrolStart");
            double patrolEnd   = e.getDouble("patrolEnd");
            if (e.optBoolean("boss", false)) {
                enemies.add(new Boss(x, y, width, height, speed, patrolStart, patrolEnd));
            } else {
                enemies.add(new Enemy(x, y, width, height, speed, patrolStart, patrolEnd));
            }
        }

        // 4) décorations
        if (L.has("decorations")) {
            for (Object o : L.getJSONArray("decorations")) {
                JSONObject d = (JSONObject) o;
                Image tex = new Image(d.getString("image"));
                decorations.add(new Decoration(d.getDouble("x"), d.getDouble("y"), tex));
            }
        }
    }

    // ——————————— Getters ———————————

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

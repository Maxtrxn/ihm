// src/levels/Level.java
package src.model.game;

import javafx.scene.image.Image;
import src.common.JsonReader;
import src.common.ResourcesPaths;
import src.model.game.Decoration;
import src.model.game.Enemy;
import src.model.game.Platform;
import src.model.game.Player;
import src.model.game.platforms.FragilePlatform;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class Level {
    // ——— Champs principaux ———
    protected List<Platform>   platforms;
    protected List<Enemy>      enemies;
    protected List<Decoration> decorations;
    protected Player           player;
    protected Image            backgroundImage;
    protected double           levelWidth;
    protected double           levelHeight;

    // ——— Zone de boss (optionnelle) ———
    private double bossZoneStart = Double.NEGATIVE_INFINITY;
    private double bossZoneEnd   = Double.POSITIVE_INFINITY;

    //Constructeur pour initialiser un level pour l'editeur de niveau
    public Level(double levelWidth, double levelHeight) {
        this.platforms   = new ArrayList<>();
        this.enemies     = new ArrayList<>();
        this.decorations = new ArrayList<>();
        this.levelWidth = levelWidth;
        this.levelHeight = levelHeight;
    }

    /** Constructeur JSON : charge "levels/{levelName}.json". */
    public Level(Player player, String levelName) {
        this.player      = player;
        this.platforms   = new ArrayList<>();
        this.enemies     = new ArrayList<>();
        this.decorations = new ArrayList<>();
        initialize(levelName);
    }

    /** Initialise le niveau depuis le JSON correspondant. */
    protected void initialize(String levelName) {
        JSONObject L = JsonReader.getJsonObjectContent(ResourcesPaths.LEVELS_FOLDER + levelName + ".json");
        if (L == null) {
            throw new IllegalStateException("Impossible de charger le JSON pour le niveau : " + levelName);
        }

        // 1) Background + dimensions
        if (L.has("backgroundImageFileName")) {this.backgroundImage = new Image("file:" + ResourcesPaths.BACKGROUNDS_FOLDER + L.getString("backgroundImageFileName"));}
        this.levelWidth  = L.getDouble("levelWidth");
        this.levelHeight = L.getDouble("levelHeight");

        // 2) Plateformes
        JSONArray plats = L.getJSONArray("platforms");
        for (int i = 0; i < plats.length(); i++) {
            JSONObject p = plats.getJSONObject(i);
            String name = p.getString("name");
            double x     = p.getDouble("x");
            double y     = p.getDouble("y");
            String type = p.has("type") ? p.getString("type") : null;
            if ("FragilePlatform".equals(type)) {
                platforms.add(new FragilePlatform(x, y, name));
            } else {
                platforms.add(new Platform(x, y, name));
            }
        }

        // 3) Ennemis + boss
        JSONArray ens = L.getJSONArray("enemies");
        for (int i = 0; i < ens.length(); i++) {
            JSONObject e = ens.getJSONObject(i);
            String name = e.has("name") ? e.getString("name") : "";
            double x           = e.getDouble("x");
            double y           = e.getDouble("y");
            double width       = e.has("width") ? e.getDouble("width") : 0.0;
            double height      = e.has("height") ? e.getDouble("height") : 0.0;
            double speed       = e.getDouble("speed");
            double patrolStart = e.getDouble("patrolStart");
            double patrolEnd   = e.getDouble("patrolEnd");
            boolean isBoss     = e.optBoolean("boss", false);
            if (isBoss) {
                enemies.add(new src.model.game.Boss(x, y, width, height, speed, patrolStart, patrolEnd));
            } else {
                enemies.add(new Enemy(x, y, speed, patrolStart, patrolEnd, name));
            }
        }

        // 4) Décorations (optionnel)
        if (L.has("decorations")) {
            for (Object o : L.getJSONArray("decorations")) {
                JSONObject d = (JSONObject) o;
                decorations.add(new Decoration(d.getDouble("x"), d.getDouble("y"), d.getString("name")));
            }
        }

        // 5) Zone de boss (optionnel)
        if (L.has("bossZone")) {
            JSONObject bz = L.getJSONObject("bossZone");
            bossZoneStart = bz.getDouble("startX");
            bossZoneEnd   = bz.getDouble("endX");
        }
    }

    // ——— Getters ———

    public List<Platform>   getPlatforms()       { return platforms;    }
    public List<Enemy>      getEnemies()         { return enemies;      }
    public List<Decoration> getDecorations()     { return decorations;  }
    public Image            getBackgroundImage() { return backgroundImage; }
    public double           getLevelWidth()      { return levelWidth;   }
    public double           getLevelHeight()     { return levelHeight;  }

    /** Coordonnée X où commence la zone de boss (infinie si non définie). */
    public double getBossZoneStart() { return bossZoneStart; }
    /** Coordonnée X où se termine la zone de boss (infinie si non définie). */
    public double getBossZoneEnd()   { return bossZoneEnd;   }


    // ——————————— Setters ———————————

    public void setBackgroundImage(Image bg) {this.backgroundImage = bg;}
    public void setLevelWidth(double w) {this.levelWidth = w;}
    public void setLevelHeight(double h) {this.levelHeight = h;}
    public void addPlatform(double x, double y, String platformName) {this.platforms.add(new Platform(x, y, platformName));}
    public void addDecoration(double x, double y, String decorationName) {this.decorations.add(new Decoration(x, y, decorationName));}
    public void addEnemy(double x, double y, double speed, double leftBound, double rightBound, String name){this.enemies.add(new Enemy(x, y, speed, leftBound, rightBound, name));}


    // ——————————— Helpers ———————————

    public JSONObject toJSONObject(){
        JSONObject levelJSON = new JSONObject();

        JSONArray platformsJSON = new JSONArray();
        for (Platform platform : platforms) {   
            platformsJSON.put(platform.toJSONObject());
        }
        levelJSON.put("platforms", platformsJSON);

        JSONArray enemiesJSON = new JSONArray();
        for (Enemy enemy : enemies) {   
            enemiesJSON.put(enemy.toJSONObject());
        }
        levelJSON.put("enemies", enemiesJSON);

        JSONArray decorationsJSON = new JSONArray();
        for (Decoration decoration : decorations) {   
            decorationsJSON.put(decoration.toJSONObject());
        }
        levelJSON.put("decorations", decorationsJSON);


        if(this.backgroundImage == null){
            levelJSON.put("backgroundImageFileName", "default.png");
        }else{
            //On vérifie si le fond de niveau existe déjà dans le dossier des fond
            //S'il existe c'est que le niveau avait déjà chargé le fond via le dossier
            //Sinon c'est qu'il a été choisi par l'utilisateur potentiellement dans un
            //autre dossier, donc il faut le copier dans le dossier des fonds pour 
            //pouvoir sauvegarder le nom dans le json et le recharger plus tard
            String backgroundURL = this.backgroundImage.getUrl();
            String backgroundName = backgroundURL.substring(backgroundURL.lastIndexOf('/') + 1);
            File backgroundFile = new File(ResourcesPaths.BACKGROUNDS_FOLDER + backgroundName);
            if(!backgroundFile.exists()){
                try (InputStream in = URI.create(backgroundURL).toURL().openStream();
                    OutputStream out = new FileOutputStream(backgroundFile)) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            levelJSON.put("backgroundImageFileName", backgroundName);
        }


        levelJSON.put("levelWidth", this.levelWidth);
        levelJSON.put("levelHeight", this.levelHeight);

        return levelJSON;
    }
}

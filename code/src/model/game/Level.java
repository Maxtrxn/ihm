// src/levels/Level.java
package src.model.game;

import javafx.scene.image.Image;
import src.common.JsonReader;
import src.common.ResourceManager;
import src.model.game.Decoration;
import src.model.game.Enemy;
import src.model.game.Platform;
import src.model.game.Player;
import src.model.game.platforms.FragilePlatform;
import src.model.game.platforms.SpawnPoint;
import src.controller.editor.GameEditorController.LevelObjectType;

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
import java.util.Arrays;

public class Level {
    // ——— Champs principaux ———
    protected List<Platform>   platforms;
    protected List<Enemy>      enemies;
    protected List<Decoration> decorations;
    protected Player           player;
    protected Image            backgroundImage;
    protected double           levelWidth;
    protected double           levelHeight;
    protected SpawnPoint       spawnPoint;
    private String             musicFileName;

    // ——— Zone de boss (optionnelle) ———
    private double bossZoneStart = Double.NEGATIVE_INFINITY;
    private double bossZoneEnd   = Double.POSITIVE_INFINITY;

    // ——— Mode vaisseau ———
    private boolean spaceshipMode = false;

    //Constructeur pour initialiser un level pour l'editeur de niveau
    public Level(double levelWidth, double levelHeight) {
        this.platforms   = new ArrayList<>();
        this.enemies     = new ArrayList<>();
        this.decorations = new ArrayList<>();
        this.levelWidth = levelWidth;
        this.levelHeight = levelHeight;
        this.spawnPoint = null;
    }

        /**
     * Définit le fichier audio associé à ce niveau.
     *
     * @param fname le nom du fichier (ex. "level1.mp3")
     */
    public void setMusicFileName(String fname) {
        this.musicFileName = fname;
    }


    /** Constructeur JSON : charge "levels/{levelName}.json". */
    public Level(Player player, String levelName) {
        this.player      = player;
        this.platforms   = new ArrayList<>();
        this.enemies     = new ArrayList<>();
        this.decorations = new ArrayList<>();
        initialize(levelName);
        if(this.player != null){
            this.player.setX(this.spawnPoint.getX());
            this.player.setY(this.spawnPoint.getY() - this.player.getHeight());
        }
    }

    /** Initialise le niveau depuis le JSON correspondant. */
    protected void initialize(String levelName) {
        JSONObject L = JsonReader.getJsonObjectContent(ResourceManager.LEVELS_FOLDER + levelName + ".json");
        if (L == null) {
            throw new IllegalStateException("Impossible de charger le JSON pour le niveau : " + levelName);
        }

        // 1) Background + dimensions
        if (L.has("backgroundImageFileName")) {this.backgroundImage = new Image("file:" + ResourceManager.BACKGROUNDS_FOLDER + L.getString("backgroundImageFileName"));}
        this.levelWidth  = L.getDouble("levelWidth");
        this.levelHeight = L.getDouble("levelHeight");
        this.spaceshipMode = L.optBoolean("spaceshipMode", false);

        if (L.has("musicFileName")) {
            this.musicFileName = L.getString("musicFileName");
          } else {
            this.musicFileName = null;  // ou un nom par défaut
          }

        // 2) Plateformes (s’il y en a)
        JSONArray plats = L.optJSONArray("platforms");
        if (plats != null) {
            for (int i = 0; i < plats.length(); i++) {
                JSONObject p = plats.getJSONObject(i);
                String name = p.getString("name");
                double x    = p.getDouble("x");
                double y    = p.getDouble("y");
                JSONObject platformJSON = ResourceManager.PLATFORMS_JSON.getJSONObject(name);
                LevelObjectType type = LevelObjectType.valueOf(platformJSON.getString("type"));
                switch (type) {
                    case FRAGILE_PLATFORM:
                        platforms.add(new FragilePlatform(x, y, name)); break;
                    case PLATFORM:
                        platforms.add(new Platform(x, y, name));      break;
                    case SPAWNPOINT:
                        spawnPoint = new SpawnPoint(x, y, name);      break;
                }
            }
        }

        // 3) Ennemis + boss (s’il y en a)
        JSONArray ens = L.optJSONArray("enemies");
        if (ens != null) {
            for (int i = 0; i < ens.length(); i++) {
                JSONObject e = ens.getJSONObject(i);
                String name       = e.getString("name");
                double x          = e.getDouble("x");
                double y          = e.getDouble("y");
                double speed      = e.getDouble("speed");
                double patrolStart= e.getDouble("patrolStart");
                double patrolEnd  = e.getDouble("patrolEnd");
                JSONObject enemyJSON = ResourceManager.ENEMIES_JSON.getJSONObject(name);
                LevelObjectType type = LevelObjectType.valueOf(enemyJSON.getString("type"));
                if (type == LevelObjectType.BOSS) {
                    enemies.add(new Boss(x, y, speed, patrolStart, patrolEnd, name));
                } else if (type == LevelObjectType.ENEMY) {
                    enemies.add(new Enemy(x, y, speed, patrolStart, patrolEnd, name));
                }
            }
        }

        // 4) Décorations (s’il y en a)
        JSONArray decos = L.optJSONArray("decorations");
        if (decos != null) {
            for (int i = 0; i < decos.length(); i++) {
                JSONObject d = decos.getJSONObject(i);
                double x  = d.getDouble("x");
                double y  = d.getDouble("y");
                String name = d.getString("name");
                JSONObject decorationJSON = ResourceManager.DECORATIONS_JSON.getJSONObject(name);
                LevelObjectType type = LevelObjectType.valueOf(decorationJSON.getString("type"));
                if (type == LevelObjectType.DECORATION) {
                    decorations.add(new Decoration(x, y, name));
                }
            }
        }

        // 5) Zone de boss (optionnel)
        if (L.has("bossZone")) {
            JSONObject bz = L.getJSONObject("bossZone");
            bossZoneStart = bz.getDouble("startX");
            bossZoneEnd   = bz.getDouble("endX");
        }



        if (L.has("spawnPoint")){
            JSONObject sp = L.getJSONObject("spawnPoint");
            double x = sp.getDouble("x");
            double y =  sp.getDouble("y");
            String name = sp.getString("name");
            this.spawnPoint = new SpawnPoint(x, y, name);
        }else{
            this.spawnPoint = new SpawnPoint(0, 0, "Spawnpoint");
        }
    }

    /** Retourne vrai si on a activé le mode vaisseau dans le JSON. */
    public boolean isSpaceshipMode() {
        return spaceshipMode;
    }

    // ——— Getters ———

    public List<Platform>   getPlatforms()       { return platforms;    }
    public List<Enemy>      getEnemies()         { return enemies;      }
    public List<Decoration> getDecorations()     { return decorations;  }
    public Image            getBackgroundImage() { return backgroundImage; }
    public double           getLevelWidth()      { return levelWidth;   }
    public double           getLevelHeight()     { return levelHeight;  }
    public String getMusicFileName()             { return musicFileName; }
    public SpawnPoint getSpawnPoint(){return this.spawnPoint;}
    public List<LevelObject> getLevelObjects() {
        List<LevelObject> levelObjects = new ArrayList<>();
        levelObjects.addAll(this.platforms);
        levelObjects.addAll(this.decorations);
        levelObjects.addAll(this.enemies);
        if(this.spawnPoint != null) levelObjects.add(this.spawnPoint);
        return levelObjects;
    }
      

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
    public void removePlatform(Platform platform){this.platforms.remove(platform);}
    public void removeDecoration(Decoration decoration){this.decorations.remove(decoration);}
    public void removeEnemy(Enemy enemy){this.enemies.remove(enemy);}
    public void setSpawnPoint(SpawnPoint spawnPoint){this.spawnPoint = spawnPoint;}

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
            File backgroundFile = new File(ResourceManager.BACKGROUNDS_FOLDER + backgroundName);
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

        //On ajoute la zone de boss dans le json que si il y a des valeurs spécifique.
        if(this.bossZoneStart != Double.NEGATIVE_INFINITY){
            JSONObject bossZoneJSON = new JSONObject();
            bossZoneJSON.put("startX", this.bossZoneStart);
            bossZoneJSON.put("endX", this.bossZoneEnd);
            levelJSON.put("bossZone", bossZoneJSON);
        }

        if (this.spawnPoint != null){
            JSONObject spawnPointJSON = this.spawnPoint.toJSONObject();
            levelJSON.put("spawnPoint", spawnPointJSON);
        }
        

        return levelJSON;
    }
}

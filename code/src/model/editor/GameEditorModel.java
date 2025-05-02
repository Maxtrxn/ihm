package src.model.editor;


import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import src.common.JsonReader;
import src.common.ResourceManager;
import src.controller.editor.GameEditorController;
import src.model.game.Decoration;
import src.model.game.Enemy;
import src.model.game.Level;
import src.model.game.LevelObject;
import src.model.game.Platform;
import src.model.game.platforms.SpawnPoint;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;

import javax.sound.sampled.LineListener;

import org.json.JSONObject;


public class GameEditorModel{
    private PropertyChangeSupport support;
    private final GameEditorController controller;
    private String levelName;
    private Level level;
    private String selectedLevelObjectName;
    private LevelObject clickSelectedLevelObject;
    private String musicFileName;



    public GameEditorModel(GameEditorController controller){
        this.support = new PropertyChangeSupport(this);
        this.controller = controller;
        this.levelName = null;
        this.level = null;
        this.selectedLevelObjectName = null;
        this.clickSelectedLevelObject = null;
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        support.addPropertyChangeListener(propertyName, listener);
    }


    public void setLevelName(String levelName){
        String oldValue = this.levelName;
        this.levelName = levelName;
        this.support.firePropertyChange("changeLevelName", oldValue, levelName);
    }
    
    public String getSelectedLevelObjectName(){return this.selectedLevelObjectName;}
    public void setSelectedLevelObjectName(String name){
        String oldName = this.selectedLevelObjectName;
        this.selectedLevelObjectName = name;
        this.support.firePropertyChange("changeSelectedLevelObjectName", oldName, this.selectedLevelObjectName);
    }
    public void setClickSelectedLevelObject(LevelObject levelObject){this.clickSelectedLevelObject = levelObject;}
    public LevelObject getClickSelectedLevelObject(){return this.clickSelectedLevelObject;}
    
    public void setSpawnPoint(double x, double y){
        this.level.setSpawnPoint(new SpawnPoint(x, y, selectedLevelObjectName));
        this.support.firePropertyChange("changeLevelData", null, this.level);
    }

    public void setLevelBackground(Image bg){
        this.level.setBackgroundImage(bg);
        this.support.firePropertyChange("changeLevelData", null, this.level);
    }
    public Level getLevel(){return this.level;}
    public String getLevelName(){return this.levelName;}


    public void addPlatform(double x, double y){
        this.level.addPlatform(x, y, selectedLevelObjectName);
        this.support.firePropertyChange("changeLevelData", null, this.level);
    }
    
    public void removeLevelObject(LevelObject levelObject){
        if(levelObject instanceof Platform){
            if(levelObject instanceof SpawnPoint){
                this.level.setSpawnPoint(null);
            }else{
                this.level.removePlatform((Platform)levelObject);
            }
        }else if(levelObject instanceof Decoration){
            this.level.removeDecoration((Decoration)levelObject);
        }else if(levelObject instanceof Enemy){
            this.level.removeEnemy((Enemy)levelObject);
        }else{
            throw new IllegalStateException("Level object has not the right type");
        }

        this.support.firePropertyChange("changeLevelData", null, this.level);
    }

    //Booléen foreground pour savoir si on ajoute la décoration au premier au à l'arrière plan
    public void addDecoration(double x, double y, boolean foreground){
        if(foreground){
            this.level.addDecoration(x, y, selectedLevelObjectName);
        }else{
            this.level.addDecoration(x, y, selectedLevelObjectName);
        }
        this.support.firePropertyChange("changeLevelData", null, this.level);
    }

    public void addEnemy(double x, double y, double leftBound, double rightBound, double speed){
        this.level.addEnemy(x, y, speed, leftBound, rightBound, this.selectedLevelObjectName);
        this.support.firePropertyChange("changeLevelData", null, this.level);
    }
    
    public boolean saveLevel(boolean overwrite){
        JSONObject levelJSON = level.toJSONObject();
        // levelJSON contiendra désormais musicFileName grâce à Level.toJSONObject()
        return JsonReader.saveJsonObject(levelJSON, ResourceManager.LEVELS_FOLDER + levelName + ".json", overwrite);
    }

    public void deleteLevel(String levelName){
        File levelFile = new File(ResourceManager.LEVELS_FOLDER + levelName + ".json");

        if (levelFile.exists()) levelFile.delete();
    }


    //Pour charger un niveau déjà existant
    public void loadLevel(String levelName){
        this.level = new Level(null, levelName);
        this.setLevelName(levelName);
        this.musicFileName = this.level.getMusicFileName();
        this.support.firePropertyChange("initLevel", null, this.level);
    }

    //Pour initialiser un nouveau niveau
    public void initLevel(String levelName, double levelWidth, double levelHeight){
        Level lastLevel = this.level;
        this.musicFileName = null;  // niveau vierge, pas encore de musique
        this.setLevelName(levelName);
        this.level = new Level(levelWidth, levelHeight);
        this.support.firePropertyChange("initLevel", lastLevel, this.level);
    }

        /**
     * Définit le fichier audio associé au niveau dans l’éditeur
     * et met à jour le modèle JSON.
     *
     * @param fname le nom du fichier (ex. "level1.mp3")
     */
    public void setMusicFileName(String fname) {
        // Sauvegarde de l’ancienne valeur (au besoin)
        String old = this.musicFileName;
        // Mise à jour du champ local
        this.musicFileName = fname;
        // Propagation dans l’objet Level pour la sauvegarde JSON
        this.level.setMusicFileName(fname);
        // Notifier la vue d’un changement de données de niveau
        this.support.firePropertyChange("changeLevelData", null, this.level);
    }


    public LevelObject getLevelObjectAt(double x, double y){
        for (LevelObject levelObject : this.level.getLevelObjects()) {
            if(levelObject.getX() == x && levelObject.getY() == y){
                return levelObject;
            }
        } 
        return null;
    }

}

package src.controller.editor;

import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import src.common.ResourceManager;
import src.model.editor.GameEditorModel;
import src.model.game.Level;
import src.model.game.LevelObject;
import src.view.editor.MapEditorView;

public class MapEditorController {
    private GameEditorModel model;
    private MapEditorView view;

    public MapEditorController(GameEditorModel model, Level level){
        this.model = model;
        this.view = new MapEditorView(this, level);

        this.model.addPropertyChangeListener("changeLevelData", e -> this.view.showLevel((Level)e.getNewValue()));
        this.model.addPropertyChangeListener("changeLevelObjectName", e -> this.view.updateSelectedLevelObjectImage((String)e.getNewValue()));
    }

    ScrollPane getMapEditorScrollPane(){
        return this.view.getMainRegion();
    }


    public void addPlatform(double x, double y){this.model.addPlatform(x, y);}
    public void addDecoration(double x, double y, boolean foreground){this.model.addDecoration(x, y, foreground);}
    public void addEnemy(double x, double y, double leftBound, double rightBound, double speed){this.model.addEnemy(x, y, leftBound, rightBound, speed);}
    public LevelObject clickSelectLevelObject(double mouseClickPosX, double mouseClickPosY){return this.model.clickSelectLevelObject(mouseClickPosX, mouseClickPosY);}
    public void handleMouseClick(double cellTopLeftX, double cellTopLeftY){
        if(this.model.getSelectedLevelObjectName() == null){
            //Si il n'y a pas d'objet sélectionné dans le listview
            for (LevelObject levelObject : this.model.getLevel().getLevelObjects()) {
                if(levelObject.getX() == cellTopLeftX && levelObject.getY() == cellTopLeftY){
                    //Si les coordonnées de la case cliquée correspondent avec les coo de l'objet
                    if(this.model.getClickSelectedLevelObject() == levelObject){
                        //Si on a cliqué 2 fois sur un objet, on le supprime
                        this.model.removeLevelObject(levelObject);
                        this.model.setClickSelectedLevelObject(null);
                    }else{
                        //Si c'est la premiere fois qu'on clique sur l'objet on le selectionne
                        this.model.setClickSelectedLevelObject(levelObject);
                        this.view.updateClickSelectedLevelObject(levelObject);
                    }
    
                    return;
                }
            } 

            //Si on a cliqué dans le vide, on désélectionne la plateforme sélectionnée
            this.model.setClickSelectedLevelObject(null);
        }else{
            //Si il y a un objet sélectionné dans le listview
            String selectedLevelObjectName = this.model.getSelectedLevelObjectName();

            
            if(ResourceManager.PLATFORMS_JSON.has(selectedLevelObjectName)){
                this.model.addPlatform(cellTopLeftX, cellTopLeftY);
            }else if(ResourceManager.DECORATIONS_JSON.has(selectedLevelObjectName)){
                this.model.addDecoration(cellTopLeftX, cellTopLeftY, false);
            }else if(ResourceManager.ENEMIES_JSON.has(selectedLevelObjectName)){

            }


        }
    }


}

package src.controller.editor;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.common.ResourceManager;
import src.model.editor.GameEditorModel;
import src.model.game.Level;
import src.model.game.LevelObject;
import src.model.game.Platform;
import src.view.editor.MapEditorView;

public class MapEditorController {
    private GameEditorModel model;
    private MapEditorView view;

    public MapEditorController(GameEditorModel model, Level level){
        this.model = model;
        this.view = new MapEditorView(this, level);

        this.model.addPropertyChangeListener("changeLevelData", e -> {
            this.model.setClickSelectedLevelObject(null);
            this.view.updateClickSelectedLevelObject(null);
            this.view.showLevel((Level)e.getNewValue());
        });
        this.model.addPropertyChangeListener("changeLevelObjectName", e -> this.view.updateSelectedLevelObjectImage((String)e.getNewValue()));
    }

    ScrollPane getMapEditorRegion(){return this.view.getMainRegion();}
    HBox getMapEditorSettingsRegion(){return this.view.getSettingsRegion();}


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
            this.view.updateClickSelectedLevelObject(null);
        }else{
            //Si il y a un objet sélectionné dans le listview
            String selectedLevelObjectName = this.model.getSelectedLevelObjectName();

            for (LevelObject levelObject : this.model.getLevel().getLevelObjects()) {
                if(levelObject.getX() == cellTopLeftX && levelObject.getY() == cellTopLeftY){
                    //Si il y a déjà un level object à l'endroit où on a cliqué, on ne l'ajoute pas,
                    //C'est plus simple pour gérer la selection avec un clique d'un objet placé.
                    return;
                }
            }
            
            if(ResourceManager.PLATFORMS_JSON.has(selectedLevelObjectName)){
                this.model.addPlatform(cellTopLeftX, cellTopLeftY);
            }else if(ResourceManager.DECORATIONS_JSON.has(selectedLevelObjectName)){
                this.model.addDecoration(cellTopLeftX, cellTopLeftY, false);
            }else if(ResourceManager.ENEMIES_JSON.has(selectedLevelObjectName)){
                handleEnemyPlacement(cellTopLeftX, cellTopLeftY);
            }


        }
    }





    public void handleEnemyPlacement(double levelObjectX, double levelObjectY){
        Stage newWindow = new Stage();
        newWindow.initModality(Modality.APPLICATION_MODAL);

        Label leftPatrolDistanceLabel = new Label("Distance de la patrouille vers la gauche (en pixel) :");
        Spinner<Integer> leftPatrolDistanceSelection = new Spinner<>(0, 200, 100, 1);
        leftPatrolDistanceSelection.setEditable(true);

        Label rightPatrolDistanceLabel = new Label("Distance de la patrouille vers la droite (en pixel) :");
        Spinner<Integer> rightPatrolDistanceSelection = new Spinner<>(0, 200, 100, 1);
        rightPatrolDistanceSelection.setEditable(true);

        Label speedLabel = new Label("Vitesse de l'ennemi :");
        Spinner<Integer> speedSelection = new Spinner<>(0, 200, 60, 1);
        speedSelection.setEditable(true);

        HBox buttons = new HBox();
        Button createButton = new Button("Confirmer");
        createButton.setOnAction(e -> {
            double leftBound = leftPatrolDistanceSelection.getValue();
            double rightBound = rightPatrolDistanceSelection.getValue();
            double speed = speedSelection.getValue();
            this.model.addEnemy(levelObjectX, levelObjectY, levelObjectX - leftBound, levelObjectX + rightBound, speed);
            newWindow.close();
        });
        Button cancelButton = new Button("Annuler");
        cancelButton.setOnAction(e -> {
            newWindow.close();
        });
        buttons.getChildren().addAll(createButton, cancelButton);
        buttons.setStyle("-fx-padding: 10; -fx-alignment: center;");


        VBox layout = new VBox(10, leftPatrolDistanceLabel, leftPatrolDistanceSelection, rightPatrolDistanceLabel,
        rightPatrolDistanceSelection, speedLabel, speedSelection, buttons);
        layout.setStyle("-fx-padding: 10; -fx-alignment: center;");

        Scene scene = new Scene(layout, 300, 400);
        newWindow.setScene(scene);
        newWindow.setTitle("Paramètres de l'ennemi");
        newWindow.showAndWait();
    }


}

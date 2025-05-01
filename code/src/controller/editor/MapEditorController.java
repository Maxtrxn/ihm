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
import src.controller.editor.GameEditorController.LevelObjectType;
import src.model.editor.GameEditorModel;
import src.model.game.Level;
import src.model.game.LevelObject;
import src.model.game.Platform;
import src.model.game.platforms.SpawnPoint;
import src.view.editor.MapEditorView;

public class MapEditorController {
    private GameEditorModel model;
    private MapEditorView view;
    private Stage stage;

    public MapEditorController(GameEditorModel model, Level level, Stage stage){
        this.model = model;
        this.view = new MapEditorView(this, level);
        this.stage = stage;

        this.model.addPropertyChangeListener("changeLevelData", e -> {
            this.model.setClickSelectedLevelObject(null);
            this.view.updateClickSelectedLevelObject(null);
            this.view.showLevel((Level)e.getNewValue());
        });
        this.model.addPropertyChangeListener("changeSelectedLevelObjectName", e -> {
            model.setClickSelectedLevelObject(null);
            view.updateClickSelectedLevelObject(null);
            this.view.updateSelectedLevelObjectImage((String)e.getNewValue());
        });

    }

    ScrollPane getMapEditorRegion(){return this.view.getMainRegion();}
    HBox getMapEditorSettingsRegion(){return this.view.getSettingsRegion();}


    public void addPlatform(double x, double y){this.model.addPlatform(x, y);}
    public void addDecoration(double x, double y, boolean foreground){this.model.addDecoration(x, y, foreground);}
    public void addEnemy(double x, double y, double leftBound, double rightBound, double speed){this.model.addEnemy(x, y, leftBound, rightBound, speed);}


    public void handleMouseClick(double cellTopLeftX, double cellTopLeftY){
        if(this.model.getSelectedLevelObjectName() == null){
            //Si il n'y a pas d'objet sélectionné dans le listview
            handleObjectSelectionOrDeletion(cellTopLeftX, cellTopLeftY);
        }else{
            //Si il y a un objet sélectionné dans le listview
            handleObjectPlacement(cellTopLeftX, cellTopLeftY);
        }
    }



    private void handleObjectSelectionOrDeletion(double x, double y) {
        LevelObject onPos = model.getLevelObjectAt(x, y);
    
        LevelObject newSelection = null;

        if (onPos == null) {
            //Clic dans le vide -> désélection
        } else if (onPos == model.getClickSelectedLevelObject()) {
            //Deucième clic sur un objet -> suppression
            model.removeLevelObject(onPos);
        } else {
            //Sélection d'un nouvel objet
            newSelection = onPos;
        }

        model.setClickSelectedLevelObject(newSelection);
        view.updateClickSelectedLevelObject(newSelection);
    }



    private void handleObjectPlacement(double x, double y) {
        //Si il y a déjà un level object à l'endroit où on a cliqué, on ne l'ajoute pas,
        //C'est plus simple pour gérer la selection avec un clique d'un objet placé.
        if(model.getLevelObjectAt(x, y) != null) return;
    
        String name = model.getSelectedLevelObjectName();
    
        if(ResourceManager.PLATFORMS_JSON.has(name)){
            LevelObjectType type = LevelObjectType.valueOf(ResourceManager.PLATFORMS_JSON.getJSONObject(name).getString("type"));
            switch (type) {
                case SPAWNPOINT -> model.setSpawnPoint(x, y);
                case FRAGILE_PLATFORM, PLATFORM -> model.addPlatform(x, y);
            }
        }else if(ResourceManager.DECORATIONS_JSON.has(name)){
            model.addDecoration(x, y, false);
        }else if(ResourceManager.ENEMIES_JSON.has(name)){
            handleEnemyPlacement(x, y);
        }
    }




    private void handleEnemyPlacement(double levelObjectX, double levelObjectY){
        Stage newWindow = new Stage();
        newWindow.initModality(Modality.APPLICATION_MODAL);

        Label leftPatrolDistanceLabel = new Label(ResourceManager.getString("handleEnemyPlacement_leftPatrolDistanceLabel"));
        Spinner<Integer> leftPatrolDistanceSelection = new Spinner<>(0, 200, 100, 1);
        leftPatrolDistanceSelection.setEditable(true);

        Label rightPatrolDistanceLabel = new Label(ResourceManager.getString("handleEnemyPlacement_rightPatrolDistanceLabel"));
        Spinner<Integer> rightPatrolDistanceSelection = new Spinner<>(0, 200, 100, 1);
        rightPatrolDistanceSelection.setEditable(true);

        Label speedLabel = new Label(ResourceManager.getString("handleEnemyPlacement_speedLabel"));
        Spinner<Integer> speedSelection = new Spinner<>(0, 200, 60, 1);
        speedSelection.setEditable(true);

        HBox buttons = new HBox();
        Button createButton = new Button(ResourceManager.getString("confirm_button"));
        createButton.setOnAction(e -> {
            double leftBound = leftPatrolDistanceSelection.getValue();
            double rightBound = rightPatrolDistanceSelection.getValue();
            double speed = speedSelection.getValue();
            this.model.addEnemy(levelObjectX, levelObjectY, levelObjectX - leftBound, levelObjectX + rightBound, speed);
            newWindow.close();
        });
        Button cancelButton = new Button(ResourceManager.getString("cancel_button"));
        cancelButton.setOnAction(e -> {
            newWindow.close();
        });
        buttons.getChildren().addAll(createButton, cancelButton);
        buttons.setStyle("-fx-padding: 10; -fx-alignment: center;");


        VBox layout = new VBox(10, leftPatrolDistanceLabel, leftPatrolDistanceSelection, rightPatrolDistanceLabel,
        rightPatrolDistanceSelection, speedLabel, speedSelection, buttons);
        layout.setStyle("-fx-padding: 10; -fx-alignment: center;");

        Scene scene = new Scene(layout, 300, 400);
        scene.getStylesheets().add(this.stage.getScene().getStylesheets().getFirst());
        newWindow.setScene(scene);
        newWindow.setTitle(ResourceManager.getString("handleEnemyPlacement_window_title"));
        newWindow.showAndWait();
    }


}

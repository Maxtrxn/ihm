package src.view.editor.gameEditorSubView;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import src.common.JsonReader;
import src.view.editor.GameEditorView;
import javafx.scene.image.Image;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;

import src.view.editor.GameEditorView.LevelObjectType;

import org.json.JSONObject;


public class EditorLevelObjectSelector extends VBox{
    private GameEditorView parent;
    
    public EditorLevelObjectSelector(GameEditorView parent){
        super();
        this.parent = parent;
        initializePlatformSelector();
        
    }


    public void initializePlatformSelector(){
        ListView<LevelObjectSelectorItem> platformSelector = new ListView<>();
        // Gestion de la sélection
        platformSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if(newValue == null || newValue.getTexture() == null){
                parent.updateSelectedLevelObject(null, null, null);
            }else{
                parent.updateSelectedLevelObject(newValue.getLevelObjectNameLabel().getText(), newValue.getLevelObjectType(), new ImageView(newValue.getTexture()));
            }
        });

        //Chargement des plateformes dans le ListView
        JSONObject platformsObjects = JsonReader.getJsonObjectContent("platforms.json");
        platformSelector.getItems().add(new LevelObjectSelectorItem("Plateformes"));
        Set<String> keys = platformsObjects.keySet();
        for (String name : keys) {
            LevelObjectSelectorItem temp = new LevelObjectSelectorItem(name, platformsObjects, LevelObjectType.PLATFORM);
            platformSelector.getItems().add(temp);
        }

        //Chargement des decorations dans le ListView
        JSONObject decorationsObjects = JsonReader.getJsonObjectContent("decorations.json");
        platformSelector.getItems().add(new LevelObjectSelectorItem("Décorations"));
        keys.clear();
        keys = decorationsObjects.keySet();
        for (String name : keys) {
            LevelObjectSelectorItem temp = new LevelObjectSelectorItem(name, decorationsObjects, LevelObjectType.DECORATION);
            platformSelector.getItems().add(temp);
        }
        
        //Chargement des ennemis dans le ListView
        platformSelector.getItems().add(new LevelObjectSelectorItem("Ennemis"));


        platformSelector.setPrefWidth(200);

        Button retractButton = new Button("<");
        //Gestion de la rétraction du sélecteur de plateformes
        retractButton.setOnAction(e -> {
            if (platformSelector.isVisible()) {
                this.setPrefWidth(retractButton.getWidth());
                platformSelector.setVisible(false);
                retractButton.setText(">");
            } else{
                this.setPrefWidth(platformSelector.getPrefWidth());
                platformSelector.setVisible(true);
                retractButton.setText("<");
            }
        });
        Button unselectButton = new Button("Désélectioner");
        //Gestion de la déselection de plateforme dans le sélecteur
        unselectButton.setOnAction(e -> {platformSelector.getSelectionModel().clearSelection();});
        this.getChildren().addAll(retractButton, platformSelector, unselectButton);
    }
}


class LevelObjectSelectorItem extends VBox{
    private Label levelObjectNameLabel;
    private Image texture;
    private LevelObjectType levelObjectType;


    //Pour faire un séparateur
    public LevelObjectSelectorItem(String levelObjectName){
        super();

        this.levelObjectNameLabel = new Label(levelObjectName);
        this.levelObjectNameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        this.getChildren().add(this.levelObjectNameLabel);
        this.setStyle("-fx-background-color: black; -fx-pref-height: 2px;");
        this.texture = null;
    }

    //Pour faire un élément 
    public LevelObjectSelectorItem(String levelObjectName, JSONObject objects, LevelObjectType levelObjectType){
        super();

        JSONObject obj = objects.getJSONObject(levelObjectName);
        String levelObjectTextureFileName = obj.getString("textureFileName");

        switch (levelObjectType) {
            case GameEditorView.LevelObjectType.PLATFORM:
                this.texture = new Image("file:../resources/textures/platforms/" + levelObjectTextureFileName);
                break;
            case GameEditorView.LevelObjectType.DECORATION:
                this.texture = new Image("file:../resources/textures/decorations/" + levelObjectTextureFileName);
                break;
            case GameEditorView.LevelObjectType.ENEMY:
                this.texture = new Image("file:../resources/textures/enemies/" + levelObjectTextureFileName);
                break;
            default:
                break;
        }
        
        ImageView texturePreview = new ImageView(this.texture);
        this.levelObjectNameLabel = new Label(levelObjectName);
        texturePreview.setFitWidth(this.texture.getWidth() / 4);
        texturePreview.setFitHeight(this.texture.getHeight() / 4);
        this.getChildren().addAll(this.levelObjectNameLabel, texturePreview);
        this.levelObjectType = levelObjectType;
    }

    public Image getTexture(){return this.texture;}
    public Label getLevelObjectNameLabel(){return this.levelObjectNameLabel;}
    public LevelObjectType getLevelObjectType(){return this.levelObjectType;}
}
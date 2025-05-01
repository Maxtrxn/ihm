package src.view.editor;

import java.util.Set;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import src.common.JsonReader;
import src.common.ResourceManager;
import src.controller.editor.EditorLevelObjectSelectorController;
import src.controller.editor.GameEditorController.LevelObjectType;
import org.json.JSONObject;

public class EditorLevelObjectSelectorView {
    private EditorLevelObjectSelectorController controller;
    private VBox region;

    public EditorLevelObjectSelectorView(EditorLevelObjectSelectorController controller){
        this.controller = controller;
        this.region = new VBox();
        this.region.getStyleClass().add("main-region");

        initializeLevelObjectSelector();
    }


    public VBox getRegion(){return this.region;}


    public void initializeLevelObjectSelector(){
        ListView<LevelObjectSelectorItem> levelObjectSelector = new ListView<>();
        //Gestion de la sélection
        levelObjectSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if(newValue == null){
                this.controller.handleSelectedLevelObjectChange(null);
            }else{
                this.controller.handleSelectedLevelObjectChange(newValue.getLevelObjectNameLabel().getText());
            }
        });

        //Chargement des plateformes dans le ListView
        JSONObject platformsObjects = JsonReader.getJsonObjectContent(ResourceManager.RESOURCE_FOLDER + "platforms.json");
        levelObjectSelector.getItems().add(new LevelObjectSelectorItem(ResourceManager.getString("EditorLevelObjectSelectorView_separator_platforms")));
        Set<String> keys = platformsObjects.keySet();
        for (String name : keys) {
            JSONObject platform = platformsObjects.getJSONObject(name);
            String typeStr = platform.getString("type");
            LevelObjectType type = LevelObjectType.valueOf(typeStr);
            LevelObjectSelectorItem temp = new LevelObjectSelectorItem(name, platformsObjects, type);
            levelObjectSelector.getItems().add(temp);
        }

        //Chargement des decorations dans le ListView
        JSONObject decorationsObjects = JsonReader.getJsonObjectContent(ResourceManager.RESOURCE_FOLDER + "decorations.json");
        levelObjectSelector.getItems().add(new LevelObjectSelectorItem(ResourceManager.getString("EditorLevelObjectSelectorView_separator_decorations")));
        keys.clear();
        keys = decorationsObjects.keySet();
        for (String name : keys) {
            JSONObject decoration = decorationsObjects.getJSONObject(name);
            String typeStr = decoration.getString("type");
            LevelObjectType type = LevelObjectType.valueOf(typeStr);
            LevelObjectSelectorItem temp = new LevelObjectSelectorItem(name, decorationsObjects, type);
            levelObjectSelector.getItems().add(temp);
        }
        
        //Chargement des ennemis dans le ListView
        JSONObject enemiesObjects = JsonReader.getJsonObjectContent(ResourceManager.RESOURCE_FOLDER + "enemies.json");
        levelObjectSelector.getItems().add(new LevelObjectSelectorItem(ResourceManager.getString("EditorLevelObjectSelectorView_separator_enemies")));
        keys.clear();
        keys = enemiesObjects.keySet();
        for (String name : keys) {
            JSONObject enemy = enemiesObjects.getJSONObject(name);
            String typeStr = enemy.getString("type");
            LevelObjectType type = LevelObjectType.valueOf(typeStr);
            LevelObjectSelectorItem temp = new LevelObjectSelectorItem(name, enemiesObjects, type);
            levelObjectSelector.getItems().add(temp);
        }


        levelObjectSelector.setPrefWidth(200);

        Button retractButton = new Button("<");
        //Gestion de la rétraction du sélecteur de plateformes
        retractButton.setOnAction(e -> {
            if (levelObjectSelector.isVisible()) {
                this.region.setPrefWidth(retractButton.getWidth());
                levelObjectSelector.setVisible(false);
                retractButton.setText(">");
            } else{
                this.region.setPrefWidth(levelObjectSelector.getPrefWidth());
                levelObjectSelector.setVisible(true);
                retractButton.setText("<");
            }
        });
        Button unselectButton = new Button(ResourceManager.getString("unselect_button"));
        //Gestion de la déselection de plateforme dans le sélecteur
        unselectButton.setOnAction(e -> {levelObjectSelector.getSelectionModel().clearSelection();});
        this.region.getChildren().addAll(retractButton, levelObjectSelector, unselectButton);
    }
}


class LevelObjectSelectorItem extends VBox{
    private Label levelObjectNameLabel;
    private Image texture;
    private double textureScaleFactor;
    private LevelObjectType levelObjectType;


    //Pour faire un séparateur
    public LevelObjectSelectorItem(String levelObjectName){
        super();

        this.levelObjectNameLabel = new Label("    "+ levelObjectName+ "    ");
        this.levelObjectNameLabel.getStyleClass().add("sp-separator");
        this.getChildren().add(this.levelObjectNameLabel);
        this.texture = null;
    }

    //Pour faire un élément 
    public LevelObjectSelectorItem(String levelObjectName, JSONObject objects, LevelObjectType levelObjectType){
        super();

        JSONObject obj = objects.getJSONObject(levelObjectName);
        String levelObjectTextureFileName = obj.getString("textureFileName");

        switch (levelObjectType) {
            case LevelObjectType.FRAGILE_PLATFORM:
            case LevelObjectType.SPAWNPOINT:
            case LevelObjectType.PLATFORM:
                this.texture = new Image("file:" + ResourceManager.PLATFORMS_FOLDER + levelObjectTextureFileName);
                break;
            case LevelObjectType.DECORATION:
                this.texture = new Image("file:" + ResourceManager.DECORATIONS_FOLDER + levelObjectTextureFileName);
                break;
            case LevelObjectType.BOSS:
            case LevelObjectType.ENEMY:
                this.texture = new Image("file:" + ResourceManager.ENEMIES_FOLDER + levelObjectTextureFileName);
                break;
            default:
                break;
        }

        this.textureScaleFactor = obj.getDouble("scaleFactor");
        
        ImageView texturePreview = new ImageView(this.texture);
        this.levelObjectNameLabel = new Label(levelObjectName);
        texturePreview.setFitWidth(this.texture.getWidth() / 4);
        texturePreview.setFitHeight(this.texture.getHeight() / 4);
        this.getChildren().addAll(this.levelObjectNameLabel, texturePreview);
        this.levelObjectType = levelObjectType;
    }

    public Image getTexture(){return this.texture;}
    public double getTextureScaleFactor(){return this.textureScaleFactor;};
    public Label getLevelObjectNameLabel(){return this.levelObjectNameLabel;}
    public LevelObjectType getLevelObjectType(){return this.levelObjectType;}
}

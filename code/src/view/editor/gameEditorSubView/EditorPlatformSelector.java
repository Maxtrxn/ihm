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

import org.json.JSONObject;


public class EditorPlatformSelector extends VBox{
    private GameEditorView parent;
    
    public EditorPlatformSelector(GameEditorView parent){
        super();
        this.parent = parent;
        initializePlatformSelector();
        
    }


    public void initializePlatformSelector(){
        ListView<PlatformSelectorItem> platformSelector = new ListView<>();
        JSONObject platformsObjects = JsonReader.getJsonObjectContent("platforms.json");
        Set<String> keys = platformsObjects.keySet();
        for (String string : keys) {
            PlatformSelectorItem temp = new PlatformSelectorItem(string);
            platformSelector.getItems().add(temp);
        }
        platformSelector.setPrefWidth(200);

        Button retractButton = new Button("<");
        Button unselectButton = new Button("Désélectioner");

        this.getChildren().addAll(retractButton, platformSelector, unselectButton);




        unselectButton.setOnAction(e -> {
            platformSelector.getSelectionModel().clearSelection();
        });
        
        // Gestion de la sélection
        platformSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if(newValue == null){
                parent.updateSelectedPlatform(null, null);
            }else{
                parent.updateSelectedPlatform(newValue.getplatformNameLabel().getText(), new ImageView(newValue.getTexture()));
            }
        });
        


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
    }
}


class PlatformSelectorItem extends VBox{
    private static JSONObject platformObjects = JsonReader.getJsonObjectContent("platforms.json");

    private Label platformNameLabel;
    private Image texture;
    private int width;
    private int height;

    public PlatformSelectorItem(String platformName){
        super();

        JSONObject platformObject = PlatformSelectorItem.platformObjects.getJSONObject(platformName);

        String platformTexturePath = platformObject.getString("textureFileName");


        this.texture = new Image("file:../resources/textures/platforms/" + platformTexturePath);

        ImageView texturePreview = new ImageView(this.texture);


        
        this.platformNameLabel = new Label(platformName);

        texturePreview.setFitWidth(this.texture.getWidth() / 4);
        texturePreview.setFitHeight(this.texture.getHeight() / 4);
        this.getChildren().addAll(this.platformNameLabel, texturePreview);
    }

    public Image getTexture(){
        return this.texture;
    }

    public Label getplatformNameLabel(){
        return this.platformNameLabel;
    }
}
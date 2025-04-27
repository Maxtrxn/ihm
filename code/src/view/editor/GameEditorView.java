package src.view.editor;

import src.view.editor.gameEditorSubView.EditorMenuBar;
import src.view.editor.gameEditorSubView.EditorPlatformSelector;
import src.view.editor.gameEditorSubView.MapEditor;
import src.view.editor.gameEditorSubView.MapEditorSettings;
import src.controller.editor.GameEditorController;

import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;



public class GameEditorView extends BorderPane{
    private final GameEditorController controller;
    private MapEditor center = null;
    private EditorMenuBar top = null;
    private EditorPlatformSelector left = null;
    private MapEditorSettings bottom = null;


    public GameEditorView(GameEditorController controller, Stage stage) {
        super();
        this.controller = controller;
        this.controller.setView(this);
        

        //La racine est un BorderPane qui contient :
        // -Un menu en haut pour diverses fonctionnalités
        // -La selection des plateformes à gauche grâce à un ListView (A VOIR)------------------------------------------------------------------------
        // -L'affichage du niveau dans l'éditeur au centre grâce à un ScrollPane

        
        this.top = new EditorMenuBar(this);
        this.setTop(top);
        this.left = new EditorPlatformSelector(this);
        this.setLeft(left);
        Scene scene = new Scene(this);
        scene.getStylesheets().add(getClass().getResource("/css/editorStyle.css").toString());
        stage.setScene(scene); 
        stage.setMaximized(true);
    }

    public GameEditorController getController() {return this.controller;}

    
    public void initLevel(String levelName, int cellSize, int nbRows, int nbCols){
        this.center = new MapEditor(cellSize, nbRows, nbCols, this);
        this.setCenter(center);
        this.bottom = new MapEditorSettings(this);
        this.setBottom(bottom);

        this.controller.initLevel(levelName, nbCols*cellSize, nbRows*cellSize);
    }


    public void updateBackground(Image image){
        if(this.center != null){
            this.center.setMapBackground(image);
            this.controller.updateBackground(image);
        }
    }

    public void updateSelectedPlatform(String platformName, ImageView selectedPlatformImage){
        if(this.center != null){
            this.center.setSelectedPlatformImage(selectedPlatformImage);
            this.controller.updateSelectedPlatform(platformName);
        }
    }

    public void updateVisibleLayer(int visibleLayer){
        if(this.center != null){
            this.center.showOneLayer(visibleLayer);
        }
    }


}

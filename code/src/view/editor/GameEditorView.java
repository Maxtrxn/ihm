package src.view.editor;

import src.view.editor.gameEditorSubView.EditorMenuBar;
import src.view.editor.gameEditorSubView.EditorLevelObjectSelector;
import src.view.editor.gameEditorSubView.MapEditor;
import src.view.editor.gameEditorSubView.MapEditorSettings;
import src.controller.editor.GameEditorController;
import src.model.game.Level;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import src.controller.editor.GameEditorController.LevelObjectType;



public class GameEditorView extends BorderPane{
    private final GameEditorController controller;
    private MapEditor center = null;
    private EditorMenuBar top = null;
    private EditorLevelObjectSelector left = null;
    private MapEditorSettings bottom = null;
    private Stage stage;


    public GameEditorView(GameEditorController controller, Stage stage) {
        super();
        this.controller = controller;
        this.controller.setView(this);
        this.stage = stage;
        

        //La racine est un BorderPane qui contient :
        // -Un menu en haut pour diverses fonctionnalités
        // -La selection des plateformes à gauche grâce à un ListView (A VOIR)------------------------------------------------------------------------
        // -L'affichage du niveau dans l'éditeur au centre grâce à un ScrollPane

        
        this.top = new EditorMenuBar(this);
        this.setTop(top);
        this.left = new EditorLevelObjectSelector(this);
        this.setLeft(left);
        Scene scene = new Scene(this);
        scene.getStylesheets().add(getClass().getResource("/css/editorStyle.css").toString());
        stage.setScene(scene); 
        stage.setMaximized(true);
        updateLevelName("");
    }

    public GameEditorController getController() {return this.controller;}
    public Stage getStage() {return this.stage;}
    
    public void initLevel(String levelName, int cellSize, int nbRows, int nbCols){
        this.updateLevelName(levelName);
        //this.controller.initLevel(nbCols*cellSize, nbRows*cellSize);
        this.center = new MapEditor(cellSize, nbRows, nbCols, this);
        this.setCenter(center);
        this.bottom = new MapEditorSettings(this);
        this.setBottom(bottom);
    }

    
    public void initLevel(Level level, String levelName){
        this.updateLevelName(levelName);
        this.center = new MapEditor(level, this);
        this.setCenter(center);
        this.bottom = new MapEditorSettings(this);
        this.setBottom(bottom);
    }


    public void updateLevelName(String levelName){
        this.controller.updateLevelName(levelName);
        this.stage.setTitle("Steampunk Adventure - Éditeur de niveau" + " - " + levelName);
    }


    public void updateBackground(Image image){
        if(this.center != null){
            this.controller.updateBackground(image);
            this.center.showLevel();
        }
    }

    public void updateSelectedLevelObject(String name, LevelObjectType levelObjectType, ImageView selectedLevelObjectImage){
        if(this.center != null){
            this.center.setSelectedLevelObjectImage(selectedLevelObjectImage, levelObjectType);
            this.controller.updateSelectedLevelObjectName(name);
        }
    }

    public void updateVisibleLayer(int visibleLayer){
        if(this.center != null){
            this.center.showOneLayer(visibleLayer);
        }
    }


}

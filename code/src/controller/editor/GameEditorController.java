package src.controller.editor;


import java.beans.PropertyChangeEvent;


import javafx.stage.Stage;
import src.common.ResourceManager;
import src.model.editor.GameEditorModel;
import src.model.game.Level;
import src.view.editor.GameEditorView;


public class GameEditorController{
    public enum LevelObjectType {
        PLATFORM, FRAGILE_PLATFORM, DECORATION, ENEMY, BOSS, SPAWNPOINT;
    }
    private GameEditorModel model = null;
    private GameEditorView view = null;
    private Stage stage;
    

    public GameEditorController(Stage stage) {
        this.model = new GameEditorModel(this);
        this.view = new GameEditorView(this, stage);
        this.stage = stage;

        EditorMenuBarController menuBarMVC = new EditorMenuBarController(this.model, stage);
        this.view.setTop(menuBarMVC.getEditorMenuBarView());


        this.model.addPropertyChangeListener("changeLevelName", e -> {stage.setTitle("Steampunk Adventure - "+ ResourceManager.getString("editor_title_name") + " - " + e.getNewValue());});
        this.model.addPropertyChangeListener("initLevel", e -> handleInitLevel(e));
    }

    public GameEditorModel getModel(){return this.model;}
    public GameEditorView getView(){return this.view;}


    public void handleInitLevel(PropertyChangeEvent e){
        MapEditorController mapEditorMVC = new MapEditorController(model, (Level)e.getNewValue(), this.stage);

        this.view.setCenter(mapEditorMVC.getMapEditorRegion());
        this.view.setBottom(mapEditorMVC.getMapEditorSettingsRegion());

        EditorLevelObjectSelectorController levelObjectSelectorMVC = new EditorLevelObjectSelectorController(model);
        this.view.setLeft(levelObjectSelectorMVC.getRegion());
    }
}
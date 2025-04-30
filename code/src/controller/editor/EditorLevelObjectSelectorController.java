package src.controller.editor;

import javafx.scene.layout.VBox;
import src.model.editor.GameEditorModel;
import src.view.editor.EditorLevelObjectSelectorView;

public class EditorLevelObjectSelectorController {
    private GameEditorModel model;
    private EditorLevelObjectSelectorView view;


    public EditorLevelObjectSelectorController(GameEditorModel model){
        this.model = model;
        this.view = new EditorLevelObjectSelectorView(this);
    }
    

    public VBox getRegion(){return this.view.getRegion();}

    public void handleSelectedLevelObjectChange(String levelObjectName){
        this.model.setSelectedLevelObjectName(levelObjectName);
    }
}

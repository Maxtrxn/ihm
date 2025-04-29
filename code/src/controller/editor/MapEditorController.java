package src.controller.editor;

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
    }

    MapEditorView getMapEditorScrollPane(){
        return this.view;
    }


    public void addPlatform(double x, double y){this.model.addPlatform(x, y);}
    public void addDecoration(double x, double y, boolean foreground){this.model.addDecoration(x, y, foreground);}
    public void addEnemy(double x, double y, double leftBound, double rightBound, double speed){this.model.addEnemy(x, y, leftBound, rightBound, speed);}
    public LevelObject clickSelectLevelObject(double mouseClickPosX, double mouseClickPosY){return this.model.clickSelectLevelObject(mouseClickPosX, mouseClickPosY);}


}

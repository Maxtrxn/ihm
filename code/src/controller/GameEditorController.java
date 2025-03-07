package src.controller;


import src.view.GameEditorView;
import src.model.GameEditor;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class GameEditorController{
    public int rows;
    public int cols;
    public int cell_size;
    private GameEditor model = null;
    private GameEditorView view = null;
    


    public GameEditorController(int rows, int cols, int cell_size) {
        this.rows = rows;
        this.cols = cols;
        this.cell_size = cell_size;

        this.model = new GameEditor(this);
        this.view = new GameEditorView(this);
    }

    public void setModel(GameEditor model){
        this.model = model;
    }

    public void setView(GameEditorView view){
        this.view = view;
    }

    public GameEditor getModel(){
        return this.model;
    }

    public GameEditorView getView(){
        return this.view;
    }


}
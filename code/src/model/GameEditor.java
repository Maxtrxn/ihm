package src.model;


import javafx.scene.paint.Color;
import src.controller.GameEditorController;


public class GameEditor{
    private final GameEditorController controller;
    private final int rows;
    private final int cols;
    private final Color[][] colors;

    public GameEditor(GameEditorController controller){
        this.controller = controller;
        this.controller.setModel(this);

        this.rows = controller.rows;
        this.cols = controller.cols;
        colors = new Color[rows][cols];
        resetColors();
    }

    public void resetColors() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                colors[i][j] = Color.WHITE;
            }
        }
    }

    public void setColor(int row, int col, Color color) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            colors[row][col] = color;
        }
    }

    public Color getColor(int row, int col) {
        return colors[row][col];
    }
}
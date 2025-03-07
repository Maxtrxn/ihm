package src.view;


import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;

import org.w3c.dom.events.MouseEvent;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import src.controller.GameEditorController;
import src.model.GameEditor;


public class GameEditorView {
    private final GameEditorController controller;
    private final BorderPane root;
    private final ScrollPane scrollPane;
    private final GridPane gridPane;
    private final Rectangle[][] rectangles;
    private final Color defaultColor = Color.rgb(255, 255, 255, 1); //Couleur de base d'une case : blanc pour l'instant mais transparent après
    private final Color hoverColor = Color.rgb(0, 0, 255, 0.3); //Couleur de la case sélectionnée : bleu translucide
    private int hoveredLastCol = -1;
    private int hoveredLastRow = -1;
    private final Color selectColor = Color.rgb(0, 255, 0, 0.3); //Couleur de la case sélectionnée : rouge translucide
    private int selectedCol = -1;
    private int selectedRow = -1;


    public GameEditorView(GameEditorController controller) {
        this.controller = controller;
        this.controller.setView(this);

        //Simple border pane qui contiendra :
        // -La selection des plateformes à gauche
        // -L'affichage du niveau dans l'éditeur au centre (ScrollPane)
        // -Le slider qui permettera de défiler horizontalement le scroll pane du centre (Slider)
        this.root = new BorderPane();

        //Le grid pane qui sera dans le scroll pane au centre
        this.gridPane = new GridPane();
        
        //Le scrollPane qui permet de défiler le gridpane
        this.scrollPane = new ScrollPane(this.gridPane);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportWidth(300);
        scrollPane.setPrefViewportHeight(200);
        

        this.root.setCenter(this.scrollPane);

        this.rectangles = new Rectangle[this.controller.rows][this.controller.cols];


        initializeGridPane();  
    }

    public GridPane getGridPane() {
        return this.gridPane;
    }

    public ScrollPane getScrollPane() {
        return this.scrollPane;
    }

    public BorderPane getBorderPane() {
        return this.root;
    }


    //Fonction qui permet d'initialiser le GridPane au centre du BorderPane.
    //Elle créer des Rectangles dans chaque cases pour pouvoir agir facilement sur les cases (couleurs, tailles, etc...)
    //Elle associe aussi les évènement visuel concernant le GridPane à leur fonctionnalité.
    private void initializeGridPane() {
        for (int i = 0; i < this.controller.rows; i++) {
            for (int j = 0; j < this.controller.cols; j++) {
                Rectangle rect = new Rectangle(this.controller.cell_size, this.controller.cell_size);
                rect.setFill(Color.WHITE);
                this.gridPane.setStyle("-fx-grid-lines-visible: true;");
                this.gridPane.add(rect, j, i);
                rectangles[i][j] = rect;
            }
        }

        //Ajout de la gestion du déplacement de la souris dans le grid pane
        this.gridPane.setOnMouseMoved(event -> {
            int col = (int) (event.getX() / this.controller.cell_size);
            int row = (int) (event.getY() / this.controller.cell_size);
            this.updateHoveredCellColor(row, col, this.hoverColor);
        });

        //Ajout de la gestion du clique de la souris dans le grid pane
        this.gridPane.setOnMousePressed(event -> {
            int col = (int) (event.getX() / this.controller.cell_size);
            int row = (int) (event.getY() / this.controller.cell_size);
            updateSelectedCellColor(row, col, this.selectColor);
        });
    }



    //Fonction qui permet de modifier la couleur d'une case séléctionnée en faisant les vérifications nécessaires.
    private void updateSelectedCellColor(int row, int col, Color color){
        if(row < 0 || row >= rectangles.length || col < 0 || col >= rectangles[0].length){
            //On ne fait rien si la case spécifiée est en dehors de la grid. 
            return;
        }

        if(row == this.selectedRow && col == this.selectedCol){
            //On ne fait rien la case spécifiée est déjà sélectionnée
            return;
        }

        rectangles[row][col].setFill(color);
        this.hoveredLastRow = -1;
        this.hoveredLastCol = -1;

        if(this.selectedRow != -1 && this.selectedCol != -1){
            //On vérifie qu'il y avait bien une case de selectionnée avant de la remettre à la couleur par défaut
            rectangles[this.selectedRow][this.selectedCol].setFill(this.defaultColor);
        }

        this.selectedRow = row;
        this.selectedCol = col;
    }


    //Fonction qui permet de modifier la couleur d'une case survolée en faisant les vérifications nécessaires.
    private void updateHoveredCellColor(int row, int col, Color color) {
        if(row < 0 || row >= rectangles.length || col < 0 || col >= rectangles[0].length){
            //On ne fait rien si la case spécifiée est en dehors de la grid. 
            return;
        }

        if(row == this.hoveredLastRow && col == this.hoveredLastCol){
            //On ne fait rien si la case spécifiée était déjà survolée
            return;
        }

        if(this.hoveredLastRow != -1 && this.hoveredLastCol != -1){
            //S'il y avait bien une ancienne case survolée, on la remet à défaut
            rectangles[this.hoveredLastRow][this.hoveredLastCol].setFill(this.defaultColor);
        }

        if(col == this.selectedCol && row == this.selectedRow){
            //On ne fait rien si la case spécifiée est sélectionné
            return;
        }

        rectangles[row][col].setFill(color);

        this.hoveredLastCol = col;
        this.hoveredLastRow = row;
    }
}
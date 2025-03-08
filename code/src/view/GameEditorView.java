package src.view;


import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import src.controller.GameEditorController;
import java.io.File;




import javafx.scene.input.ScrollEvent;


public class GameEditorView {
    private final GameEditorController controller;
    private final BorderPane root;
    private final Rectangle[][] rectangles;
    private final Color defaultColor = Color.rgb(255, 255, 255, 0.0); //Couleur de base d'une case : blanc pour l'instant mais transparent après
    private final Color hoverColor = Color.rgb(0, 0, 255, 0.3); //Couleur de la case sélectionnée : bleu translucide
    private int hoveredLastCol = -1;
    private int hoveredLastRow = -1;
    private final Color selectColor = Color.rgb(0, 255, 0, 0.3); //Couleur de la case sélectionnée : rouge translucide
    private int selectedCol = -1;
    private int selectedRow = -1;


    public GameEditorView(GameEditorController controller) {
        this.controller = controller;
        this.controller.setView(this);

        this.rectangles = new Rectangle[this.controller.rows][this.controller.cols];


        //La racine est un BorderPane qui contient :
        // -Un menu en haut pour diverses fonctionnalités
        // -La selection des plateformes à gauche grâce à un ScrollPane (A VOIR)------------------------------------------------------------------------
        // -L'affichage du niveau dans l'éditeur au centre grâce à un ScrollPane
        this.root = new BorderPane();
        initializeRoot();
    }

    public BorderPane getRoot() {
        return this.root;
    }

    private ScrollPane getCenterScrollPane(){
        return (ScrollPane)this.root.getCenter();
    }

    private GridPane getCenterGridPane(){
        return (GridPane)this.getCenterScrollPane().getContent();
    }


    //Initialise la racine et tout ce qu'elle contient
    private void initializeRoot(){
        //On ajoute au centre de la racine (BorderPane) le ScrollPane qui contient lui même un GridPane.
        //Le ScrollPane permettra de défiler facilement l'affichage du niveau.
        //Le GridPane sera utile pour gérer tous les placements bloc par bloc des plateformes
        this.root.setCenter(new ScrollPane(new GridPane()));

        initializeCenterGridPane();
        initializeCenterScrollPane();


        //On ajoute en haut de la racine un menu permettant à l'utilisateur d'effectuer diverses actions (Fichier, Settings, etc...)
        this.root.setTop(new MenuBar());

        initializeTopMenu();

    }


    //Fonction qui permet d'initialiser le ScrollPane au centre du BorderPane.
    //Elle associe les évènements concernant le ScrollPane à leur fonctionnalité.
    private void initializeCenterScrollPane(){
        ScrollPane scrollPane = this.getCenterScrollPane();

        scrollPane.addEventFilter(ScrollEvent.ANY, event -> {
            //On utilise addEventFilter sur ScrollEvent.ANY pour retirer celui que le ScrollPane avait de base

            if (event.getDeltaY() != 0) {
                //Si la molette défile verticalement, on défile horizontalement
                scrollPane.setHvalue(scrollPane.getHvalue() - (event.getDeltaY() / 1000));
            }
        });
    }


    //Fonction qui permet d'initialiser le GridPane dans le ScrollPane.
    //Elle créer des Rectangles dans chaque cases pour pouvoir agir facilement sur les cases (couleurs, tailles, etc...)
    //Elle associe aussi les évènements concernant le GridPane à leur fonctionnalité.
    private void initializeCenterGridPane(){
        GridPane gridPane = this.getCenterGridPane();

        for (int i = 0; i < this.controller.rows; i++) {
            for (int j = 0; j < this.controller.cols; j++) {
                Rectangle rect = new Rectangle(this.controller.cell_size, this.controller.cell_size);
                rect.setFill(this.defaultColor);
                gridPane.setStyle("-fx-grid-lines-visible: true;");
                gridPane.add(rect, j, i);
                rectangles[i][j] = rect;
            }
        }

        //Ajout de la gestion du déplacement de la souris dans le grid pane
        gridPane.setOnMouseMoved(event -> {
            int col = (int) (event.getX() / this.controller.cell_size);
            int row = (int) (event.getY() / this.controller.cell_size);
            this.updateHoveredCellColor(row, col, this.hoverColor);
        });

        //Ajout de la gestion du clique de la souris dans le grid pane
        gridPane.setOnMousePressed(event -> {
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


    public void initializeTopMenu(){
        MenuBar menuBar = (MenuBar)this.root.getTop();

        //Création du menu "Fichier"
        Menu fichierMenu = new Menu("Fichier");
        //Création des éléments que contient "Fichier"
        MenuItem openItem = new MenuItem("Ouvrir");
        MenuItem choseBackgroundItem = new MenuItem("Choisir une image de fond");
        choseBackgroundItem.setOnAction(event -> choseBackgroundItemAction());
        MenuItem saveItem = new MenuItem("Enregistrer");
        MenuItem quitItem = new MenuItem("Quitter");
        fichierMenu.getItems().addAll(openItem, choseBackgroundItem, saveItem, new SeparatorMenuItem(), quitItem);

        //Création du menu "Paramètres"
        Menu parametresMenu = new Menu("Paramètres");
        //Création des éléments que contient "Paramètres"
        MenuItem preferencesItem = new MenuItem("Préférences");
        parametresMenu.getItems().add(preferencesItem);

        //Création du menu "Édition"
        Menu editionMenu = new Menu("Édition");
        //Création des éléments que contient "Édition"
        MenuItem copierItem = new MenuItem("Copier");
        MenuItem collerItem = new MenuItem("Coller");
        editionMenu.getItems().addAll(copierItem, collerItem);

        //On ajoute les menus à la barre
        menuBar.getMenus().addAll(fichierMenu, parametresMenu, editionMenu);
    }


    private void choseBackgroundItemAction(){
        FileChooser  fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");

        // Filtrer pour afficher seulement les fichiers image
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );


        File selectedFile = fileChooser.showOpenDialog(null);
        if(selectedFile != null){
            Image image = new Image(selectedFile.toURI().toString());
            double newHeight = this.getCenterGridPane().getHeight();
            double ratio = newHeight / image.getHeight(); 
            double newWidth = image.getWidth() * ratio;
            

            // Load the background image
            BackgroundImage backgroundImage = new BackgroundImage(
                image,
                BackgroundRepeat.REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.	DEFAULT,
                new BackgroundSize(
                    newWidth,  // Width auto (to allow repeating)
                    newHeight,  // Height 100% (fit the GridPane height)
                    false, false,  // Don't scale width or height
                    false, false    // Contain width, cover height
                )
            );

            this.getCenterGridPane().setBackground(new Background(backgroundImage));
        }
    }

}
package src.view.editor.gameEditorSubView;

import java.io.File;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.view.editor.GameEditorView;

public class EditorMenuBar extends MenuBar{
    private GameEditorView parent;

    public EditorMenuBar(GameEditorView parent){
        this.parent = parent;

        //Création du menu "Fichier"
        Menu fichierMenu = new Menu("Fichier");
        //Création des éléments que contient "Fichier"
        MenuItem newLevelItem = new MenuItem("Nouveau");
        newLevelItem.setOnAction(e -> newLevelItemAction());
        MenuItem openLevelItem = new MenuItem("Ouvrir");
        MenuItem choseBackgroundItem = new MenuItem("Choisir une image de fond");
        choseBackgroundItem.setOnAction(event -> choseBackgroundItemAction());
        MenuItem saveLevelItem = new MenuItem("Enregistrer");
        saveLevelItem.setOnAction(event -> this.parent.getController().saveLevel());
        MenuItem quitItem = new MenuItem("Quitter");
        quitItem.setOnAction(event -> Platform.exit());
        fichierMenu.getItems().addAll(newLevelItem, openLevelItem, choseBackgroundItem, saveLevelItem, new SeparatorMenuItem(), quitItem);

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
        this.getMenus().addAll(fichierMenu, parametresMenu, editionMenu);
    }


    private void choseBackgroundItemAction(){
        //On créer un sélecteur de fichier
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");

        //On ajoute un filtre au sélecteur de fichier pour n'avoir que des images
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );


        //On ouvre la fenêtre du sélecteur de fichier
        File selectedFile = fileChooser.showOpenDialog(null);
        if(selectedFile != null){
            //On transforme le fichier choisi en instance d'Image et on calcule sa nouvelle hauteur pour qu'elle soit la
            //même que celle de la fentre du niveau (GridPane) en calculant aussi proportionnellement sa largeur 
            Image image = new Image(selectedFile.toURI().toString());
            
            parent.updateBackground(image);
        }
    }


    private void newLevelItemAction(){
        Stage newWindow = new Stage();
        newWindow.initModality(Modality.APPLICATION_MODAL);


        Label levelNameLabel = new Label("Sélectionnez le nom niveau :");
        TextField levelName = new TextField();

        Label cellSizeLabel = new Label("Sélectionnez la taille des cases de l'éditeur (16-128) :");
        Spinner<Integer> cellSizeSelection = new Spinner<>(16, 128, 16, 8);
        cellSizeSelection.setEditable(true);

        Label nbRowsLabel = new Label("Sélectionnez le nombre de ligne pour la grille (1-10000) :");
        Spinner<Integer> nbRows = new Spinner<>(1, 10000, 40, 1);
        nbRows.setEditable(true);

        Label nbColsLabel = new Label("Sélectionnez le nombre de colonne pour la grille (1-10000) :");
        Spinner<Integer> nbCols = new Spinner<>(1, 10000, 400, 1);
        nbCols.setEditable(true);


        HBox buttons = new HBox();
        Button createButton = new Button("Créer");
        createButton.setOnAction(e -> {
            this.parent.initLevel(levelName.getText(), cellSizeSelection.getValue(), nbRows.getValue(), nbCols.getValue());
            newWindow.close();
        });
        Button cancelButton = new Button("Annuler");
        cancelButton.setOnAction(e -> {
            newWindow.close();
        });
        buttons.getChildren().addAll(createButton, cancelButton);
        buttons.setStyle("-fx-padding: 10; -fx-alignment: center;");


        VBox layout = new VBox(10, levelNameLabel, levelName, cellSizeLabel, cellSizeSelection, nbRowsLabel, nbRows, nbColsLabel, nbCols, buttons);
        layout.setStyle("-fx-padding: 10; -fx-alignment: center;");

        Scene scene = new Scene(layout, 300, 400);
        newWindow.setScene(scene);
        newWindow.setTitle("Paramètres du niveau");
        newWindow.showAndWait();
    }
}
